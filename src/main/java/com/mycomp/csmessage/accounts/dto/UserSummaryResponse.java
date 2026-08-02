package com.mycomp.csmessage.accounts.dto;

import com.mycomp.csmessage.accounts.models.Role;

public record UserSummaryResponse(String id, String username, String email, Role role) {}
