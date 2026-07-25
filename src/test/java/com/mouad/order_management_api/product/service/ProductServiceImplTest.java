package com.mouad.order_management_api.product.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.mouad.order_management_api.common.exception.CategoryNotFoundException;
import com.mouad.order_management_api.common.exception.ConflictException;
import com.mouad.order_management_api.common.exception.ProductNotFoundException;
import com.mouad.order_management_api.product.dto.CategoryDetailResponse;
import com.mouad.order_management_api.product.dto.CategoryResponse;
import com.mouad.order_management_api.product.dto.CreateCategoryRequest;
import com.mouad.order_management_api.product.dto.CreateProductRequest;
import com.mouad.order_management_api.product.dto.ProductResponse;
import com.mouad.order_management_api.product.dto.ProductSummary;
import com.mouad.order_management_api.product.dto.UpdateProductRequest;
import com.mouad.order_management_api.product.model.Category;
import com.mouad.order_management_api.product.model.Product;
import com.mouad.order_management_api.product.model.ProductStatus;
import com.mouad.order_management_api.product.repository.CategoryRepository;
import com.mouad.order_management_api.product.repository.ProductRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class ProductServiceImplTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private ProductServiceImpl productService;

    private Product product;
    private CreateProductRequest createRequest;
    private UpdateProductRequest updateRequest;
    private Category electronicsCategory;

    @BeforeEach
    void setUp() {
        electronicsCategory = new Category("Electronics", "Electronic items");

        product = new Product(
           "SKU-100",
            "Mechanical Keyboard",
            "Hot-swappable keyboard",
            BigDecimal.valueOf(79.99),
            25,
            electronicsCategory,
            ProductStatus.ACTIVE
        );

        createRequest = new CreateProductRequest(
            "SKU-100",
            "Mechanical Keyboard",
            "Hot-swappable keyboard",
            BigDecimal.valueOf(79.99),
            25,
            "Electronics",
            ProductStatus.ACTIVE
        );

        updateRequest = new UpdateProductRequest(
            "SKU-999",
            "Mechanical Keyboard Pro",
            "Updated keyboard",
            BigDecimal.valueOf(99.99),
            30,
            "Electronics",
            ProductStatus.ACTIVE
        );
    }

    @Test
    void create_shouldThrowConflictException_whenSkuAlreadyExists() {

        when(productRepository.existsBySku("SKU-100")).thenReturn(true);

        assertThatThrownBy(() -> productService.create(createRequest))
            .isInstanceOf(ConflictException.class)
            .hasMessageContaining("SKU-100");

        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    void create_shouldThrowConflictException_whenNameAlreadyExists() {

        when(productRepository.existsBySku("SKU-100")).thenReturn(false);
        when(productRepository.existsByName("Mechanical Keyboard")).thenReturn(
            true
        );

        assertThatThrownBy(() -> productService.create(createRequest))
            .isInstanceOf(ConflictException.class)
            .hasMessageContaining("Mechanical Keyboard");

        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    void create_shouldSaveProductAndReturnResponse_whenSkuAndNameAreAvailable() {
        CreateProductRequest request = new CreateProductRequest(
            "SKU-300",
            "Wireless Mouse",
            "Ergonomic wireless mouse",
            BigDecimal.valueOf(29.99),
            50,
            "Electronics",
            ProductStatus.ACTIVE
        );

        when(productRepository.existsBySku("SKU-300")).thenReturn(false);
        when(productRepository.existsByName("Wireless Mouse")).thenReturn(
            false
        );
        when(categoryRepository.findByName("Electronics")).thenReturn(
            Optional.of(electronicsCategory)
        );

        ProductResponse response = productService.create(request);

        assertThat(response.sku()).isEqualTo("SKU-300");
        assertThat(response.name()).isEqualTo("Wireless Mouse");
        assertThat(response.description()).isEqualTo(
            "Ergonomic wireless mouse"
        );
        assertThat(response.price()).isEqualByComparingTo(
            BigDecimal.valueOf(29.99)
        );
        assertThat(response.quantityInStock()).isEqualTo(50);
        assertThat(response.category().name()).isEqualTo("Electronics");
        assertThat(response.status()).isEqualTo(ProductStatus.ACTIVE);

        verify(productRepository).save(any(Product.class));
    }

    @Test
    void delete_shouldThrowConflictException_whenProductIsReserved() {
        UUID productId = UUID.randomUUID();

        Product product = new Product(
            "SKU-400",
            "Gaming Monitor",
            "27-inch reserved monitor",
            BigDecimal.valueOf(249.99),
            10,
            electronicsCategory,
            ProductStatus.RESERVED
        );

        when(productRepository.findById(productId)).thenReturn(
            Optional.of(product)
        );

        assertThatThrownBy(() -> productService.delete(productId))
            .isInstanceOf(ConflictException.class)
            .hasMessageContaining("RESERVED");

        verify(productRepository, never()).delete(any(Product.class));
    }

    @Test
    void delete_shouldDeleteProduct_whenProductIsActive() {
        UUID productId = UUID.randomUUID();

        when(productRepository.findById(productId)).thenReturn(
            Optional.of(product)
        );

        productService.delete(productId);

        verify(productRepository).delete(product);
    }

    @Test
    void getById_shouldThrowResourceNotFoundException_whenProductDoesNotExist() {
        UUID productId = UUID.randomUUID();

        when(productRepository.findById(productId)).thenReturn(
            Optional.empty()
        );

        assertThatThrownBy(() -> productService.getById(productId))
            .isInstanceOf(ProductNotFoundException.class)
            .hasMessageContaining(productId.toString());
    }

    @Test
    void update_shouldThrowConflictException_whenSkuBelongsToAnotherProduct() {
        UUID productId = UUID.randomUUID();

        when(productRepository.findById(productId)).thenReturn(
            Optional.of(product)
        );

        when(
            productRepository.existsBySkuAndIdNot(updateRequest.sku(), productId)
        ).thenReturn(true);

        assertThatThrownBy(() -> productService.update(productId, updateRequest))
            .isInstanceOf(ConflictException.class)
            .hasMessageContaining(updateRequest.sku());

        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    void update_shouldThrowConflictException_whenNameBelongsToAnotherProduct() {
        UUID productId = UUID.randomUUID();

        when(productRepository.findById(productId)).thenReturn(
            Optional.of(product)
        );

        when(
            productRepository.existsBySkuAndIdNot(updateRequest.sku(), productId)
        ).thenReturn(false);

        when(
            productRepository.existsByNameAndIdNot(updateRequest.name(), productId)
        ).thenReturn(true);

        assertThatThrownBy(() -> productService.update(productId, updateRequest))
            .isInstanceOf(ConflictException.class)
            .hasMessageContaining(updateRequest.name());

        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    void update_shouldUpdateProduct_whenProductIsActiveAndIdNot() {
        UUID productId = UUID.randomUUID();

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(categoryRepository.findByName("Electronics")).thenReturn(
            Optional.of(electronicsCategory)
        );

        when(productRepository.existsBySkuAndIdNot(updateRequest.sku(), productId)).thenReturn(false);
        when(productRepository.existsByNameAndIdNot(updateRequest.name(), productId)).thenReturn(false);

        ProductResponse response = productService.update(productId, updateRequest);

        assertThat(response.sku()).isEqualTo("SKU-999");
        assertThat(response.name()).isEqualTo("Mechanical Keyboard Pro");
        assertThat(response.description()).isEqualTo("Updated keyboard");
        assertThat(response.price()).isEqualByComparingTo(BigDecimal.valueOf(99.99));
        assertThat(response.quantityInStock()).isEqualTo(30);
        assertThat(response.category().name()).isEqualTo("Electronics");
        assertThat(response.status()).isEqualTo(ProductStatus.ACTIVE);

        verify(productRepository).save(any(Product.class));
    }

    @Test
    void getBySku_shouldReturnProductResponse() {
        when(productRepository.findProductBySku("SKU-100")).thenReturn(product);

        ProductResponse response = productService.getBySku("SKU-100");

        assertThat(response.sku()).isEqualTo("SKU-100");
    }

    @Test
    void getBySku_shouldThrowProductNotFoundException() {
        when(productRepository.findProductBySku("UNKNOWN")).thenReturn(null);

        assertThatThrownBy(() -> productService.getBySku("UNKNOWN"))
            .isInstanceOf(ProductNotFoundException.class)
            .hasMessageContaining("UNKNOWN");
    }

    @Test
    void createCategory_shouldCreateAndReturnResponse() {
        CreateCategoryRequest request = new CreateCategoryRequest("Books", "All kinds of books");
        when(categoryRepository.existsByName("Books")).thenReturn(false);
        when(categoryRepository.save(any(Category.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CategoryResponse response = productService.createCategory(request);

        assertThat(response.name()).isEqualTo("Books");
        assertThat(response.description()).isEqualTo("All kinds of books");
    }

    @Test
    void createCategory_shouldThrowConflictException() {
        CreateCategoryRequest request = new CreateCategoryRequest("Books", "All kinds of books");
        when(categoryRepository.existsByName("Books")).thenReturn(true);

        assertThatThrownBy(() -> productService.createCategory(request))
            .isInstanceOf(ConflictException.class)
            .hasMessageContaining("already exists");
    }

    @Test
    void getAllCategories_shouldReturnList() {
        when(categoryRepository.findAll()).thenReturn(List.of(electronicsCategory));

        List<CategoryResponse> categories = productService.getAllCategories();

        assertThat(categories).hasSize(1);
        assertThat(categories.getFirst().name()).isEqualTo("Electronics");
    }

    @Test
    void deleteCategory_shouldDelete() {
        UUID categoryId = UUID.randomUUID();
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(electronicsCategory));
        when(productRepository.existsByCategoryId(categoryId)).thenReturn(false);

        productService.deleteCategory(categoryId);

        verify(categoryRepository).delete(electronicsCategory);
    }

    @Test
    void deleteCategory_shouldThrowCategoryNotFoundException() {
        UUID categoryId = UUID.randomUUID();
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.deleteCategory(categoryId))
            .isInstanceOf(CategoryNotFoundException.class);
    }

    @Test
    void deleteCategory_shouldThrowConflictException() {
        UUID categoryId = UUID.randomUUID();
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(electronicsCategory));
        when(productRepository.existsByCategoryId(categoryId)).thenReturn(true);

        assertThatThrownBy(() -> productService.deleteCategory(categoryId))
            .isInstanceOf(ConflictException.class)
            .hasMessageContaining("products");
    }

    @Test
    void getCategoryWithProducts_shouldReturnDetail() {
        UUID categoryId = UUID.randomUUID();
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(electronicsCategory));
        when(productRepository.findByCategoryId(categoryId)).thenReturn(List.of(product));

        CategoryDetailResponse response = productService.getCategoryWithProducts(categoryId);

        assertThat(response.name()).isEqualTo("Electronics");
        assertThat(response.products()).hasSize(1);
        assertThat(response.products().getFirst().name()).isEqualTo("Mechanical Keyboard");
    }

    @Test
    void getAll_shouldReturnPageOfProducts() {
        Pageable pageable = Pageable.ofSize(20);
        Page<Product> productPage = new PageImpl<>(List.of(product));
        when(productRepository.findAllWithCategory(pageable)).thenReturn(productPage);

        Page<ProductResponse> result = productService.getAll(pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().getFirst().sku()).isEqualTo("SKU-100");
    }

    @Test
    void getAllProductsSummary_shouldReturnPage() {
        Pageable pageable = Pageable.ofSize(20);
        ProductSummary summary = new ProductSummary(UUID.randomUUID(), "Mechanical Keyboard", BigDecimal.valueOf(79.99), "Electronics");
        Page<ProductSummary> summaryPage = new PageImpl<>(List.of(summary));
        when(productRepository.findAllSummaries(pageable)).thenReturn(summaryPage);

        Page<ProductSummary> result = productService.getAllProductsSummary(pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().getFirst().productName()).isEqualTo("Mechanical Keyboard");
    }
}
