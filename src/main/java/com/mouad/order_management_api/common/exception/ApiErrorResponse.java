package com.mouad.order_management_api.common.exception;

import java.time.Instant;

public class ApiErrorResponse {
    Instant timestamp;
    int status;
    String error;
    String message;
    String path;

    public ApiErrorResponse(Instant timestamp, int status, String error, String message, String path) {
        this.timestamp = timestamp;
        this.status = status;
        this.error = error;
        this.message = message;
        this.path = path;
    }

    public Instant getTimestamp() { return timestamp; }
    public int getStatus() { return status; }
    public String getError() { return error; }
    public String getMessage() { return message; }
    public String getPath() { return path; }
}