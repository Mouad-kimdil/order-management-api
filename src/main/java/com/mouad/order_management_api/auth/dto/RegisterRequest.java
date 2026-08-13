package com.mouad.order_management_api.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank(message = "Email is required")
        @Email(message = "Email not valid")
        String email,

        @NotBlank(message = "Username is required")
        @Size(min = 3, max = 50, message = "username must be 3-50 character")
        String username,

        @NotBlank(message = "Password is required")
        @Size(min = 8, max = 72, message = "Password must be 8-72 character")
        String password
) {}

