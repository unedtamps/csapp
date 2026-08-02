# syntax=docker/dockerfile:1
FROM maven:3.9.16-eclipse-temurin-25 AS build
WORKDIR /workspace
COPY pom.xml .
RUN mvn -B -q dependency:go-offline
COPY src src
RUN mvn -B -q -DskipTests package
RUN mkdir -p extracted \
    && java -Djarmode=tools -jar target/*.jar extract --destination /workspace/extracted \
    && mv /workspace/extracted/csmessage-0.0.1-SNAPSHOT.jar /workspace/app.jar \
    && mv /workspace/extracted/lib /workspace/lib \
    && rm -rf /workspace/extracted

FROM eclipse-temurin:25-jre-noble
RUN apt-get update \
    && apt-get install -y --no-install-recommends wget \
    && rm -rf /var/lib/apt/lists/*
RUN groupadd -r app && useradd -r -g app -d /app app
WORKDIR /app
COPY --from=build /workspace/lib ./lib
COPY --from=build /workspace/app.jar ./app.jar
USER app
ENV SPRING_PROFILES_ACTIVE=prod
EXPOSE 8080
HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=3 \
  CMD wget -q --spider -O /dev/null http://localhost:8080/actuator/health || exit 1
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75.0", "-XX:+ExitOnOutOfMemoryError", "-jar", "app.jar"]
