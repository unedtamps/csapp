package com.mycomp.csapp.accounts.api.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
    @NotBlank(message = "Username is required")
        @Size(min = 3, max = 20, message = "Username must be between 3 and 20 characters")
        String username,
    @Email(message = "Invalid email address") @NotBlank String email,
    @NotBlank @Size(min = 8, max = 40, message = "Password must be between 8 and 40 characters")
        String password) {}
