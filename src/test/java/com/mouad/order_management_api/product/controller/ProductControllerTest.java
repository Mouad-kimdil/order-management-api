package com.mouad.order_management_api.product.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import java.math.BigDecimal;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mouad.order_management_api.product.dto.CategoryResponse;
import com.mouad.order_management_api.product.dto.ProductResponse;
import com.mouad.order_management_api.product.model.ProductStatus;
import com.mouad.order_management_api.product.service.ProductServiceImpl;

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

}