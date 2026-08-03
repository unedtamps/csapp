package com.mycomp.csapp.accounts.dto;

import com.mycomp.csapp.accounts.models.Role;

public record UserSummaryResponse(String id, String username, String email, Role role) {}
