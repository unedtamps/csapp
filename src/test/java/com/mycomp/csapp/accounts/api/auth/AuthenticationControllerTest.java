package com.mycomp.csapp.accounts.api.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.mycomp.csapp.accounts.api.auth.dto.LoginRequest;
import com.mycomp.csapp.accounts.api.auth.dto.LoginResponse;
import com.mycomp.csapp.accounts.api.auth.dto.LogoutRequest;
import com.mycomp.csapp.accounts.api.auth.dto.RegisterRequest;
import com.mycomp.csapp.accounts.application.AuthenticationService;
import com.mycomp.csapp.accounts.domain.Role;
import com.mycomp.csapp.accounts.persistence.jpa.entity.UserEntity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

@ExtendWith(MockitoExtension.class)
class AuthenticationControllerTest {

  @Mock private AuthenticationService authenticationService;

  private AuthenticationController authenticationController;

  @BeforeEach
  void setUp() {
    authenticationController = new AuthenticationController(authenticationService);
  }

  @Test
  void registerReturnsCreatedWithTheServiceUser() {
    RegisterRequest request = new RegisterRequest("alice", "alice@example.com", "password");
    UserEntity user =
        UserEntity.builder()
            .id("u1")
            .username("alice")
            .email("alice@example.com")
            .password("encoded-password")
            .role(Role.USER)
            .build();
    when(authenticationService.register(request)).thenReturn(user);

    var response = authenticationController.register(request);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    assertThat(response.getBody()).isSameAs(user);
  }

  @Test
  void loginReturnsTheServiceResponse() {
    LoginRequest request = new LoginRequest("alice@example.com", "password");
    LoginResponse loginResponse =
        LoginResponse.builder().accessToken("access").refreshToken("refresh").build();
    when(authenticationService.login(request.email(), request.password()))
        .thenReturn(loginResponse);

    var response = authenticationController.login(request);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isSameAs(loginResponse);
  }

  @Test
  void logoutReturnsNoContentAndDelegatesTheToken() {
    LogoutRequest request = new LogoutRequest("refresh-token");

    var response = authenticationController.logout(request);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    assertThat(response.getBody()).isNull();
    verify(authenticationService).logout("refresh-token");
  }
}
