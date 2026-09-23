package com.mouad.order_management_api.product.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mouad.order_management_api.auth.security.JwtAuthenticationFilter;
import com.mouad.order_management_api.product.dto.ProductResponse;
import com.mouad.order_management_api.product.dto.CategoryResponse;
import com.mouad.order_management_api.product.dto.ProductSummary;
import com.mouad.order_management_api.product.dto.CreateProductRequest;
import com.mouad.order_management_api.product.dto.UpdateProductRequest;
import com.mouad.order_management_api.product.model.ProductStatus;
import com.mouad.order_management_api.product.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProductController.class)
@AutoConfigureMockMvc(addFilters = false)
public class ProductControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockitoBean
    private ProductService productService;

    private ProductResponse productResponse;

    private UUID productId;

    @BeforeEach
    void setUp() {
        CategoryResponse categoryResponse = new CategoryResponse(
                UUID.randomUUID(),
                "Electronics",
                "Electronic items"
        );

        productResponse = new ProductResponse(
                UUID.randomUUID(),
                "SKU-100",
                "Mechanical Keyboard",
                "Hot-swappable keyboard",
                BigDecimal.valueOf(79.99),
                25,
                categoryResponse,
                ProductStatus.ACTIVE
        );

        productId = UUID.randomUUID();
    }

    @Test
    void getAll_shouldReturnProducts_whenProductsExist() throws Exception {

        Page<ProductResponse> page = new PageImpl<>(List.of(productResponse));

        when(productService.getAll(any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/v1/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].sku").value("SKU-100"));
    }

    @Test
    void  getBySku_shouldReturnProduct_whenProductExists() throws Exception {
        when(productService.getBySku("SKU-100")).thenReturn(productResponse);

        mockMvc.perform(get("/api/v1/products/sku/{sku}", "SKU-100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sku").value("SKU-100"));
    }

    @Test
    void getProductSummary_shouldReturnSummaries_whenProductsExist() throws Exception {
        ProductSummary productSummary = new ProductSummary(
                UUID.randomUUID(),
                "Mechanical Keyboard",
                BigDecimal.valueOf(79.99),
                "Electronics"
        );
        Page<ProductSummary> page = new PageImpl<>(List.of(productSummary));

        when(productService.getAllProductsSummary(any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/v1/products/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].productName").value("Mechanical Keyboard"));
    }

    @Test
    void createProduct_shouldReturn201_whenRequestIsValid() throws Exception {
        CreateProductRequest request = new CreateProductRequest(
                "SKU-100",
                "Mechanical Keyboard",
                "Hot-swappable keyboard",
                BigDecimal.valueOf(79.99),
                25,
                "Electronics",
                ProductStatus.ACTIVE
        );

        when(productService.create(any(CreateProductRequest.class))).thenReturn(productResponse);

        String json = objectMapper.writeValueAsString(request);

        mockMvc.perform(post("/api/v1/products")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.sku").value("SKU-100"));
    }

    @Test
    void updateProduct_shouldReturn200_whenRequestIsValid() throws Exception {
        UpdateProductRequest request = new UpdateProductRequest(
                "SKU-100",
                "Mechanical Keyboard",
                "Hot-swappable keyboard",
                BigDecimal.valueOf(79.99),
                25,
                "Electronics",
                ProductStatus.ACTIVE
        );

        when(productService.update(any(UUID.class), any(UpdateProductRequest.class))).thenReturn(productResponse);

        String json = objectMapper.writeValueAsString(request);

        mockMvc.perform(put("/api/v1/products/{id}", productId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sku").value("SKU-100"));
    }

    @Test
    void deleteProduct_shouldReturn204_whenProductExists() throws Exception {
        mockMvc.perform(delete("/api/v1/products/{id}", productId))
                .andExpect(status().isNoContent());
    }
}