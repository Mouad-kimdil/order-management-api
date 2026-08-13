package com.mouad.order_management_api.product.service;

import com.mouad.order_management_api.auth.model.Role;
import com.mouad.order_management_api.auth.model.User;
import com.mouad.order_management_api.auth.security.SecurityUser;
import com.mouad.order_management_api.common.exception.OrderNotFoundException;
import com.mouad.order_management_api.product.dto.OrderResponse;
import com.mouad.order_management_api.product.model.Order;
import com.mouad.order_management_api.product.model.OrderStatus;
import com.mouad.order_management_api.product.repository.OrderRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.security.access.AccessDeniedException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;

    public OrderServiceImpl(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    private OrderResponse toResponse(Order order) {
        return new OrderResponse(order.getId(),
                order.getStatus(), order.getCreatedAt(),
                order.getOwner().getId(),
                order.getOwner().getUsername());
    }

    private User currentUser() {
        SecurityUser principal = (SecurityUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return principal.getUser();
    }

    private boolean isAdmin() {
        return currentUser().getRole() == Role.ROLE_ADMIN;
    }

    @Override
    public OrderResponse create() {
        Order order = new Order(currentUser(), OrderStatus.PENDING, LocalDateTime.now());
        orderRepository.save(order);
        return toResponse(order);
    }

    @Override
    @Transactional
    public List<OrderResponse> getMyOrders() {
        if (isAdmin()) {
            return orderRepository.findAll()
                    .stream()
                    .map(this::toResponse)
                    .toList();
        }
        return orderRepository.findByOwner(currentUser())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public OrderResponse getOwnedOrder(UUID id) throws AccessDeniedException {
        if (isAdmin()) {
            return orderRepository.findWithOwnerId(id)
                    .map(this::toResponse)
                    .orElseThrow(OrderNotFoundException::new);
        }
        return orderRepository.findByIdAndOwner(id, currentUser())
                .map(this::toResponse)
                .orElseThrow(() -> new AccessDeniedException("Access denied"));
    }
}