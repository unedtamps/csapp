# CS App (csapp)

Backend service for real-time chat built with Spring Boot. Provides JWT-authenticated REST APIs for accounts and a STOMP-over-WebSocket private messaging layer, with message history persisted to MongoDB and users stored in PostgreSQL.

## Features

- **Authentication & accounts** — register, login, refresh (one-time-use rotation), logout; JWT bearer access tokens (15 min) + refresh tokens (24 h)
- **Role-based access** — `USER`, `ADMIN`, `STAFF`; admin-only endpoints and configurable seeded admin accounts
- **Real-time private messaging** — STOMP over WebSocket, brokered by RabbitMQ's STOMP plugin, delivered to per-user queues
- **Message history** — conversations persisted to MongoDB with cursor-based pagination and conversation listing
- **API documentation** — OpenAPI (Swagger UI) for REST, SpringWolf (AsyncAPI) for the STOMP contract
- **Observability** — OpenTelemetry tracing (Java agent + annotations) with an OTLP collector exporting to Grafana Cloud, local Jaeger, and MDS log correlation
- **Secrets management** — Infisical cloud (Universal Auth) injected as a Spring property source
- **Testcontainers** — integration tests spin up real PostgreSQL, MongoDB, and RabbitMQ

## Tech Stack

| Area | Choice |
|---|---|
| Language / runtime | Java 25, Spring Boot 4.1.0 |
| Build | Maven (wrapper 3.9.16) |
| Users / auth state | PostgreSQL 17 + Spring Data JPA + Flyway migrations |
| Message history | MongoDB 7 + Spring Data MongoDB |
| Realtime | Spring WebSocket + STOMP relay to RabbitMQ (`rabbitmq_stomp` plugin) |
| Security | Spring Security, jjwt 0.12.6 (HMAC-SHA), BCrypt |
| Docs | springdoc-openapi 3.0.0, SpringWolf 2.5.0 (stomp + ui) |
| Tracing | OpenTelemetry instrumentation-annotations 2.14.0 + javaagent, otel-collector-contrib |
| Secrets | Infisical SDK 3.0.7 |
| IDs | ULID (`ulid-creator`) |
| Tests | JUnit 5, Mockito, Testcontainers (postgresql, mongodb, rabbitmq), Awaitility |

## Architecture

```
                ┌─────────────────────────────────────────────────┐
                │                   csapp (8080)                  │
  REST clients  │  /auth /user /admin /chat (JwtAuthenticationFilter) │
 ───────────────▶  PostgreSQL: users, user_details, refresh_tokens│
                │  MongoDB:    message_history                    │
                └───────┬─────────────────────────────────────────┘
 WebSocket     STOMP CONNECT (Bearer) │  /app/chat.private (send)
 client ────────────────▶ WebSocket /ws │  /queue/messages.{email} (receive)
                        └──────────────┬─────────────────────────┘
                        RabbitMQ STOMP relay (61613) │
                        └────────────────────────────┘
```

- REST calls are secured by `JwtAuthenticationFilter`; WebSocket auth happens at the STOMP `CONNECT` frame via `StompAuthenticationInterceptor` (bearer token in `Authorization` header).
- Inbound messages land on `/app/chat.private` (`PrivateMessageController`) and are relayed by `PrivateMessageService` to `/queue/messages.{recipientEmail}`.
- `PrivateMessageService` publishes an in-process `MessageSentEvent`; `MessageHistoryEventListener` (async, `@WithSpan`) persists it to MongoDB. RabbitMQ is used only as the STOMP broker relay, not as a message-history queue.
- Conversation ids are canonical, order-independent joins of the two participants' emails (sorted + `_`).

## Getting Started

### Prerequisites

- JDK 25
- Docker + docker compose (for PostgreSQL, MongoDB, RabbitMQ, observability stack)

### 1. Local environment

```sh
cp .env.example .env   # fill in values
```

The repo ships a direnv `.envrc` that copies `.env.example` to `.env` on first load and sets `JAVA_TOOL_OPTIONS=-javaagent:${PWD}/opentelemetry-javaagent.jar` for local tracing.

