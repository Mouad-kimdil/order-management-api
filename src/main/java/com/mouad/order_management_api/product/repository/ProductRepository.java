package com.mouad.order_management_api.product.repository;

import java.util.List;
import java.util.UUID;

import com.mouad.order_management_api.product.dto.ProductSummary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.mouad.order_management_api.product.model.Product;
import org.springframework.data.jpa.repository.Query;

public interface ProductRepository extends JpaRepository<Product, UUID> {
    boolean existsBySku(String sku);
    boolean existsByName(String name);
    boolean existsBySkuAndIdNot(String sku, UUID id);
    boolean existsByNameAndIdNot(String name, UUID id);

    Product findProductBySku(String sku);

    @Query(
            value = "SELECT p FROM Product p JOIN FETCH p.category",
            countQuery = "SELECT COUNT(p) FROM Product p"
    )
    Page<Product> findAllWithCategory(Pageable pageable);

    @Query(
            value = "SELECT new com.mouad.order_management_api.product.dto.ProductSummary(" +
                    "p.id, p.name, p.price, c.name) FROM Product p JOIN p.category c",
            countQuery = "SELECT COUNT(p) FROM Product p"
    )
    Page<ProductSummary> findAllSummaries(Pageable pageable);

    boolean existsByCategoryId(UUID id);

    List<Product> findByCategoryId(UUID id);


}