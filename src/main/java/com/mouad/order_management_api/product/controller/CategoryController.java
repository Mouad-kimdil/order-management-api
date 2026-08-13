package com.mouad.order_management_api.product.controller;

import com.mouad.order_management_api.product.dto.CategoryDetailResponse;
import com.mouad.order_management_api.product.dto.CategoryResponse;
import com.mouad.order_management_api.product.dto.CreateCategoryRequest;
import com.mouad.order_management_api.product.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/category")
public class CategoryController {
    private final ProductService productService;

    public CategoryController(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CategoryResponse> createCategory(@RequestBody @Valid CreateCategoryRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(productService.createCategory(request));
    }

    @GetMapping
    public List<CategoryResponse> getAllCategories() {
        return productService.getAllCategories();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteCategory(@PathVariable UUID id) {
        productService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<CategoryDetailResponse> getCategoryWithProducts(@PathVariable UUID id) {
        return ResponseEntity.ok(productService.getCategoryWithProducts(id));
    }
}