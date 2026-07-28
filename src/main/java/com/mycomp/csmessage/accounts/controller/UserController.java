package com.mycomp.csmessage.accounts.controller;

import com.mycomp.csmessage.accounts.dto.AuthClaims;
import com.mycomp.csmessage.accounts.dto.UserDetailsRequest;
import com.mycomp.csmessage.accounts.dto.UserResponse;
import com.mycomp.csmessage.accounts.models.User;
import com.mycomp.csmessage.accounts.service.UserService;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/user")
public class UserController {

  private final UserService userService;

  @PostMapping("/details")
  public ResponseEntity<UserResponse> updateUserDetails(
      @AuthenticationPrincipal AuthClaims currentUser,
      @Valid @RequestBody UserDetailsRequest request) {

    User updated = userService.updateUserDetails(currentUser.id(), request);
    return ResponseEntity.ok(toResponse(updated));
  }

  private UserResponse toResponse(User user) {
    return new UserResponse(
        user.getId(), user.getUsername(), user.getEmail(), user.getRole(), user.getDetails());
  }
}
