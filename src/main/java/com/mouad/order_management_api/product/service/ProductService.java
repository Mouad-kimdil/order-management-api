package com.mouad.order_management_api.product.service;

import java.util.List;
import java.util.UUID;

import com.mouad.order_management_api.product.dto.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductService {
    ProductResponse create(CreateProductRequest request);
    Page<ProductResponse> getAll(Pageable pageable);
    ProductResponse getById(UUID id);
    ProductResponse getBySku(String sku);
    ProductResponse update(UUID id, UpdateProductRequest request);
    void delete(UUID id);

    CategoryResponse createCategory(CreateCategoryRequest request);
    List<CategoryResponse> getAllCategories();

    void deleteCategory(UUID id);

    CategoryDetailResponse getCategoryWithProducts(UUID id);
    Page<ProductSummary> getAllProductsSummary(Pageable pageable);
}