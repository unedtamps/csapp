package com.mycomp.csapp.accounts.dto;

import com.mycomp.csapp.accounts.models.Role;
import com.mycomp.csapp.accounts.models.UserDetails;

public record UserResponse(
    String id,
    String username,
    String email,
    Role role,
    UserDetails details) {}