> `.env` is gitignored. Without Infisical credentials the app falls back to local env vars (dev profile); in `prod`/`staging` profiles it refuses to start if Infisical is unreachable.

### 2. Run infrastructure

```sh
docker compose up -d postgres mongodb rabbitmq mongo-express jaeger otel-collector
```

| Service | URL / port | Credentials |
|---|---|---|
| PostgreSQL | `localhost:5432` | `postgres` / `password`, db `mydb` |
| MongoDB | `localhost:27017` | `root` / `password`, db `mydb` |
| RabbitMQ management UI | `localhost:15672` | `guest` / `guest` (STOMP on `61613`) |
| mongo-express | `localhost:8081` | `admin` / `admin` |
| Jaeger UI | `localhost:16686` | — |

### 3. Run the app

```sh
./mvnw spring-boot:run
```

Default profile is `dev` (`server.port=8080`). On first boot `DevelopmentAdminSeeder` creates a dev admin:

- email `admin@example.com` / password `password`

### 4. Verify

- Health: `http://localhost:8080/actuator/health`
- Swagger UI: `http://localhost:8080/swagger-ui.html` (disabled in prod)
- SpringWolf AsyncAPI UI: `http://localhost:8080/springwolf/asyncapi-ui.html`

## REST API

### Auth — `/auth` (public)

| Method | Path | Description |
|---|---|---|
| POST | `/auth/register` | Register (username 3–20, email, password 8–40) |
| POST | `/auth/login` | Login, returns `accessToken` + `refreshToken` |
| POST | `/auth/refresh` | Rotate refresh token (one-time use) |
| POST | `/auth/logout` | Revoke refresh token |

### User — `/user` (authenticated)

| Method | Path | Description |
|---|---|---|
| POST | `/user/details` | Create/update own `UserDetails` (address, phone, ID number, DOB, mother name) |
| GET | `/user/find-by-email?email=` | Look up a user summary by email |

### Admin — `/admin` (`ROLE_ADMIN`)

| Method | Path | Description |
|---|---|---|
| GET | `/admin/` | Health check |

### Chat history — `/chat` (authenticated)

| Method | Path | Description |
|---|---|---|
| GET | `/chat/history?peerEmail=&before=&limit=` | History with a peer; `limit` default 50 (max 100), `before` is an ISO-8601 cursor for infinite scroll |
| GET | `/chat/conversations` | All conversations, most recent first |

All authenticated endpoints require `Authorization: Bearer <accessToken>`.

## WebSocket / STOMP

| Item | Value |
|---|---|
| Endpoint | `ws://localhost:8080/ws` (raw WebSocket, no SockJS) |
| STOMP CONNECT header | `Authorization: Bearer <accessToken>` |
| Publish (send) | `/app/chat.private` — payload `{"recipientEmail": "...", "message": "..."}` |
| Subscribe (receive) | `/queue/messages.{yourEmail}` |
| Broker relay | RabbitMQ STOMP on `61613`, vhost `csapp` |

Example client flow (JavaScript/STOMP.js):

```js
const stomp = new StompJs.Client({ brokerURL: "ws://localhost:8080/ws",
  connectHeaders: { Authorization: "Bearer " + accessToken } });
stomp.onConnect = () => {
  stomp.subscribe("/queue/messages." + email, msg => console.log(msg.body));
  stomp.publish({ destination: "/app/chat.private",
    body: JSON.stringify({ recipientEmail: "bob@example.com", message: "hi" }) });
};
stomp.activate();
```

## Configuration

Application settings are read from env vars (`.env` / Infisical). Key variables:

