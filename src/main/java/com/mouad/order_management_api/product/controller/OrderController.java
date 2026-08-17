package com.mouad.order_management_api.product.controller;

import com.mouad.order_management_api.product.dto.CreateOrderRequest;
import com.mouad.order_management_api.product.dto.OrderResponse;
import com.mouad.order_management_api.product.dto.UpdateOrderStatusRequest;
import com.mouad.order_management_api.product.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
    public ResponseEntity<OrderResponse> create(@RequestBody @Valid CreateOrderRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(orderService.create(request));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}/status")
    public ResponseEntity<OrderResponse> updateStatus(@PathVariable UUID id, @RequestBody @Valid UpdateOrderStatusRequest request) {
        return ResponseEntity.status(HttpStatus.OK).body(orderService.updateStatus(id, request));
    }
}