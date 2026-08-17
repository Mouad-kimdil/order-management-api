package com.mouad.order_management_api.common.exception;

import com.mouad.order_management_api.product.model.OrderStatus;

public class IllegalTransitionException extends RuntimeException {
    public IllegalTransitionException(OrderStatus from, OrderStatus to) {
        super("Illegal order status transition from " + from + " to " + to);
    }
}
