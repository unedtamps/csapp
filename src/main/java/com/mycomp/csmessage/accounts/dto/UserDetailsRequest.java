package com.mycomp.csmessage.accounts.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserDetailsRequest(
    @NotBlank(message = "Address is required") String address,
    @NotBlank(message = "Phone number is required")
        @Size(min = 10, max = 13, message = "Invalid phone number")
        String phoneNumber,
    @NotBlank(message = "Identification number is required") String identificationNumber,
    String dateOfBirth,
    String motherName) {}
