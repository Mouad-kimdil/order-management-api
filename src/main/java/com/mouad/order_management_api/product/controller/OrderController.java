package com.mouad.order_management_api.product.controller;

import com.mouad.order_management_api.product.dto.OrderResponse;
import com.mouad.order_management_api.product.service.OrderService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.springframework.security.access.AccessDeniedException;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {
    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping
    public List<OrderResponse> getMyOrders() {
        return orderService.getMyOrders();
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getOrderById(@PathVariable UUID id) throws AccessDeniedException {
        return ResponseEntity.ok(orderService.getOwnedOrder(id));
    }

    @PostMapping
    public ResponseEntity<OrderResponse> create() {
        return ResponseEntity.status(HttpStatus.CREATED).body(orderService.create());
    }
}