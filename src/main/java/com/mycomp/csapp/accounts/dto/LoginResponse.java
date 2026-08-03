package com.mycomp.csapp.accounts.dto;

import lombok.Builder;

@Builder
public record LoginResponse(String accessToken, String refreshToken) {}
