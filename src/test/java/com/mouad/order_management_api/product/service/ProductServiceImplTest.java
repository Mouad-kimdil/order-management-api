package com.mouad.order_management_api.product.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.mouad.order_management_api.common.exception.CategoryNotFoundException;
import com.mouad.order_management_api.common.exception.ConflictException;
import com.mouad.order_management_api.common.exception.ProductNotFoundException;
import com.mouad.order_management_api.product.dto.*;
import com.mouad.order_management_api.product.model.Category;
import com.mouad.order_management_api.product.model.Product;
import com.mouad.order_management_api.product.model.ProductStatus;
import com.mouad.order_management_api.product.repository.CategoryRepository;
import com.mouad.order_management_api.product.repository.ProductRepository;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;

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
    void getBySku_shouldReturnProduct_whenProductExists() {
        when(productRepository.findProductBySku("SKU-100")).thenReturn(Optional.of(product));

        ProductResponse response = productService.getBySku("SKU-100");

        assertThat(response.sku()).isEqualTo("SKU-100");
        assertThat(response.name()).isEqualTo("Mechanical Keyboard");
    }

    @Test
    void getBySku_ShouldThrowProductNotFoundException_WhenProductDoesNotExists() {
        when(productRepository.findProductBySku("SKU-NOPE")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.getBySku("SKU-NOPE"))
                .isInstanceOf(ProductNotFoundException.class)
                .hasMessageContaining("SKU-NOPE");
    }

    @Test
    void getById_shouldReturnProduct_whenProductExists() {
        UUID productId = UUID.randomUUID();

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));

        ProductResponse response = productService.getById(productId);

        assertThat(response.sku()).isEqualTo("SKU-100");
        assertThat(response.name()).isEqualTo("Mechanical Keyboard");
    }

    @Test
    void update_shouldThrowProductNotFoundException_whenProductDoesNotExists() {
        UUID id = UUID.randomUUID();

        when(productRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.update(id, updateRequest))
                .isInstanceOf(ProductNotFoundException.class)
                .hasMessageContaining(String.valueOf(id));
    }

    @Test
    void update_shouldThrowCategoryNotFoundException_whenCategoryDoesNotExists() {
        UUID id = UUID.randomUUID();

        when(productRepository.findById(id)).thenReturn(Optional.of(product));

        when(productRepository.existsBySkuAndIdNot(updateRequest.sku(), id)).thenReturn(false);
        when(productRepository.existsByNameAndIdNot(updateRequest.name(), id)).thenReturn(false);

        when(categoryRepository.findByName("Electronics")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.update(id, updateRequest))
                .isInstanceOf(CategoryNotFoundException.class)
                .hasMessageContaining("Electronics");
    }

    @Test
    void delete_shouldThrowProductNotFoundException_whenProductDoesNotExists() {
        UUID id = UUID.randomUUID();

        when(productRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.delete(id))
                .isInstanceOf(ProductNotFoundException.class)
                .hasMessageContaining(String.valueOf(id));

        verify(productRepository, never()).delete(any(Product.class));
    }

    @Test
    void createCategory_shouldThrowConflictException_whenCategoryExists() {
        CreateCategoryRequest request = new CreateCategoryRequest("ELECTRONICS", "Electronic items");
        when(categoryRepository.existsByName(request.name())).thenReturn(true);

        assertThatThrownBy(() -> productService.createCategory(request))
                .isInstanceOf(ConflictException.class);

        verify(categoryRepository, never()).save(any(Category.class));
    }

    @Test
    void createCategory_shouldCreateCategory_whenCategoryDoesNotExists() {
        CreateCategoryRequest request = new CreateCategoryRequest("Software", "Software products");

        when(categoryRepository.existsByName(request.name())).thenReturn(false);
        when(categoryRepository.save(any(Category.class))).thenAnswer(
                invocation -> {
                    Category category = invocation.getArgument(0);
                    Field idField = Category.class.getDeclaredField("id");
                    idField.setAccessible(true);
                    idField.set(category, UUID.randomUUID());
                    return category;
                }
        );

        CategoryResponse response = productService.createCategory(request);

        assertThat(response.id()).isNotNull();
        assertThat(response.name()).isEqualTo(request.name());
        assertThat(response.description()).isEqualTo(request.description());

        verify(categoryRepository).save(any(Category.class));
    }

    @Test
    void getAllCategories_shouldReturnListOfCategories_whenCategoriesExist() {
        Category electronics = mock(Category.class);
        when(electronics.getId()).thenReturn(UUID.randomUUID());
        when(electronics.getName()).thenReturn("Electronics");
        when(electronics.getDescription()).thenReturn("Electronic items");

        Category book = mock(Category.class);
        when(book.getId()).thenReturn(UUID.randomUUID());
        when(book.getName()).thenReturn("Books");
        when(book.getDescription()).thenReturn("Reading materials");

        when(categoryRepository.findAll()).thenReturn(List.of(electronics, book));

        List<CategoryResponse> response = productService.getAllCategories();

        assertThat(response.size()).isEqualTo(2);
    }

    @Test
    void deleteCategory_ShouldThrowCategoryNotFoundException_whenCategoryDoesNotExist() {
        UUID categoryId = UUID.randomUUID();

        when(categoryRepository.findById(categoryId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.deleteCategory(categoryId))
                .isInstanceOf(CategoryNotFoundException.class)
                .hasMessageContaining(String.valueOf(categoryId));

        verify(categoryRepository, never()).delete(any(Category.class));
    }

    @Test
    void deleteCategory_shouldThrowConflictException_whenCategoryHasProducts() {
        UUID categoryId = UUID.randomUUID();

        Category category = mock(Category.class);

        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));
        when(productRepository.existsByCategoryId(categoryId)).thenReturn(true);

        assertThatThrownBy(() -> productService.deleteCategory(categoryId))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining(String.valueOf(categoryId));

        verify(categoryRepository, never()).delete(any(Category.class));
    }

    @Test
    void deleteCategory_shouldDeleteCategory_whenCategoryExistsAndHasNoProducts() {
        UUID categoryId = UUID.randomUUID();

        Category category = mock(Category.class);

        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));
        when(productRepository.existsByCategoryId(categoryId)).thenReturn(false);

        productService.deleteCategory(categoryId);

        verify(categoryRepository).delete(category);
    }

    @Test
    void getCategoryWithProducts_shouldThrowCategoryNotFoundException_whenCategoryDoesNotExist() {
        UUID categoryId = UUID.randomUUID();

        when(categoryRepository.findById(categoryId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.getCategoryWithProducts(categoryId))
                .isInstanceOf(CategoryNotFoundException.class)
                .hasMessageContaining(String.valueOf(categoryId));
    }

    @Test
    void getCategoryWithProducts_shouldReturnCategoryWithProducts_whenCategoryExist() {
        UUID categoryId = UUID.randomUUID();

        Category category = mock(Category.class);
        when(category.getName()).thenReturn("Books");
        when(category.getDescription()).thenReturn("Reading materials");

        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));
        when(productRepository.findByCategoryId(categoryId)).thenReturn(List.of(product));

        CategoryDetailResponse response = productService.getCategoryWithProducts(categoryId);

        assertThat(response.name()).isEqualTo("Books");
        assertThat(response.description()).isEqualTo("Reading materials");
        assertThat(response.products().size()).isEqualTo(1);
    }

    @Test
    void getAll_shouldReturnProducts_whenProductsExist() {
        Pageable pageable = PageRequest.of(0, 20);
        Page<Product> page = new PageImpl<>(List.of(product));

        when(productRepository.findAllWithCategory(pageable)).thenReturn(page);
        Page<ProductResponse> response = productService.getAll(pageable);
        assertThat(response.getContent().size()).isEqualTo(1);
        assertThat(response.getContent().getFirst().sku()).isEqualTo("SKU-100");
    }

    @Test
    void getAllProductsSummary_shouldReturnSummaries_whenProductsExist() {
        Pageable pageable = PageRequest.of(0, 20);
        ProductSummary productSummary = new ProductSummary(
                UUID.randomUUID(),
                "Mechanical Keyboard",
                BigDecimal.valueOf(79.99),
                "Electronics"
        );
        Page<ProductSummary> page = new PageImpl<>(List.of(productSummary));

        when(productRepository.findAllSummaries(pageable)).thenReturn(page);

        Page<ProductSummary> response = productService.getAllProductsSummary(pageable);

        assertThat(response.getContent().size()).isEqualTo(1);
        assertThat(response.getContent().getFirst().productName()).isEqualTo("Mechanical Keyboard");
    }
}
