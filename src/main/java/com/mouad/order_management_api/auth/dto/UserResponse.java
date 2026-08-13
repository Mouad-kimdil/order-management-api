package com.mouad.order_management_api.auth.dto;

import com.mouad.order_management_api.auth.model.Role;
import com.mouad.order_management_api.auth.model.User;

import java.util.UUID;

public record UserResponse(
        UUID id,
        String email,
        String username,
        Role role
) {
    public static UserResponse from(User user) {
        return new UserResponse(user.getId(), user.getEmail(), user.getUsername(), user.getRole());
    }
}