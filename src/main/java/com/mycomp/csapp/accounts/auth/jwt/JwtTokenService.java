package com.mycomp.csapp.accounts.auth.jwt;

import com.github.f4b6a3.ulid.UlidCreator;
import com.mycomp.csapp.accounts.auth.principal.AuthenticatedUser;
import com.mycomp.csapp.accounts.domain.Role;
import com.mycomp.csapp.accounts.persistence.jpa.entity.UserEntity;
import com.mycomp.csapp.shared.error.ApplicationException;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
public class JwtTokenService {

  private final JwtProperties jwtProperties;

  private final SecretKey secretKey;

  public JwtTokenService(JwtProperties jwtProperties) {
    this.jwtProperties = jwtProperties;
    this.secretKey =
        Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8));
  }

  public String generateAccessToken(UserEntity user) {
    return Jwts.builder()
        .subject(user.getId())
        .claim("email", user.getEmail())
        .claim("role", user.getRole().name())
        .expiration(new Date(System.currentTimeMillis() + jwtProperties.getAccessTokenExpiration()))
        .issuedAt(new Date())
        .signWith(secretKey)
        .compact();
  }

  public String generateRefreshToken(UserEntity user) {
    return Jwts.builder()
        .id(UlidCreator.getUlid().toString())
        .subject(user.getId())
        .issuedAt(new Date())
        .expiration(new Date(System.currentTimeMillis() + jwtProperties.getRefreshTokenExpiration()))
        .signWith(secretKey)
        .compact();
  }

  public String extractUserId(String token) {
    return getClaims(token).getSubject();
  }

  public AuthenticatedUser extractAuthClaims(String token) {
    return AuthenticatedUser.builder()
        .id(extractUserId(token))
        .email(extractEmail(token))
        .role(extractRole(token))
        .build();
  }

  public String extractEmail(String token) {
    return getClaims(token).get("email", String.class);
  }

  public Role extractRole(String token) {
    return Role.valueOf(getClaims(token).get("role", String.class));
  }

  public String extractJti(String token) {
    return getClaims(token).getId();
  }

  public boolean isTokenExpired(String token) {
    try {
      return getClaims(token).getExpiration().before(new Date());
    } catch (ExpiredJwtException e) {
      return true;
    }
  }

  public Claims getClaims(String token) {
    try {
      return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload();
    } catch (ExpiredJwtException e) {
      throw e;
    } catch (JwtException e) {
      throw new ApplicationException(HttpStatus.UNAUTHORIZED, "Invalid token");
    }
  }
}
