package com.mycomp.csapp.integration.chats;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import com.github.f4b6a3.ulid.UlidCreator;
import com.mycomp.csapp.accounts.auth.jwt.JwtTokenService;
import com.mycomp.csapp.accounts.domain.Role;
import com.mycomp.csapp.accounts.persistence.jpa.entity.UserEntity;
import com.mycomp.csapp.chats.domain.ConversationIdGenerator;
import com.mycomp.csapp.chats.history.persistence.mongodb.MessageHistoryDocument;
import com.mycomp.csapp.chats.history.persistence.mongodb.MessageHistoryRepository;
import com.mycomp.csapp.chats.messaging.payload.PrivateMessagePayload;

import java.lang.reflect.Type;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.data.domain.PageRequest;
import org.springframework.messaging.converter.JacksonJsonMessageConverter;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.mongodb.MongoDBContainer;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.rabbitmq.RabbitMQContainer;
import org.testcontainers.utility.MountableFile;

@Testcontainers
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = "spring.profiles.active=test")
@Timeout(value = 120, unit = TimeUnit.SECONDS)
class ChatWebSocketIntegrationTest {

  private static final String VHOST = "csapp";
  private static final String BROKER_USER = "csapp";
  private static final String BROKER_PASS = "csapp-pass";

  @Container
  static final PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:16-alpine");

  @Container static final MongoDBContainer mongo = new MongoDBContainer("mongo:7");

  @SuppressWarnings("resource")
  @Container
  static final RabbitMQContainer rabbit =
      new RabbitMQContainer("rabbitmq:3.13-management-alpine")
          .withAdminUser(BROKER_USER)
          .withAdminPassword(BROKER_PASS)
          .withEnv("RABBITMQ_DEFAULT_VHOST", VHOST)
          .withCopyFileToContainer(
              MountableFile.forClasspathResource("enabled_plugins"),
              "/etc/rabbitmq/enabled_plugins")
          .withExposedPorts(5672, 61613);

