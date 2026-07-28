package com.mycomp.csmessage.accounts.service;

import com.mycomp.csmessage.accounts.dto.LoginResponse;
import com.mycomp.csmessage.accounts.dto.RegisterRequest;
import com.mycomp.csmessage.accounts.middleware.JwtUtil;
import com.mycomp.csmessage.accounts.models.Role;
import com.mycomp.csmessage.accounts.models.User;
import com.mycomp.csmessage.accounts.repository.UsersRepository;
import com.mycomp.csmessage.exceptions.BaseException;

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

  public LoginResponse refresh(String refreshToken) {
    String userId = jwtUtil.extractUserId(refreshToken);

    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new BaseException(HttpStatus.UNAUTHORIZED, "User not found"));

    return LoginResponse.builder()
        .accessToken(jwtUtil.generateAccessToken(user))
        .refreshToken(refreshToken)
        .build();
  }
}
