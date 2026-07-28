package com.mycomp.csmessage.accounts.middleware;

import com.mycomp.csmessage.accounts.models.Role;
import com.mycomp.csmessage.accounts.models.User;
import com.mycomp.csmessage.config.JwtConfig;
import com.mycomp.csmessage.exceptions.BaseException;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
public class JwtUtil {

  private final JwtConfig jwtConfig;

  private final SecretKey secretKey;

  public JwtUtil(JwtConfig jwtConfig) {
    this.jwtConfig = jwtConfig;
    this.secretKey = Keys.hmacShaKeyFor(jwtConfig.getSecret().getBytes(StandardCharsets.UTF_8));
  }

  public String generateAccessToken(User user) {
    return Jwts.builder()
        .subject(user.getId())
        .claim("email", user.getEmail())
        .claim("role", user.getRole().name())
        .expiration(new Date(System.currentTimeMillis() + jwtConfig.getAccessTokenExpiration()))
        .issuedAt(new Date())
        .signWith(secretKey)
        .compact();
  }

  public String generateRefreshToken(User user) {
    return Jwts.builder()
        .subject(user.getId())
        .issuedAt(new Date())
        .expiration(new Date(System.currentTimeMillis() + jwtConfig.getRefreshTokenExpiration()))
        .signWith(secretKey)
        .compact();
  }

  public String extractUserId(String token) {
    return getClaims(token).getSubject();
  }

  public String extractEmail(String token) {
    return getClaims(token).get("email", String.class);
  }

  public Role extractRole(String token) {
    return Role.valueOf(getClaims(token).get("role", String.class));
  }

  public boolean isTokenExpired(String token) {
    return getClaims(token).getExpiration().before(new Date());
  }

  public Claims getClaims(String token) {
    try {
      return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload();
    } catch (JwtException e) {
      throw new BaseException(HttpStatus.UNAUTHORIZED, "Invalid token");
    }
  }
}
