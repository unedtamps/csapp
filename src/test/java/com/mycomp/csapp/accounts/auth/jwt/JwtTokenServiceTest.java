package com.mycomp.csapp.accounts.auth.jwt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.mycomp.csapp.accounts.auth.principal.AuthenticatedUser;
import com.mycomp.csapp.accounts.domain.Role;
import com.mycomp.csapp.accounts.persistence.jpa.entity.UserEntity;
import com.mycomp.csapp.shared.error.ApplicationException;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import javax.crypto.SecretKey;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class JwtTokenServiceTest {

  private static final String SECRET = "unit-test-secret-0123456789abcdef0123456789abcdef";

  private JwtTokenService jwtTokenService;

  @BeforeEach
  void setUp() {
    JwtProperties config = new JwtProperties();
    config.setSecret(SECRET);
    config.setAccessTokenExpiration(900000);
    config.setRefreshTokenExpiration(86400000);
    jwtTokenService = new JwtTokenService(config);
  }

  @Test
  void accessTokenRoundTripsClaims() {
    UserEntity user =
        UserEntity.builder()
            .id("u1")
            .email("alice@example.com")
            .password("x")
            .role(Role.USER)
            .build();

    String token = jwtTokenService.generateAccessToken(user);

    assertThat(jwtTokenService.extractUserId(token)).isEqualTo("u1");
    assertThat(jwtTokenService.extractEmail(token)).isEqualTo("alice@example.com");
    assertThat(jwtTokenService.extractRole(token)).isEqualTo(Role.USER);
    assertThat(jwtTokenService.isTokenExpired(token)).isFalse();
  }

  @Test
  void accessTokenClaimsCanBeBuiltAsAnAuthenticationPrincipal() {
    UserEntity user =
        UserEntity.builder()
            .id("u1")
            .email("alice@example.com")
            .password("x")
            .role(Role.USER)
            .build();

    AuthenticatedUser claims =
        jwtTokenService.extractAuthClaims(jwtTokenService.generateAccessToken(user));

    assertThat(claims).isEqualTo(new AuthenticatedUser("u1", "alice@example.com", Role.USER));
  }

  @Test
  void expiredTokenIsDetected() {
    SecretKey key = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
    String expired =
        Jwts.builder()
            .subject("u1")
            .expiration(new Date(System.currentTimeMillis() - 60_000))
            .signWith(key)
            .compact();

    assertThat(jwtTokenService.isTokenExpired(expired)).isTrue();
  }

  @Test
  void tamperedTokenThrows() {
    UserEntity user =
        UserEntity.builder()
            .id("u1")
            .email("alice@example.com")
            .password("x")
            .role(Role.USER)
            .build();
    String token = jwtTokenService.generateAccessToken(user);
    String tampered = token.substring(0, token.length() - 2) + "xx";

    assertThatThrownBy(() -> jwtTokenService.extractUserId(tampered))
        .isInstanceOf(ApplicationException.class);
  }
}
