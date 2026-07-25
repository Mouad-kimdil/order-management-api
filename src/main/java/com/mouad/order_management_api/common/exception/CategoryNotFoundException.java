package com.mouad.order_management_api.common.exception;

public class CategoryNotFoundException extends RuntimeException {
    public CategoryNotFoundException(String exception) {
        super(exception);
    }
}