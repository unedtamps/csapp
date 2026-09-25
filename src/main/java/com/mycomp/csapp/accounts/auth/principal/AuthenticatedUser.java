package com.mycomp.csapp.accounts.auth.principal;

import com.mycomp.csapp.accounts.domain.Role;

import lombok.Builder;

import org.springframework.security.core.AuthenticatedPrincipal;

@Builder
public record AuthenticatedUser(String id, String email, Role role)
    implements AuthenticatedPrincipal {
  @Override
  public String getName() {
    return email;
  }
}
