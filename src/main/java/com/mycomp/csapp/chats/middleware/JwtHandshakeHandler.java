package com.mycomp.csapp.chats.middleware;

import com.mycomp.csapp.accounts.dto.AuthClaims;
import com.mycomp.csapp.accounts.middleware.JwtUtil;

import java.security.Principal;
import java.util.List;
import java.util.Map;

import lombok.RequiredArgsConstructor;

import org.springframework.http.server.ServerHttpRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

@Component
@RequiredArgsConstructor
public class JwtHandshakeHandler extends DefaultHandshakeHandler {

  private final JwtUtil jwtUtil;

  @Override
  protected Principal determineUser(
      ServerHttpRequest request, WebSocketHandler handler, Map<String, Object> attributes) {

    String query = request.getURI().getQuery();
    if (query == null || !query.contains("token=")) {
      return null;
    }

    String token = extractTokenFromQuery(query);
    if (token == null) {
      return null;
    }

    try {
      if (jwtUtil.isTokenExpired(token)) {
        return null;
      }

      AuthClaims claims =
          AuthClaims.builder()
              .id(jwtUtil.extractUserId(token))
              .email(jwtUtil.extractEmail(token))
              .role(jwtUtil.extractRole(token))
              .build();

      var authorities = List.of(new SimpleGrantedAuthority("ROLE_" + claims.role().name()));
      return new UsernamePasswordAuthenticationToken(claims, null, authorities);

    } catch (Exception e) {
      return null;
    }
  }

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