| Variable | Default | Purpose |
|---|---|---|
| `SPRING_DATASOURCE_URL` / `_USERNAME` / `_PASSWORD` | — | PostgreSQL connection |
| `MONGODB_URI` | — | MongoDB connection string |
| `RABBITMQ_HOST` | `localhost` | STOMP relay host |
| `RABBITMQ_PORT` | `61613` | STOMP relay port |
| `RABBITMQ_VHOST` | `csapp` | STOMP vhost |
| `RABBITMQ_USER` / `RABBITMQ_PASSWORD` | `stomp-client` / `xxxxx` | Relay client credentials |
| `RABBITMQ_SYSTEM_USER` / `RABBITMQ_SYSTEM_PASSWORD` | `stomp-host` / `xxxx` | Relay system credentials |
| `SPRING_SECURITY_JWT_SECRET` | — | HMAC secret (access + refresh tokens) |
| `SPRING_SECURITY_JWT_ACCESS_TOKEN_EXPIRATION` | `900000` | Access token TTL (ms) |
| `SPRING_SECURITY_JWT_REFRESH_TOKEN_EXPIRATION` | `86400000` | Refresh token TTL (ms) |
| `SPRING_SECURITY_ADMINS` | JSON array | Admin accounts to seed in prod |
| `CORS_ALLOWED_ORIGIN_PATTERNS` | `http://localhost:5173` | Comma-separated CORS origin patterns |
| `INFISICAL_CLIENT_ID` / `_SECRET` / `INFISICAL_PROJECT_ID` | — | Infisical Universal Auth credentials |
| `INFISICAL_ENVIRONMENT` | `dev` | Infisical environment slug |
| `OTEL_EXPORTER_OTLP_ENDPOINT` | `http://localhost:4317` | OTLP collector endpoint |
| `OTEL_RESOURCE_ATTRIBUTES` | `service.name=csapp,...` | Trace resource attributes |
| `GRAFANA_AUTH` | — | Basic auth for the Grafana OTLP gateway |

See `.env.example` for the full list. Profile-specific overrides live in `application-prod.properties` (Swagger UI disabled, minimal error output) and `application-staging.properties`.

## Testing

```sh
./mvnw test
```

Integration tests use Testcontainers (PostgreSQL 16, MongoDB 7, RabbitMQ 3.13 with `rabbitmq_stomp`) and are wired via `@DynamicPropertySource`. JWT/STOMP test properties are in `src/test/resources/application-test.properties`.

## Docker

```sh
docker compose up --build csapp
```

- Multi-stage Dockerfile (build with `maven:3.9.16-eclipse-temurin-25`, run on `eclipse-temurin:25-jre-noble` as non-root `app` user, `jarmode=tools` layered extraction, healthcheck on `/actuator/health`)
- The compose `csapp` service runs with `SPRING_PROFILES_ACTIVE=staging` and expects real Infisical credentials; the OpenTelemetry agent jar is mounted read-only from the repo root
- The `otel-collector` exports traces/metrics/logs to Grafana Cloud (via `GRAFANA_AUTH`) and a debug exporter; Jaeger runs as a local OTLP backend

## Project Structure

```
src/main/java/com/mycomp/csapp
├── CsAppApplication.java             # @SpringBootApplication + @EnableAsync
├── accounts                          # auth, users, roles, refresh tokens (PostgreSQL)
│   ├── api/                          # auth, user, and admin REST controllers + DTOs
│   ├── application/                  # AuthenticationService, UserAccountService
│   ├── auth/                         # JWT, servlet, principal, and WebSocket auth
│   ├── domain/                       # account roles and domain values
│   └── persistence/jpa/              # account entities and repositories
├── chats                             # STOMP messaging + history (MongoDB)
│   ├── domain/                       # conversation identity
│   ├── history/                      # history API, application, events, listener, MongoDB
│   └── messaging/                    # private-message API, application, and payload
├── config                            # web, security, WebSocket, and documentation config
├── bootstrap                         # admin seeders/properties and Infisical integration
└── shared                            # application errors and REST error responses
src/main/resources
├── application*.properties          # base / prod / staging
├── db/migrations/                   # Flyway: V1 users, V2 refresh_tokens
└── META-INF/spring.factories        # Infisical post-processor registration
```

## Notes

- WebSocket authentication happens on the STOMP `CONNECT` frame; `QueryTokenHandshakeHandler` / `QueryTokenHandshakeInterceptor` (query-param token) are defined but not wired.
- `ddl-auto=validate` + Flyway manage the PostgreSQL schema; MongoDB collections are managed by Spring Data (compound index on conversation + timestamp).
