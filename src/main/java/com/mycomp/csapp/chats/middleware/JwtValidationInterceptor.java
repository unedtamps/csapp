package com.mycomp.csapp.chats.middleware;

import com.mycomp.csapp.accounts.middleware.JwtUtil;

import java.util.Map;

import lombok.RequiredArgsConstructor;

import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

@Component
@RequiredArgsConstructor
public class JwtValidationInterceptor implements HandshakeInterceptor {

  private final JwtUtil jwtUtil;

  @Override
  public boolean beforeHandshake(
      ServerHttpRequest request,
      ServerHttpResponse response,
      WebSocketHandler wsHandler,
      Map<String, Object> attributes) {

    String query = request.getURI().getQuery();
    if (query == null || !query.contains("token=")) {
      return false;
    }

    String token = extractTokenFromQuery(query);
    if (token == null) {
      return false;
    }

    try {
      return !jwtUtil.isTokenExpired(token);
    } catch (Exception e) {
      return false;
    }
  }

  @Override
  public void afterHandshake(
      ServerHttpRequest request,
      ServerHttpResponse response,
      WebSocketHandler wsHandler,
      Exception exception) {}

  private static String extractTokenFromQuery(String query) {
    for (String param : query.split("&")) {
      String[] pair = param.split("=", 2);
      if (pair.length == 2 && "token".equals(pair[0])) {
        return pair[1];
      }
    }
    return null;
  }
}
