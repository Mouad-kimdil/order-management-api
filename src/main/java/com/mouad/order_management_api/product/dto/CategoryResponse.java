package com.mouad.order_management_api.product.dto;

import java.util.UUID;

public record CategoryResponse(
        UUID id,
        String name,
        String description
) {
}