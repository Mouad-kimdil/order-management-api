package com.mouad.order_management_api.product.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CreateOrderItemRequest(
        @NotNull
        UUID productId,

        @Min(1)
        int quantity
) {
}