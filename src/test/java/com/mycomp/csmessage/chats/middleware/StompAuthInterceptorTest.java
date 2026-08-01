package com.mycomp.csmessage.chats.middleware;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.mycomp.csmessage.accounts.middleware.JwtUtil;
import com.mycomp.csmessage.accounts.models.Role;
import com.mycomp.csmessage.exceptions.BaseException;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.OpenTelemetry;

import java.security.Principal;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageBuilder;

@ExtendWith(MockitoExtension.class)
class StompAuthInterceptorTest {

  private static final String SESSION_ID = "s1";

  @Mock private JwtUtil jwtUtil;
  @Mock private MessageChannel channel;

  private StompAuthInterceptor interceptor;

  @BeforeAll
  static void setupTelemetry() {
    GlobalOpenTelemetry.resetForTest();
    GlobalOpenTelemetry.set(OpenTelemetry.noop());
  }

  @BeforeEach
  void setUp() {
    interceptor = new StompAuthInterceptor(jwtUtil);
  }

  @Test
  void connectWithValidTokenSetsPrincipalAndStoresSession() {
    when(jwtUtil.isTokenExpired("valid-token")).thenReturn(false);
    when(jwtUtil.extractUserId("valid-token")).thenReturn("u1");
    when(jwtUtil.extractEmail("valid-token")).thenReturn("alice@example.com");
    when(jwtUtil.extractRole("valid-token")).thenReturn(Role.USER);

    Message<?> result = interceptor.preSend(connectMessage(SESSION_ID, "valid-token"), channel);

    Principal principal = StompHeaderAccessor.wrap(result).getUser();
    assertThat(principal).isNotNull();
    assertThat(principal.getName()).isEqualTo("alice@example.com");
  }

  @Test
  void connectWithoutAuthorizationHeaderThrows() {
    assertThatThrownBy(() -> interceptor.preSend(connectMessage(SESSION_ID, null), channel))
        .isInstanceOf(BaseException.class);
  }

  @Test
  void connectWithExpiredTokenThrows() {
    when(jwtUtil.isTokenExpired("expired-token")).thenReturn(true);

    assertThatThrownBy(
            () -> interceptor.preSend(connectMessage(SESSION_ID, "expired-token"), channel))
        .isInstanceOf(BaseException.class);
  }

  @Test
  void subsequentFrameRestoresPrincipalFromStoredSession() {
    when(jwtUtil.isTokenExpired("valid-token")).thenReturn(false);
    when(jwtUtil.extractUserId("valid-token")).thenReturn("u1");
    when(jwtUtil.extractEmail("valid-token")).thenReturn("alice@example.com");
    when(jwtUtil.extractRole("valid-token")).thenReturn(Role.USER);
    interceptor.preSend(connectMessage(SESSION_ID, "valid-token"), channel);

    Message<?> subscribe = frameMessage(StompCommand.SUBSCRIBE, SESSION_ID);

    Message<?> result = interceptor.preSend(subscribe, channel);
    assertThat(StompHeaderAccessor.wrap(result).getUser()).isNotNull();
  }

  @Test
  void disconnectRemovesStoredSession() {
    when(jwtUtil.isTokenExpired("valid-token")).thenReturn(false);
    when(jwtUtil.extractUserId("valid-token")).thenReturn("u1");
    when(jwtUtil.extractEmail("valid-token")).thenReturn("alice@example.com");
    when(jwtUtil.extractRole("valid-token")).thenReturn(Role.USER);
    interceptor.preSend(connectMessage(SESSION_ID, "valid-token"), channel);

    interceptor.preSend(frameMessage(StompCommand.DISCONNECT, SESSION_ID), channel);

    Message<?> result =
        interceptor.preSend(frameMessage(StompCommand.SUBSCRIBE, SESSION_ID), channel);
    assertThat(StompHeaderAccessor.wrap(result).getUser()).isNull();
  }

  private static Message<byte[]> connectMessage(String sessionId, String token) {
    StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.CONNECT);
    accessor.setSessionId(sessionId);
    if (token != null) {
      accessor.setNativeHeader("Authorization", "Bearer " + token);
    }
    return MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());
  }

  private static Message<byte[]> frameMessage(StompCommand command, String sessionId) {
    StompHeaderAccessor accessor = StompHeaderAccessor.create(command);
    accessor.setSessionId(sessionId);
    return MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());
  }
}