  @DynamicPropertySource
  static void registerProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", postgres::getJdbcUrl);
    registry.add("spring.datasource.username", postgres::getUsername);
    registry.add("spring.datasource.password", postgres::getPassword);
    registry.add("spring.mongodb.uri", () -> mongo.getConnectionString() + "/cs_app");
    registry.add("spring.rabbitmq.stomp.relay-host", rabbit::getHost);
    registry.add("spring.rabbitmq.stomp.relay-port", () -> rabbit.getMappedPort(61613));
    registry.add("spring.rabbitmq.stomp.virtual-host", () -> VHOST);
    registry.add("spring.rabbitmq.stomp.login", () -> BROKER_USER);
    registry.add("spring.rabbitmq.stomp.passcode", () -> BROKER_PASS);
    registry.add("spring.rabbitmq.stomp.system-login", () -> BROKER_USER);
    registry.add("spring.rabbitmq.stomp.system-passcode", () -> BROKER_PASS);
  }

  @LocalServerPort private int port;

  @Autowired private JwtTokenService jwtTokenService;

  @Autowired private MessageHistoryRepository messageHistoryRepository;

  @Test
  void privateMessageIsDeliveredToRecipientQueueAndPersisted() throws Exception {
    String senderEmail = "alice@example.com";
    String recipientEmail = "bob@example.com";

    StompSession senderSession = null;
    StompSession recipientSession = null;
    try {
      recipientSession = connect(recipientEmail);
       List<PrivateMessagePayload> recipientReceived = new CopyOnWriteArrayList<>();
      recipientSession.subscribe(
          "/queue/messages." + recipientEmail, new RecordingFrameHandler(recipientReceived));

      senderSession = connect(senderEmail);
       List<PrivateMessagePayload> senderReceived = new CopyOnWriteArrayList<>();
      senderSession.subscribe(
          "/queue/messages." + senderEmail, new RecordingFrameHandler(senderReceived));

       PrivateMessagePayload outbound = new PrivateMessagePayload();
      outbound.setRecipientId(recipientEmail);
      outbound.setMessage("hi bob");
      senderSession.send("/app/chat.private", outbound);

      await()
          .atMost(10, TimeUnit.SECONDS)
          .pollInterval(200, TimeUnit.MILLISECONDS)
          .untilAsserted(
              () -> {
                assertThat(recipientReceived)
                    .isNotEmpty()
                    .allMatch(message -> "hi bob".equals(message.getMessage()));
              });

       PrivateMessagePayload delivered = recipientReceived.get(0);
      assertThat(delivered.getSenderId()).isEqualTo(senderEmail);
      assertThat(delivered.getRecipientId()).isEqualTo(recipientEmail);
      assertThat(senderReceived).isEmpty();

       String conversationId =
           ConversationIdGenerator.conversationId(senderEmail, recipientEmail);
      await()
          .atMost(10, TimeUnit.SECONDS)
          .pollInterval(200, TimeUnit.MILLISECONDS)
          .untilAsserted(
              () -> {
                 List<MessageHistoryDocument> history =
                    messageHistoryRepository.findByConversationIdOrderByTimestampDesc(
                        conversationId, PageRequest.of(0, 50));
                assertThat(history).isNotEmpty();
                 MessageHistoryDocument latest = history.get(0);
                assertThat(latest.getSenderId()).isEqualTo(senderEmail);
                assertThat(latest.getRecipientId()).isEqualTo(recipientEmail);
                assertThat(latest.getMessage()).isEqualTo("hi bob");
              });
    } finally {
      if (senderSession != null) {
        senderSession.disconnect();
      }
      if (recipientSession != null) {
        recipientSession.disconnect();
      }
    }
  }

  @Test
  void connectWithInvalidTokenIsRejected() throws Exception {
    RecordingSessionHandler handler = new RecordingSessionHandler();

    WebSocketStompClient client = new WebSocketStompClient(new StandardWebSocketClient());
    client.setMessageConverter(new MappingJackson2MessageConverter());
    StompHeaders headers = new StompHeaders();
    headers.set("Authorization", "Bearer not-a-valid-token");

    try {
      client
          .connectAsync(websocketUrl(), new WebSocketHttpHeaders(), headers, handler)
          .get(3, TimeUnit.SECONDS);
    } catch (Exception ignored) {
      // Expected: CONNECT is rejected, so no CONNECTED frame (or an ERROR/close) arrives.
    }

    await()
        .atMost(10, TimeUnit.SECONDS)
        .untilAsserted(() -> assertThat(handler.isRejected()).isTrue());
    assertThat(handler.isConnected()).isFalse();
  }

  private StompSession connect(String email) throws Exception {
    WebSocketStompClient client = new WebSocketStompClient(new StandardWebSocketClient());
    client.setMessageConverter(new JacksonJsonMessageConverter());

    StompHeaders headers = new StompHeaders();
    headers.set("Authorization", "Bearer " + jwtTokenService.generateAccessToken(user(email)));

    return client
        .connectAsync(
            websocketUrl(),
            new WebSocketHttpHeaders(),
            headers,
            new StompSessionHandlerAdapter() {})
        .get(10, TimeUnit.SECONDS);
  }

  private static UserEntity user(String email) {
    return UserEntity.builder()
        .id(UlidCreator.getUlid().toString())
        .username(email.substring(0, email.indexOf('@')))
        .email(email)
        .password("unused")
        .role(Role.USER)
        .build();
  }

  private String websocketUrl() {
    return "ws://localhost:" + port + "/ws";
  }

  private static class RecordingFrameHandler implements StompFrameHandler {
     private final List<PrivateMessagePayload> messages;

     private RecordingFrameHandler(List<PrivateMessagePayload> messages) {
      this.messages = messages;
    }

    @Override
    public Type getPayloadType(StompHeaders headers) {
      return PrivateMessagePayload.class;
    }

    @Override
    public void handleFrame(StompHeaders headers, Object payload) {
      messages.add((PrivateMessagePayload) payload);
    }
  }

  private static class RecordingSessionHandler extends StompSessionHandlerAdapter {
    private final AtomicBoolean connected = new AtomicBoolean(false);
    private final AtomicBoolean errorReceived = new AtomicBoolean(false);
    private final AtomicBoolean transportClosed = new AtomicBoolean(false);

    @Override
    public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
      connected.set(true);
    }

    @Override
    public void handleFrame(StompHeaders headers, Object payload) {
      errorReceived.set(true);
    }

    @Override
    public void handleTransportError(StompSession session, Throwable exception) {
      transportClosed.set(true);
    }

    boolean isConnected() {
      return connected.get();
    }

    boolean isRejected() {
      return errorReceived.get() || transportClosed.get();
    }
  }
}
