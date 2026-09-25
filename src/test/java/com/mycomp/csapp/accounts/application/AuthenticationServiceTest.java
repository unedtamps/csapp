package com.mycomp.csapp.accounts.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.mycomp.csapp.accounts.api.auth.dto.RegisterRequest;
import com.mycomp.csapp.accounts.auth.jwt.JwtProperties;
import com.mycomp.csapp.accounts.auth.jwt.JwtTokenService;
import com.mycomp.csapp.accounts.domain.Role;
import com.mycomp.csapp.accounts.persistence.jpa.entity.RefreshTokenEntity;
import com.mycomp.csapp.accounts.persistence.jpa.entity.UserEntity;
import com.mycomp.csapp.accounts.persistence.jpa.repository.RefreshTokenRepository;
import com.mycomp.csapp.accounts.persistence.jpa.repository.UserRepository;
import com.mycomp.csapp.shared.error.ApplicationException;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

  @Mock private UserRepository userRepository;
  @Mock private PasswordEncoder passwordEncoder;
  @Mock private JwtTokenService jwtTokenService;
  @Mock private RefreshTokenRepository refreshTokenRepository;
  @Mock private JwtProperties jwtProperties;

  private AuthenticationService authenticationService;

  @BeforeEach
  void setUp() {
    authenticationService =
        new AuthenticationService(
            userRepository, passwordEncoder, jwtTokenService, refreshTokenRepository, jwtProperties);
  }

  @Test
  void registerCreatesAnEncodedUserWithTheDefaultRole() {
    RegisterRequest request = new RegisterRequest("alice", "alice@example.com", "password");
    when(userRepository.existsByEmail(request.email())).thenReturn(false);
    when(userRepository.existsByUsername(request.username())).thenReturn(false);
    when(passwordEncoder.encode(request.password())).thenReturn("encoded-password");
    when(userRepository.save(any(UserEntity.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    UserEntity result = authenticationService.register(request);

    ArgumentCaptor<UserEntity> captor = ArgumentCaptor.forClass(UserEntity.class);
    verify(userRepository).save(captor.capture());
    UserEntity saved = captor.getValue();

    assertThat(result).isSameAs(saved);
    assertThat(saved.getUsername()).isEqualTo("alice");
    assertThat(saved.getEmail()).isEqualTo("alice@example.com");
    assertThat(saved.getPassword()).isEqualTo("encoded-password");
    assertThat(saved.getRole()).isEqualTo(Role.USER);
  }

  @Test
  void registerRejectsAnExistingEmail() {
    RegisterRequest request = new RegisterRequest("alice", "alice@example.com", "password");
    when(userRepository.existsByEmail(request.email())).thenReturn(true);

    assertThatThrownBy(() -> authenticationService.register(request))
        .isInstanceOf(ApplicationException.class)
        .hasMessage("Email already exists")
        .satisfies(
            exception ->
                assertThat(((ApplicationException) exception).getStatus())
                    .isEqualTo(HttpStatus.CONFLICT));
  }

  @Test
  void loginReturnsTokensAndStoresTheRefreshToken() {
    UserEntity user = user("u1", "alice@example.com");
    when(userRepository.findByEmail(user.getEmail())).thenReturn(user);
    when(passwordEncoder.matches("password", user.getPassword())).thenReturn(true);
    when(jwtTokenService.generateAccessToken(user)).thenReturn("access-token");
    when(jwtTokenService.generateRefreshToken(user)).thenReturn("refresh-token");
    when(jwtTokenService.extractJti("refresh-token")).thenReturn("refresh-id");
    when(jwtProperties.getRefreshTokenExpiration()).thenReturn(86_400_000L);

    var response = authenticationService.login(user.getEmail(), "password");

    assertThat(response.accessToken()).isEqualTo("access-token");
    assertThat(response.refreshToken()).isEqualTo("refresh-token");

    ArgumentCaptor<RefreshTokenEntity> captor = ArgumentCaptor.forClass(RefreshTokenEntity.class);
    verify(refreshTokenRepository).save(captor.capture());
    assertThat(captor.getValue().getId()).isEqualTo("refresh-id");
    assertThat(captor.getValue().getUser()).isSameAs(user);
  }

  @Test
  void refreshDeletesTheOldTokenBeforeSavingTheReplacement() {
    UserEntity user = user("u1", "alice@example.com");
    when(jwtTokenService.isTokenExpired("old-refresh-token")).thenReturn(false);
    when(jwtTokenService.extractJti("old-refresh-token")).thenReturn("old-refresh-id");
    when(refreshTokenRepository.deleteByIdReturningCount("old-refresh-id")).thenReturn(1);
    when(jwtTokenService.extractUserId("old-refresh-token")).thenReturn(user.getId());
    when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
    when(jwtTokenService.generateAccessToken(user)).thenReturn("new-access-token");
    when(jwtTokenService.generateRefreshToken(user)).thenReturn("new-refresh-token");
    when(jwtTokenService.extractJti("new-refresh-token")).thenReturn("new-refresh-id");
    when(jwtProperties.getRefreshTokenExpiration()).thenReturn(86_400_000L);

    var response = authenticationService.refresh("old-refresh-token");

    assertThat(response.accessToken()).isEqualTo("new-access-token");
    assertThat(response.refreshToken()).isEqualTo("new-refresh-token");
    verify(refreshTokenRepository).deleteByIdReturningCount("old-refresh-id");

    ArgumentCaptor<RefreshTokenEntity> captor = ArgumentCaptor.forClass(RefreshTokenEntity.class);
    verify(refreshTokenRepository).save(captor.capture());
    assertThat(captor.getValue().getId()).isEqualTo("new-refresh-id");
    assertThat(captor.getValue().getUser()).isSameAs(user);
  }

  @Test
  void logoutRevokesTheRefreshToken() {
    when(jwtTokenService.extractJti("refresh-token")).thenReturn("refresh-id");

    authenticationService.logout("refresh-token");

    verify(refreshTokenRepository).deleteByIdReturningCount("refresh-id");
  }

  private static UserEntity user(String id, String email) {
    return UserEntity.builder()
        .id(id)
        .username(email.substring(0, email.indexOf('@')))
        .email(email)
        .password("encoded-password")
        .role(Role.USER)
        .build();
  }
}
