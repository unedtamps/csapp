package com.mycomp.csapp.accounts.auth.websocket;

import com.mycomp.csapp.accounts.auth.jwt.JwtTokenService;
import com.mycomp.csapp.accounts.auth.principal.AuthenticatedUser;
import com.mycomp.csapp.shared.error.ApplicationException;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;

import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class StompAuthenticationInterceptor implements ChannelInterceptor {

  private static final String TRACER_NAME = "com.mycomp.csapp.chats.middleware.StompAuthInterceptor";

  private final JwtTokenService jwtTokenService;

  private final Map<String, Principal> sessionUsers = new ConcurrentHashMap<>();

  @Override
  public Message<?> preSend(Message<?> message, MessageChannel channel) {
    StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);

    if (StompCommand.CONNECT.equals(accessor.getCommand())) {
      Tracer tracer = GlobalOpenTelemetry.getTracer(TRACER_NAME);
      Span span = tracer.spanBuilder("STOMP CONNECT auth").startSpan();
      try (Scope ignored = span.makeCurrent()) {
        String authHeader = accessor.getFirstNativeHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
          throw new ApplicationException(
              HttpStatus.UNAUTHORIZED, "Missing or invalid Authorization header");
        }

        String token = authHeader.substring(7);

        if (jwtTokenService.isTokenExpired(token)) {
          throw new ApplicationException(HttpStatus.UNAUTHORIZED, "Token expired");
        }

        AuthenticatedUser claims = jwtTokenService.extractAuthClaims(token);

        var authorities = List.of(new SimpleGrantedAuthority("ROLE_" + claims.role().name()));
        Principal principal = new UsernamePasswordAuthenticationToken(claims, null, authorities);
        accessor.setUser(principal);
        message = MessageBuilder.createMessage(message.getPayload(), accessor.getMessageHeaders());

        String sessionId = accessor.getSessionId();
        if (sessionId != null) {
          sessionUsers.put(sessionId, principal);
          log.info("Session CONNECT {} authenticated as {}", sessionId, principal.getName());
          span.setAttribute("session.id", sessionId);
          span.setAttribute("user.email", claims.email());
        }
      } catch (ApplicationException e) {
        span.setAttribute("auth.error", e.getMessage());
        throw e;
      } catch (Exception e) {
        span.recordException(e);
        span.setStatus(StatusCode.ERROR);
        throw e;
      } finally {
        span.end();
      }

    } else if (StompCommand.DISCONNECT.equals(accessor.getCommand())) {
      String sessionId = accessor.getSessionId();
      if (sessionId != null) {
        sessionUsers.remove(sessionId);
        log.info("Session {} disconnected", sessionId);
      }
    }

    if (accessor.getUser() == null) {
      String sessionId = accessor.getSessionId();
      Principal principal = sessionUsers.get(sessionId);
      if (principal != null) {
        accessor.setUser(principal);
        message = MessageBuilder.createMessage(message.getPayload(), accessor.getMessageHeaders());
      }
    }
    return message;
  }
}
