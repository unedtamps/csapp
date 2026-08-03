package com.mycomp.csapp.accounts.middleware;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.mycomp.csapp.accounts.models.Role;
import com.mycomp.csapp.accounts.models.User;
import com.mycomp.csapp.config.security.JwtConfig;
import com.mycomp.csapp.exceptions.BaseException;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import javax.crypto.SecretKey;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class JwtUtilTest {

  private static final String SECRET = "unit-test-secret-0123456789abcdef0123456789abcdef";

  private JwtUtil jwtUtil;

  @BeforeEach
  void setUp() {
    JwtConfig config = new JwtConfig();
    config.setSecret(SECRET);
    config.setAccessTokenExpiration(900000);
    config.setRefreshTokenExpiration(86400000);
    jwtUtil = new JwtUtil(config);
  }

  @Test
  void accessTokenRoundTripsClaims() {
    User user =
        User.builder().id("u1").email("alice@example.com").password("x").role(Role.USER).build();

    String token = jwtUtil.generateAccessToken(user);

    assertThat(jwtUtil.extractUserId(token)).isEqualTo("u1");
    assertThat(jwtUtil.extractEmail(token)).isEqualTo("alice@example.com");
    assertThat(jwtUtil.extractRole(token)).isEqualTo(Role.USER);
    assertThat(jwtUtil.isTokenExpired(token)).isFalse();
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

    assertThat(jwtUtil.isTokenExpired(expired)).isTrue();
  }

  @Test
  void tamperedTokenThrows() {
    User user =
        User.builder().id("u1").email("alice@example.com").password("x").role(Role.USER).build();
    String token = jwtUtil.generateAccessToken(user);
    String tampered = token.substring(0, token.length() - 2) + "xx";

    assertThatThrownBy(() -> jwtUtil.extractUserId(tampered))
        .isInstanceOf(BaseException.class);
  }
}
