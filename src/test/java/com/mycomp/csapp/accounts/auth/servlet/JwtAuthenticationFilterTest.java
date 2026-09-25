package com.mycomp.csapp.accounts.auth.servlet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.mycomp.csapp.accounts.auth.jwt.JwtTokenService;
import com.mycomp.csapp.accounts.auth.principal.AuthenticatedUser;
import com.mycomp.csapp.accounts.domain.Role;

import jakarta.servlet.FilterChain;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

  @Mock private JwtTokenService jwtTokenService;
  @Mock private FilterChain filterChain;

  private JwtAuthenticationFilter filter;

  @BeforeEach
  void setUp() {
    filter = new JwtAuthenticationFilter(jwtTokenService);
    SecurityContextHolder.clearContext();
  }

  @AfterEach
  void tearDown() {
    SecurityContextHolder.clearContext();
  }

  @Test
  void validBearerTokenSetsClaimsAndRoleAuthority() throws Exception {
    when(jwtTokenService.extractAuthClaims("valid-token"))
        .thenReturn(new AuthenticatedUser("u1", "alice@example.com", Role.ADMIN));
    when(jwtTokenService.isTokenExpired("valid-token")).thenReturn(false);

    MockHttpServletRequest request = requestWithBearerToken("valid-token");
    filter.doFilter(request, new MockHttpServletResponse(), filterChain);

    var authentication = SecurityContextHolder.getContext().getAuthentication();
    assertThat(authentication).isNotNull();
    assertThat(authentication.getPrincipal())
        .isEqualTo(new AuthenticatedUser("u1", "alice@example.com", Role.ADMIN));
    assertThat(authentication.getAuthorities())
        .extracting(Object::toString)
        .containsExactly("ROLE_ADMIN");
    verify(filterChain).doFilter(any(), any());
  }

  @Test
  void requestWithoutBearerTokenPassesThroughUnauthenticated() throws Exception {
    MockHttpServletRequest request = new MockHttpServletRequest();

    filter.doFilter(request, new MockHttpServletResponse(), filterChain);

    assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    verify(filterChain).doFilter(any(), any());
  }

  @Test
  void expiredBearerTokenPassesThroughUnauthenticated() throws Exception {
    when(jwtTokenService.extractAuthClaims("expired-token"))
        .thenReturn(new AuthenticatedUser("u1", "alice@example.com", Role.USER));
    when(jwtTokenService.isTokenExpired("expired-token")).thenReturn(true);

    filter.doFilter(
        requestWithBearerToken("expired-token"),
        new MockHttpServletResponse(),
        filterChain);

    assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
  }

  private static MockHttpServletRequest requestWithBearerToken(String token) {
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.addHeader("Authorization", "Bearer " + token);
    return request;
  }
}
