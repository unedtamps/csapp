package com.mycomp.csmessage.accounts.controller;

import com.mycomp.csmessage.accounts.dto.LoginRequest;
import com.mycomp.csmessage.accounts.dto.LoginResponse;
import com.mycomp.csmessage.accounts.dto.LogoutRequest;
import com.mycomp.csmessage.accounts.dto.RefreshTokenRequest;
import com.mycomp.csmessage.accounts.dto.RegisterRequest;
import com.mycomp.csmessage.accounts.models.User;
import com.mycomp.csmessage.accounts.service.AuthService;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

  private final AuthService authService;

  @PostMapping("/register")
  public ResponseEntity<User> register(@Valid @RequestBody RegisterRequest request) {
    User user = authService.register(request);
    return ResponseEntity.status(201).body(user);
  }

  @PostMapping("/login")
  public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
    LoginResponse response = authService.login(request.email(), request.password());
    return ResponseEntity.ok(response);
  }

  @PostMapping("/refresh")
  public ResponseEntity<LoginResponse> refresh(@Valid @RequestBody RefreshTokenRequest body) {

    LoginResponse response = authService.refresh(body.refreshToken());
    return ResponseEntity.ok(response);
  }

  @PostMapping("/logout")
  public ResponseEntity<Void> logout(@Valid @RequestBody LogoutRequest body) {

    authService.logout(body.refreshToken());
    return ResponseEntity.noContent().build();
  }
}
