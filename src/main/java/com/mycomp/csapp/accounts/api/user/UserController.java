package com.mycomp.csapp.accounts.api.user;

import com.mycomp.csapp.accounts.api.user.dto.UserDetailsRequest;
import com.mycomp.csapp.accounts.api.user.dto.UserResponse;
import com.mycomp.csapp.accounts.api.user.dto.UserSummaryResponse;
import com.mycomp.csapp.accounts.application.UserAccountService;
import com.mycomp.csapp.accounts.auth.principal.AuthenticatedUser;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/user")
public class UserController {

  private final UserAccountService userAccountService;

  @PostMapping("/details")
  public ResponseEntity<UserResponse> updateUserDetails(
      @AuthenticationPrincipal AuthenticatedUser currentUser,
      @Valid @RequestBody UserDetailsRequest request) {

    return ResponseEntity.ok(userAccountService.updateUserDetails(currentUser.id(), request));
  }

  @GetMapping("/find-by-email")
  public ResponseEntity<UserSummaryResponse> findByEmail(@RequestParam String email) {
    return ResponseEntity.ok(userAccountService.findByEmail(email));
  }
}
