package com.mycomp.csapp.accounts.api.user.dto;

import com.mycomp.csapp.accounts.domain.Role;
import com.mycomp.csapp.accounts.persistence.jpa.entity.UserDetailsEntity;

public record UserResponse(
    String id,
    String username,
    String email,
    Role role,
    UserDetailsEntity details) {}
