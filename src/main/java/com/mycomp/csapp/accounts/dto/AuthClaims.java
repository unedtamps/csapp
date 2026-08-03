package com.mycomp.csapp.accounts.dto;

import com.mycomp.csapp.accounts.models.Role;

import lombok.Builder;

import org.springframework.security.core.AuthenticatedPrincipal;

@Builder
public record AuthClaims(String id, String email, Role role) implements AuthenticatedPrincipal {
  @Override
  public String getName() {
    return email;
  }
}
