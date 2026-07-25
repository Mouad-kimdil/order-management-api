package com.mouad.order_management_api.product.service;

import java.util.List;
import java.util.UUID;

import com.mouad.order_management_api.common.exception.CategoryNotFoundException;
import com.mouad.order_management_api.product.dto.*;
import com.mouad.order_management_api.product.model.Category;
import com.mouad.order_management_api.product.repository.CategoryRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.mouad.order_management_api.common.exception.ConflictException;
import com.mouad.order_management_api.common.exception.ProductNotFoundException;
import com.mouad.order_management_api.product.model.Product;
import com.mouad.order_management_api.product.model.ProductStatus;
import com.mouad.order_management_api.product.repository.ProductRepository;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProductServiceImpl implements ProductService {
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    public ProductServiceImpl(ProductRepository productRepository, CategoryRepository categoryRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
    }

    private Product findProductByIdOrElseThrow(UUID id) {
        return productRepository.findById(id)
            .orElseThrow(() -> new ProductNotFoundException(id));
    }

    private ProductResponse toResponse(Product product) {
        Category category = product.getCategory();
        CategoryResponse categoryResponse = new CategoryResponse(
                category.getId(),
                category.getName(),
                category.getDescription()
        );

        return new ProductResponse(
            product.getId(),
            product.getSku(),
            product.getName(),
            product.getDescription(),
            product.getPrice(),
            product.getQuantityInStock(),
            categoryResponse,
            product.getStatus()
        );
    }

    @Transactional
    public ProductResponse create(CreateProductRequest request) {

        if (productRepository.existsBySku(request.sku())) {
            throw new ConflictException("Product with sku " + request.sku() + " already exists");
        }
        if (productRepository.existsByName(request.name())) {
            throw new ConflictException("Product with name " + request.name() + " already exists");
        }

        Category category = categoryRepository.findByName(request.categoryName())
                .orElseThrow(() -> new CategoryNotFoundException("Category: " + request.categoryName() + " was not found"));

        Product product = new Product(
            request.sku(),
            request.name(),
            request.description(),
            request.price(),
            request.quantityInStock(),
            category,
            request.status()
        );
        productRepository.save(product);
        return toResponse(product);
    }

    @Transactional
    public CategoryResponse createCategory(CreateCategoryRequest request) {
        if (categoryRepository.existsByName(request.name())) {
            throw new ConflictException("category already exists");
        }

        Category category = new Category(request.name(), request.description());

        categoryRepository.save(category);
        return new CategoryResponse(category.getId(), category.getName(), category.getDescription());
    }

    @Transactional(readOnly = true)
    public List<CategoryResponse> getAllCategories() {
        return categoryRepository.findAll()
                .stream()
                .map(category -> new CategoryResponse(
                        category.getId(),
                        category.getName(),
                        category.getDescription()
                ))
                .toList();
    }

    @Transactional(readOnly = true)
    public Page<ProductResponse> getAll(Pageable pageable) {
        return productRepository.findAllWithCategory(pageable)
            .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<ProductSummary> getAllProductsSummary(Pageable pageable) {
        return productRepository.findAllSummaries(pageable);
    }

    @Transactional(readOnly = true)
    public ProductResponse getBySku(String sku) {
        Product product = productRepository.findProductBySku(sku);
        if (product == null) {
            throw new ProductNotFoundException("Product with sku " + sku + " was not found");
        }
        return toResponse(product);
    }

    @Transactional(readOnly = true)
    public ProductResponse getById(UUID id) {
        Product product = findProductByIdOrElseThrow(id);

        return toResponse(product);
    }

    @Transactional
    public ProductResponse update(UUID id, UpdateProductRequest request) {
        Product product = findProductByIdOrElseThrow(id);

        if (productRepository.existsBySkuAndIdNot(request.sku(), id)) {
            throw new ConflictException("Another product already uses sku " + request.sku());
        }
        if (productRepository.existsByNameAndIdNot(request.name(), id)) {
            throw new ConflictException("Another product already uses name " + request.name());
        }

        Category category = categoryRepository.findByName(request.categoryName())
                        .orElseThrow(() -> new CategoryNotFoundException("category: " + request.categoryName() + " was not found"));

        product.setSku(request.sku());
        product.setName(request.name());
        product.setDescription(request.description());
        product.setPrice(request.price());
        product.setQuantityInStock(request.quantityInStock());
        product.setCategory(category);
        product.setStatus(request.status());

        productRepository.save(product);

        return toResponse(product);
    }

    @Transactional
    public void delete(UUID id) {
        Product product = findProductByIdOrElseThrow(id);

        if (product.getStatus().equals(ProductStatus.RESERVED)) {
            throw new ConflictException("Cannot delete product " + id + " because status is RESERVED");
        }

        productRepository.delete(product);
    }

    @Transactional
    public void deleteCategory(UUID id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new CategoryNotFoundException("Category: " + id + " was not found"));
        if (productRepository.existsByCategoryId(id)) {
            throw new ConflictException("Cannot delete category " + id + " because it still has products assigned to it");
        }
        categoryRepository.delete(category);
    }

    @Transactional(readOnly = true)
    public CategoryDetailResponse getCategoryWithProducts(UUID id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new CategoryNotFoundException("Category: " + id + " was not found"));

        List<ProductResponse> products = productRepository.findByCategoryId(id)
                .stream()
                .map(this::toResponse)
                .toList();
        return new CategoryDetailResponse(id, category.getName(), category.getDescription(), products);
    }
}
