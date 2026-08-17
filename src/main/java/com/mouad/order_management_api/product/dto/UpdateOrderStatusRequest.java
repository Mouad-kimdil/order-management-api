package com.mouad.order_management_api.product.dto;

import com.mouad.order_management_api.product.model.OrderStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateOrderStatusRequest(
        @NotNull
        OrderStatus status
) {
}