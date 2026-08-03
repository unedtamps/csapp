package com.mycomp.csapp.accounts.service;

import com.mycomp.csapp.accounts.dto.LoginResponse;
import com.mycomp.csapp.accounts.dto.RegisterRequest;
import com.mycomp.csapp.accounts.middleware.JwtUtil;
import com.mycomp.csapp.accounts.models.RefreshToken;
import com.mycomp.csapp.accounts.models.Role;
import com.mycomp.csapp.accounts.models.User;
import com.mycomp.csapp.accounts.repository.RefreshTokenRepository;
import com.mycomp.csapp.accounts.repository.UsersRepository;
import com.mycomp.csapp.config.security.JwtConfig;
import com.mycomp.csapp.exceptions.BaseException;

import jakarta.transaction.Transactional;

import java.time.Instant;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

  private final UsersRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtUtil jwtUtil;
  private final RefreshTokenRepository refreshTokenRepository;
  private final JwtConfig jwtConfig;

  @Transactional
  public LoginResponse login(String email, String password) {
    User user = userRepository.findByEmail(email);
    if (user == null) {
      throw new BaseException(HttpStatus.UNAUTHORIZED, "User or password is incorrect");
    }

    if (!passwordEncoder.matches(password, user.getPassword())) {
      throw new BaseException(HttpStatus.UNAUTHORIZED, "User or password is incorrect");
    }

    String accessToken = jwtUtil.generateAccessToken(user);
    String refreshToken = jwtUtil.generateRefreshToken(user);

    saveRefreshToken(user, refreshToken);

    return LoginResponse.builder().accessToken(accessToken).refreshToken(refreshToken).build();
  }

  public User register(RegisterRequest request) {
    if (userRepository.existsByEmail(request.email())) {
      throw new BaseException(HttpStatus.CONFLICT, "Email already exists");
    }

    if (userRepository.existsByUsername(request.username())) {
      throw new BaseException(HttpStatus.CONFLICT, "Username already exists");
    }

    return userRepository.save(
        User.builder()
            .email(request.email())
            .username(request.username())
            .password(passwordEncoder.encode(request.password()))
            .role(Role.USER)
            .build());
  }

  @Transactional
  public LoginResponse refresh(String refreshToken) {
    if (jwtUtil.isTokenExpired(refreshToken)) {
      throw new BaseException(HttpStatus.UNAUTHORIZED, "Refresh token expired");
    }

    String ulid = jwtUtil.extractJti(refreshToken);
    int deleted = refreshTokenRepository.deleteByIdReturningCount(ulid);
    if (deleted == 0) {
      throw new BaseException(HttpStatus.UNAUTHORIZED, "Refresh token already used or invalid");
    }

    String userId = jwtUtil.extractUserId(refreshToken);
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new BaseException(HttpStatus.UNAUTHORIZED, "User not found"));

    String newAccessToken = jwtUtil.generateAccessToken(user);
    String newRefreshToken = jwtUtil.generateRefreshToken(user);

    saveRefreshToken(user, newRefreshToken);

    return LoginResponse.builder()
        .accessToken(newAccessToken)
        .refreshToken(newRefreshToken)
        .build();
  }

  @Transactional
  public void logout(String refreshToken) {
    String ulid = jwtUtil.extractJti(refreshToken);
    refreshTokenRepository.deleteByIdReturningCount(ulid);
  }

  private void saveRefreshToken(User user, String token) {
    String id = jwtUtil.extractJti(token);
    Instant expiresAt = Instant.now().plusMillis(jwtConfig.getRefreshTokenExpiration());
    refreshTokenRepository.save(new RefreshToken(id, user, expiresAt, null));
  }
}
