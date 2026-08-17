package com.mouad.order_management_api.product.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record OrderItemResponse(
        UUID id,
        UUID productId,
        String sku,
        String name,
        BigDecimal unitPrice,
        int quantity
) {
}