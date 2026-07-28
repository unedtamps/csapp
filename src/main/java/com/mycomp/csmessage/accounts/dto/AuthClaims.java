package com.mycomp.csmessage.accounts.dto;

import com.mycomp.csmessage.accounts.models.Role;

import lombok.Builder;

import org.springframework.security.core.AuthenticatedPrincipal;

@Builder
public record AuthClaims(String id, String email, Role role) implements AuthenticatedPrincipal {
  @Override
  public String getName() {
    return email;
  }
}
