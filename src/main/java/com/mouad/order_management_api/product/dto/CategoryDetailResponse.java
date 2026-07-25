package com.mouad.order_management_api.product.dto;

import java.util.List;
import java.util.UUID;

public record CategoryDetailResponse(
        UUID id,
        String name,
        String description,
        List<ProductResponse> products
) {
}