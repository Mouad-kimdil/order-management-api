package com.mouad.order_management_api.product.dto;

import java.math.BigDecimal;
import java.util.UUID;

import com.mouad.order_management_api.product.model.ProductStatus;

public record ProductResponse(
    UUID id,
    String sku,
    String name,
    String description,
    BigDecimal price,
    Integer quantityInStock,
    CategoryResponse category,
    ProductStatus status
) {
}