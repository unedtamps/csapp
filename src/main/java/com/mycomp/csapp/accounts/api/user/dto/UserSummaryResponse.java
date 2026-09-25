package com.mycomp.csapp.accounts.api.user.dto;

import com.mycomp.csapp.accounts.domain.Role;

public record UserSummaryResponse(String id, String username, String email, Role role) {}
