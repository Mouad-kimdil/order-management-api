package com.mouad.order_management_api.common.exception;

import java.util.UUID;

public class ProductNotFoundException extends RuntimeException {
    public ProductNotFoundException(UUID id) {
        super("product with id " + id + " was not found");
    }

    public ProductNotFoundException(String message) {
        super(message);
    }
}