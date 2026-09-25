package com.mycomp.csapp.accounts.application;

import com.mycomp.csapp.accounts.api.auth.dto.LoginResponse;
import com.mycomp.csapp.accounts.api.auth.dto.RegisterRequest;
import com.mycomp.csapp.accounts.auth.jwt.JwtProperties;
import com.mycomp.csapp.accounts.auth.jwt.JwtTokenService;
import com.mycomp.csapp.accounts.domain.Role;
import com.mycomp.csapp.accounts.persistence.jpa.entity.RefreshTokenEntity;
import com.mycomp.csapp.accounts.persistence.jpa.entity.UserEntity;
import com.mycomp.csapp.accounts.persistence.jpa.repository.RefreshTokenRepository;
import com.mycomp.csapp.accounts.persistence.jpa.repository.UserRepository;
import com.mycomp.csapp.shared.error.ApplicationException;

import jakarta.transaction.Transactional;

import java.time.Instant;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtTokenService jwtTokenService;
  private final RefreshTokenRepository refreshTokenRepository;
  private final JwtProperties jwtProperties;

  @Transactional
  public LoginResponse login(String email, String password) {
    UserEntity user = userRepository.findByEmail(email);
    if (user == null) {
      throw invalidCredentials();
    }

    if (!passwordEncoder.matches(password, user.getPassword())) {
      throw invalidCredentials();
    }

    return createLoginResponse(user);
  }

  public UserEntity register(RegisterRequest request) {
    if (userRepository.existsByEmail(request.email())) {
      throw new ApplicationException(HttpStatus.CONFLICT, "Email already exists");
    }

    if (userRepository.existsByUsername(request.username())) {
      throw new ApplicationException(HttpStatus.CONFLICT, "Username already exists");
    }

    return userRepository.save(toUser(request));
  }

  @Transactional
  public LoginResponse refresh(String refreshToken) {
    if (jwtTokenService.isTokenExpired(refreshToken)) {
      throw new ApplicationException(HttpStatus.UNAUTHORIZED, "Refresh token expired");
    }

    String ulid = jwtTokenService.extractJti(refreshToken);
    int deleted = refreshTokenRepository.deleteByIdReturningCount(ulid);
    if (deleted == 0) {
      throw new ApplicationException(
          HttpStatus.UNAUTHORIZED, "Refresh token already used or invalid");
    }

    String userId = jwtTokenService.extractUserId(refreshToken);
    UserEntity user =
        userRepository
            .findById(userId)
            .orElseThrow(
                () -> new ApplicationException(HttpStatus.UNAUTHORIZED, "User not found"));

    return createLoginResponse(user);
  }

  @Transactional
  public void logout(String refreshToken) {
    String ulid = jwtTokenService.extractJti(refreshToken);
    refreshTokenRepository.deleteByIdReturningCount(ulid);
  }

  private UserEntity toUser(RegisterRequest request) {
    return UserEntity.builder()
        .email(request.email())
        .username(request.username())
        .password(passwordEncoder.encode(request.password()))
        .role(Role.USER)
        .build();
  }

  private LoginResponse createLoginResponse(UserEntity user) {
    String accessToken = jwtTokenService.generateAccessToken(user);
    String refreshToken = jwtTokenService.generateRefreshToken(user);

    saveRefreshToken(user, refreshToken);

    return LoginResponse.builder().accessToken(accessToken).refreshToken(refreshToken).build();
  }

  private static ApplicationException invalidCredentials() {
    return new ApplicationException(HttpStatus.UNAUTHORIZED, "User or password is incorrect");
  }

  private void saveRefreshToken(UserEntity user, String token) {
    String id = jwtTokenService.extractJti(token);
    Instant expiresAt = Instant.now().plusMillis(jwtProperties.getRefreshTokenExpiration());
    refreshTokenRepository.save(new RefreshTokenEntity(id, user, expiresAt, null));
  }
}
