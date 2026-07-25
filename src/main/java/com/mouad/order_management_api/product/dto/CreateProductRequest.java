package com.mouad.order_management_api.product.dto;

import java.math.BigDecimal;

import com.mouad.order_management_api.product.model.ProductStatus;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CreateProductRequest(
    @NotBlank(message = "sku is required")
    String sku,

    @NotBlank(message = "name is required")
    String name,

    String description,

    @NotNull(message = "price is required")
    @Positive(message = "price must be greater than 0")
    BigDecimal price,

    @NotNull(message = "quantityInStock is required")
    @Min(value = 0, message = "quantityInStock must be greater than or equal to 0")
    Integer quantityInStock,

    @NotBlank(message = "category name is required")
    String categoryName,

    @NotNull(message = "status is required")
    ProductStatus status

) {
}