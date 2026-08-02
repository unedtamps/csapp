package com.mycomp.csmessage.accounts.controller;

import com.mycomp.csmessage.accounts.dto.AuthClaims;
import com.mycomp.csmessage.accounts.dto.UserDetailsRequest;
import com.mycomp.csmessage.accounts.dto.UserResponse;
import com.mycomp.csmessage.accounts.dto.UserSummaryResponse;
import com.mycomp.csmessage.accounts.service.UserService;

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

  private final UserService userService;

  @PostMapping("/details")
  public ResponseEntity<UserResponse> updateUserDetails(
      @AuthenticationPrincipal AuthClaims currentUser,
      @Valid @RequestBody UserDetailsRequest request) {

    return ResponseEntity.ok(userService.updateUserDetails(currentUser.id(), request));
  }

  @GetMapping("/find-by-email")
  public ResponseEntity<UserSummaryResponse> findByEmail(@RequestParam String email) {
    return ResponseEntity.ok(userService.findByEmail(email));
  }
}
