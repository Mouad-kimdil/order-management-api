package com.mouad.order_management_api.product.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record ProductSummary(
        UUID id,
        String productName,
        BigDecimal price,
        String categoryName
) {

}
