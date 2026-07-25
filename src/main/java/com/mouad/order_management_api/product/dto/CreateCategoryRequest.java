package com.mouad.order_management_api.product.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateCategoryRequest(
        @NotBlank(message = "name is required")
        String name,

        String description
) {
}