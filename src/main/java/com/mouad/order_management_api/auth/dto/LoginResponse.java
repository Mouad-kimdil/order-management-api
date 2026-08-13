package com.mouad.order_management_api.auth.dto;

public record LoginResponse(
        String accessToken,
        String refreshToken,
        UserResponse user
) {
}