package com.mycomp.csapp.accounts.auth.websocket;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.mycomp.csapp.accounts.auth.jwt.JwtTokenService;
import com.mycomp.csapp.accounts.auth.principal.AuthenticatedUser;
import com.mycomp.csapp.accounts.domain.Role;
import com.mycomp.csapp.shared.error.ApplicationException;

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
class StompAuthenticationInterceptorTest {

  private static final String SESSION_ID = "s1";

  @Mock private JwtTokenService jwtTokenService;
  @Mock private MessageChannel channel;

  private StompAuthenticationInterceptor interceptor;

  @BeforeAll
  static void setupTelemetry() {
    GlobalOpenTelemetry.resetForTest();
    GlobalOpenTelemetry.set(OpenTelemetry.noop());
  }

  @BeforeEach
  void setUp() {
    interceptor = new StompAuthenticationInterceptor(jwtTokenService);
  }

  @Test
  void connectWithValidTokenSetsPrincipalAndStoresSession() {
    when(jwtTokenService.isTokenExpired("valid-token")).thenReturn(false);
    when(jwtTokenService.extractAuthClaims("valid-token"))
        .thenReturn(new AuthenticatedUser("u1", "alice@example.com", Role.USER));

    Message<?> result = interceptor.preSend(connectMessage(SESSION_ID, "valid-token"), channel);

    Principal principal = StompHeaderAccessor.wrap(result).getUser();
    assertThat(principal).isNotNull();
    assertThat(principal.getName()).isEqualTo("alice@example.com");
  }

  @Test
  void connectWithoutAuthorizationHeaderThrows() {
    assertThatThrownBy(() -> interceptor.preSend(connectMessage(SESSION_ID, null), channel))
        .isInstanceOf(ApplicationException.class);
  }

  @Test
  void connectWithExpiredTokenThrows() {
    when(jwtTokenService.isTokenExpired("expired-token")).thenReturn(true);

    assertThatThrownBy(
            () -> interceptor.preSend(connectMessage(SESSION_ID, "expired-token"), channel))
        .isInstanceOf(ApplicationException.class);
  }

  @Test
  void subsequentFrameRestoresPrincipalFromStoredSession() {
    when(jwtTokenService.isTokenExpired("valid-token")).thenReturn(false);
    when(jwtTokenService.extractAuthClaims("valid-token"))
        .thenReturn(new AuthenticatedUser("u1", "alice@example.com", Role.USER));
    interceptor.preSend(connectMessage(SESSION_ID, "valid-token"), channel);

    Message<?> subscribe = frameMessage(StompCommand.SUBSCRIBE, SESSION_ID);

    Message<?> result = interceptor.preSend(subscribe, channel);
    assertThat(StompHeaderAccessor.wrap(result).getUser()).isNotNull();
  }

  @Test
  void disconnectRemovesStoredSession() {
    when(jwtTokenService.isTokenExpired("valid-token")).thenReturn(false);
    when(jwtTokenService.extractAuthClaims("valid-token"))
        .thenReturn(new AuthenticatedUser("u1", "alice@example.com", Role.USER));
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
