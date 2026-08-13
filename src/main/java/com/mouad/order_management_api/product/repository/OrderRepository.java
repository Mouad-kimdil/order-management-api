package com.mouad.order_management_api.product.repository;

import com.mouad.order_management_api.auth.model.User;
import com.mouad.order_management_api.product.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrderRepository extends JpaRepository<Order, UUID> {
    List<Order> findByOwner(User owner);

    Optional<Order> findByIdAndOwner(UUID id, User owner);

    @Query("SELECT o FROM Order o JOIN FETCH o.owner WHERE o.id = :id")
    Optional<Order> findWithOwnerId(UUID id);
}