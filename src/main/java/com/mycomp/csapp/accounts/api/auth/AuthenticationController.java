package com.mycomp.csapp.accounts.api.auth;

import com.mycomp.csapp.accounts.api.auth.dto.LoginRequest;
import com.mycomp.csapp.accounts.api.auth.dto.LoginResponse;
import com.mycomp.csapp.accounts.api.auth.dto.LogoutRequest;
import com.mycomp.csapp.accounts.api.auth.dto.RefreshTokenRequest;
import com.mycomp.csapp.accounts.api.auth.dto.RegisterRequest;
import com.mycomp.csapp.accounts.application.AuthenticationService;
import com.mycomp.csapp.accounts.persistence.jpa.entity.UserEntity;

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
public class AuthenticationController {

  private final AuthenticationService authenticationService;

  @PostMapping("/register")
  public ResponseEntity<UserEntity> register(@Valid @RequestBody RegisterRequest request) {
    UserEntity user = authenticationService.register(request);
    return ResponseEntity.status(201).body(user);
  }

  @PostMapping("/login")
  public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
    LoginResponse response = authenticationService.login(request.email(), request.password());
    return ResponseEntity.ok(response);
  }

  @PostMapping("/refresh")
  public ResponseEntity<LoginResponse> refresh(@Valid @RequestBody RefreshTokenRequest body) {

    LoginResponse response = authenticationService.refresh(body.refreshToken());
    return ResponseEntity.ok(response);
  }

  @PostMapping("/logout")
  public ResponseEntity<Void> logout(@Valid @RequestBody LogoutRequest body) {

    authenticationService.logout(body.refreshToken());
    return ResponseEntity.noContent().build();
  }
}
