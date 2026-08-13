package com.mouad.order_management_api.product.repository;

import com.mouad.order_management_api.product.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CategoryRepository extends JpaRepository<Category, UUID> {
    Optional<Category> findByName(String name);

    boolean existsByName(String name);

    @Query("SELECT DISTINCT c FROM Category c LEFT JOIN FETCH c.products")
    List<Category> findAllWithProducts();
}
