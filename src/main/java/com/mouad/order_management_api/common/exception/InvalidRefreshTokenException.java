package com.mouad.order_management_api.common.exception;

public class InvalidRefreshTokenException extends RuntimeException {
    public InvalidRefreshTokenException() {
        super("Invalid or expired refresh token");
    }
}