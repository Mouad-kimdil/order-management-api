package com.mouad.order_management_api.product.service;

import com.mouad.order_management_api.product.dto.CreateOrderRequest;
import com.mouad.order_management_api.product.dto.OrderResponse;
import org.springframework.http.ResponseEntity;

import org.springframework.security.access.AccessDeniedException;
import java.util.List;
import java.util.UUID;

public interface OrderService {
    public OrderResponse create(CreateOrderRequest request);
    public List<OrderResponse> getMyOrders();
    public OrderResponse getOwnedOrder(UUID id) throws AccessDeniedException;
}