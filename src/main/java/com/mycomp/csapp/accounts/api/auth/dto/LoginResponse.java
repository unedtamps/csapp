package com.mycomp.csapp.accounts.api.auth.dto;

import lombok.Builder;

@Builder
public record LoginResponse(String accessToken, String refreshToken) {}
