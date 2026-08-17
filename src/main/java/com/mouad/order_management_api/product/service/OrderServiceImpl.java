package com.mouad.order_management_api.product.service;

import com.mouad.order_management_api.auth.model.Role;
import com.mouad.order_management_api.auth.model.User;
import com.mouad.order_management_api.auth.security.SecurityUser;
import com.mouad.order_management_api.common.exception.IllegalTransitionException;
import com.mouad.order_management_api.common.exception.InsufficientStockException;
import com.mouad.order_management_api.common.exception.OrderNotFoundException;
import com.mouad.order_management_api.common.exception.ProductNotFoundException;
import com.mouad.order_management_api.product.dto.*;
import com.mouad.order_management_api.product.model.Order;
import com.mouad.order_management_api.product.model.OrderItem;
import com.mouad.order_management_api.product.model.OrderStatus;
import com.mouad.order_management_api.product.model.Product;
import com.mouad.order_management_api.product.repository.OrderRepository;
import com.mouad.order_management_api.product.repository.ProductRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.security.access.AccessDeniedException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final Map<OrderStatus, Set<OrderStatus>> legal = Map.of(
            OrderStatus.PENDING, Set.of(OrderStatus.CONFIRMED, OrderStatus.CANCELLED),
            OrderStatus.CONFIRMED, Set.of(OrderStatus.SHIPPED, OrderStatus.CANCELLED),
            OrderStatus.SHIPPED, Set.of(OrderStatus.DELIVERED),
            OrderStatus.DELIVERED, Set.of()
    );

    public OrderServiceImpl(OrderRepository orderRepository, ProductRepository productRepository) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
    }

    private OrderResponse toResponse(Order order) {
        List<OrderItemResponse> items = order.getItems()
                .stream().map(item -> new OrderItemResponse(
                            item.getId(),
                            item.getProduct().getId(),
                            item.getSku(),
                            item.getName(),
                            item.getUnitPrice(),
                            item.getQuantity()
                    )
                ).toList();
        return new OrderResponse(order.getId(),
                order.getStatus(), order.getCreatedAt(),
                order.getOwner().getId(),
                order.getOwner().getUsername(),
                items
        );
    }

    private User currentUser() {
        SecurityUser principal = (SecurityUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return principal.getUser();
    }

    private boolean isAdmin() {
        return currentUser().getRole() == Role.ROLE_ADMIN;
    }

    @Override
    @Transactional
    public OrderResponse create(CreateOrderRequest request) {
        Order order = new Order(currentUser(), OrderStatus.PENDING, LocalDateTime.now());

        for (CreateOrderItemRequest item : request.items()) {
            Product product = productRepository.findById(item.productId())
                    .orElseThrow(() -> new ProductNotFoundException(item.productId()));

            if (product.getQuantityInStock() < item.quantity()) {
                throw new InsufficientStockException(product.getId(), item.quantity());
            }

            product.setQuantityInStock(product.getQuantityInStock() - item.quantity());

            OrderItem orderItem = new OrderItem(
                    order, product, product.getSku(),
                    product.getName(), product.getPrice(),
                    item.quantity()
            );
            order.getItems().add(orderItem);
        }
        orderRepository.save(order);
        return toResponse(order);
    }

    private boolean isTransitionAllowed(OrderStatus from, OrderStatus to) {
        return legal.get(from).contains(to);
    }

    @Override
    @Transactional
    public OrderResponse updateStatus(UUID id, UpdateOrderStatusRequest request) {
        Order order = orderRepository.findWithOwnerId(id)
                .orElseThrow(OrderNotFoundException::new);
        OrderStatus status = order.getStatus();

        if (!isTransitionAllowed(status, request.status())) {
            throw new IllegalTransitionException(status, request.status());
        }
        order.setStatus(request.status());
        if (request.status().equals(OrderStatus.CANCELLED)) {
            for (OrderItem item : order.getItems()) {
                Product product = productRepository.findById(item.getProduct().getId())
                        .orElseThrow(() -> new ProductNotFoundException(item.getProduct().getId()));
                product.setQuantityInStock(
                        product.getQuantityInStock() + item.getQuantity()
                );
            }
        }
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
