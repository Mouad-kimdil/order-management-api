package com.mouad.order_management_api.common.exception;

import java.util.UUID;

public class InsufficientStockException extends RuntimeException {
    public InsufficientStockException(UUID productId, int quantity) {
        super("Not enough stock for product " + productId + " Requested: " + quantity);
    }
}