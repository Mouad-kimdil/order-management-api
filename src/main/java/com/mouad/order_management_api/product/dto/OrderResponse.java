package com.mouad.order_management_api.product.dto;

import com.mouad.order_management_api.product.model.OrderStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record OrderResponse(
        UUID id,
        OrderStatus status,
        LocalDateTime createdAt,
        UUID ownerId,
        String ownerUsername,
        List<OrderItemResponse> items
) {
}