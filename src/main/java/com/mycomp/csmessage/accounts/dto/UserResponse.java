package com.mycomp.csmessage.accounts.dto;

import com.mycomp.csmessage.accounts.models.Role;
import com.mycomp.csmessage.accounts.models.UserDetails;

public record UserResponse(
    String id,
    String username,
    String email,
    Role role,
    UserDetails details) {}
