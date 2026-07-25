package com.mouad.order_management_api.product.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mouad.order_management_api.product.dto.CategoryResponse;
import com.mouad.order_management_api.product.dto.CreateProductRequest;
import com.mouad.order_management_api.product.dto.ProductResponse;
import com.mouad.order_management_api.product.dto.ProductSummary;
import com.mouad.order_management_api.product.dto.UpdateProductRequest;
import com.mouad.order_management_api.product.model.ProductStatus;
import com.mouad.order_management_api.product.service.ProductServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ProductController.class)
public class ProductControllerTest {
    
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductServiceImpl productService;

    @Autowired
    private ObjectMapper objectMapper;

    private ProductResponse productResponse;
    UUID productId;
    private CategoryResponse categoryResponse;

    @BeforeEach
    void setUp() {
		productId = UUID.randomUUID();
        categoryResponse = new CategoryResponse(
            UUID.randomUUID(),
            "Electronics",
            "Electronic items"
        );

        productResponse = new ProductResponse(
            productId,
            "SKU-100",
            "Mechanical Keyboard",
            "Hot-swappable keyboard",
            BigDecimal.valueOf(79.99),
            25,
            categoryResponse,
            ProductStatus.ACTIVE
        );
    }

    @Test
    void get_shouldReturnProductResponse_whenIdIsValid() throws Exception {
        when(productService.getById(productId)).thenReturn(productResponse);

        mockMvc.perform(get("/api/v1/products/{id}", productId))
	           .andExpect(status().isOk())
	           .andExpect(jsonPath("$.sku").value("SKU-100"));
    }

    @Test
    void createProduct_shouldReturn201() throws Exception {
        CreateProductRequest request = new CreateProductRequest(
            "SKU-200", "New Product", "Description",
            BigDecimal.valueOf(49.99), 10, "Electronics", ProductStatus.ACTIVE
        );
        when(productService.create(any(CreateProductRequest.class))).thenReturn(productResponse);

        mockMvc.perform(post("/api/v1/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.sku").value("SKU-100"));
    }

    @Test
    void getAllProducts_shouldReturnPage() throws Exception {
        Page<ProductResponse> page = new PageImpl<>(List.of(productResponse));
        when(productService.getAll(any())).thenReturn(page);

        mockMvc.perform(get("/api/v1/products"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].sku").value("SKU-100"));
    }

    @Test
    void getProductBySku_shouldReturn200() throws Exception {
        when(productService.getBySku("SKU-100")).thenReturn(productResponse);

        mockMvc.perform(get("/api/v1/products/sku/{sku}", "SKU-100"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.sku").value("SKU-100"));
    }

    @Test
    void getProductSummary_shouldReturnPage() throws Exception {
        ProductSummary summary = new ProductSummary(productId, "Mechanical Keyboard", BigDecimal.valueOf(79.99), "Electronics");
        Page<ProductSummary> page = new PageImpl<>(List.of(summary));
        when(productService.getAllProductsSummary(any())).thenReturn(page);

        mockMvc.perform(get("/api/v1/products/summary"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].productName").value("Mechanical Keyboard"));
    }

    @Test
    void updateProduct_shouldReturn200() throws Exception {
        UpdateProductRequest request = new UpdateProductRequest(
            "SKU-999", "Updated Product", "Updated",
            BigDecimal.valueOf(99.99), 30, "Electronics", ProductStatus.ACTIVE
        );
        when(productService.update(any(UUID.class), any(UpdateProductRequest.class))).thenReturn(productResponse);

        mockMvc.perform(put("/api/v1/products/{id}", productId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.sku").value("SKU-100"));
    }

    @Test
    void deleteProduct_shouldReturn204() throws Exception {
        mockMvc.perform(delete("/api/v1/products/{id}", productId))
            .andExpect(status().isNoContent());
    }
}