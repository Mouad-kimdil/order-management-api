package com.mouad.order_management_api.product.service;

import com.mouad.order_management_api.auth.model.Role;
import com.mouad.order_management_api.auth.model.User;
import com.mouad.order_management_api.auth.security.SecurityUser;
import com.mouad.order_management_api.common.exception.InsufficientStockException;
import com.mouad.order_management_api.common.exception.OrderNotFoundException;
import com.mouad.order_management_api.common.exception.ProductNotFoundException;
import com.mouad.order_management_api.product.dto.CreateOrderItemRequest;
import com.mouad.order_management_api.product.dto.CreateOrderRequest;
import com.mouad.order_management_api.product.dto.OrderItemResponse;
import com.mouad.order_management_api.product.dto.OrderResponse;
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
import java.util.UUID;

@Service
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;

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


    // ============================================================
    // TODO — Task 1.1: Order Line Items (Feature 1)
    // Goal: An Order gets its contents (which products, qty, price).
    // Pattern: child table `order_item` (FK to orders) + SNAPSHOT of
    // sku/name/unitPrice at purchase time (product values change later).
    //
    // Top-down, discovery-driven: start somewhere, when you need a
    // dependency GO BUILD IT NOW, then come back and continue.
    //
    // ────────────────────────────────────────────────────────────
    // STEP 1 — OrderController.java (evolve the EXISTING endpoint)
    //   [ ] Change create() to: create(@RequestBody @Valid CreateOrderRequest request)
    //       -> I need a CreateOrderRequest class. GO BUILD IT NOW:
    //          CreateOrderRequest.java (record, product/dto):
    //            - @Valid @NotEmpty List<CreateOrderItemRequest> items
    //          -> I need CreateOrderItemRequest. GO BUILD IT NOW:
    //             CreateOrderItemRequest.java (record, product/dto):
    //               - @NotNull UUID productId
    //               - @Min(1) int quantity
    //       -> back in controller: call orderService.create(request)
    //
    // ────────────────────────────────────────────────────────────
    // STEP 2 — OrderService.java + OrderServiceImpl.java
    //   [ ] Change create() -> create(CreateOrderRequest request)
    //   [ ] Inside OrderServiceImpl.create(request):
    //       - current user, new Order(PENDING, now)
    //       - for EACH item in request.items()  (each element is
    //         a CreateOrderItemRequest holding productId + quantity):
    //           - productRepository.findById(item.productId())
    //             -> missing -> throw ProductNotFoundException
    //           - I need an OrderItem entity. GO BUILD IT NOW:
    //             OrderItem.java (new entity, product/model):
    //               - @Entity @Table(name = "order_item")
    //               - @Id @UuidGenerator(style = TIME) UUID id
    //               - @ManyToOne(fetch = LAZY) @JoinColumn(name = "order_id") Order order
    //               - @ManyToOne(fetch = LAZY) @JoinColumn(name = "product_id") Product product
    //               - snapshot fields:
    //                   @Column(nullable = false) String sku
    //                   @Column(nullable = false) String name
    //                   @Column(nullable = false) BigDecimal unitPrice
    //                   @Column Integer quantity
    //               - constructor (order, product, sku, name, unitPrice, quantity)
    //               - getters
    //           - I need a place to store items on Order. GO BUILD IT NOW:
    //             Order.java:
    //               - add @OneToMany(mappedBy = "order",
    //                   cascade = CascadeType.ALL, orphanRemoval = true)
    //                   private List<OrderItem> items = new ArrayList<>();
    //               - add getItems() getter
    //           - build OrderItem with product SNAPSHOT:
    //               sku = product.getSku(), name = product.getName(),
    //               unitPrice = product.getPrice(), quantity = item.quantity()
    //           - order.getItems().add(orderItem)
    //       - orderRepository.save(order)
    //       - I need to return items in the response. GO BUILD IT NOW:
    //           OrderItemResponse.java (record, product/dto):
    //             - UUID id
    //             - UUID productId
    //             - String sku          (snapshot)
    //             - String name         (snapshot)
    //             - BigDecimal unitPrice (snapshot)
    //             - int quantity
    //           OrderResponse.java — add one component:
    //             - List<OrderItemResponse> items
    //       -> back in impl: map order (incl. items) to OrderResponse
    //
    // ────────────────────────────────────────────────────────────
    // Verification:
    //   [ ] Compile, restart the application.
    //   [ ] POST /api/v1/orders with 2 items -> items in response, rows in DB.
    //   [ ] GET /orders/{id} -> items persisted.
    // ============================================================
}
