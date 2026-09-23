package com.mouad.order_management_api.product.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mouad.order_management_api.auth.security.JwtAuthenticationFilter;
import com.mouad.order_management_api.product.dto.CategoryDetailResponse;
import com.mouad.order_management_api.product.dto.CategoryResponse;
import com.mouad.order_management_api.product.dto.CreateCategoryRequest;
import com.mouad.order_management_api.product.dto.ProductResponse;
import com.mouad.order_management_api.product.model.ProductStatus;
import com.mouad.order_management_api.product.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
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

@WebMvcTest(CategoryController.class)
@AutoConfigureMockMvc(addFilters = false)
public class CategoryControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockitoBean
    private ProductService productService;

    private CategoryResponse categoryResponse;

    private UUID categoryId;

    @BeforeEach
    void setUp() {
        categoryResponse = new CategoryResponse(
                UUID.randomUUID(),
                "Electronics",
                "Electronic items"
        );

        categoryId = UUID.randomUUID();
    }

    @Test
    void createCategory_shouldReturn201_whenRequestIsValid() throws Exception {
        CreateCategoryRequest request = new CreateCategoryRequest(
                "Electronics",
                "Electronic items"
        );

        when(productService.createCategory(any(CreateCategoryRequest.class))).thenReturn(categoryResponse);

        String json = objectMapper.writeValueAsString(request);

        mockMvc.perform(post("/api/v1/category")
                   .contentType(MediaType.APPLICATION_JSON)
                   .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Electronics"));
    }

    @Test
    void getAllCategories_shouldReturnList() throws Exception {
        when(productService.getAllCategories()).thenReturn(List.of(categoryResponse));

        mockMvc.perform(get("/api/v1/category"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Electronics"));
    }

    @Test
    void deleteCategory_shouldReturn204_whenCategoryExists() throws Exception {
        mockMvc.perform(delete("/api/v1/category/{id}", categoryId))
                .andExpect(status().isNoContent());
    }

    @Test
    void getCategoryWithProducts_shouldReturnCategoryWithProducts() throws Exception {
        ProductResponse productResponse = new ProductResponse(
                UUID.randomUUID(),
                "SKU-100",
                "Mechanical Keyboard",
                "Hot-swappable keyboard",
                BigDecimal.valueOf(79.99),
                25,
                categoryResponse,
                ProductStatus.ACTIVE
        );
        CategoryDetailResponse response = new CategoryDetailResponse(
                categoryId,
                "Electronics",
                "Electronic items",
                List.of(productResponse)
        );

        when(productService.getCategoryWithProducts(categoryId)).thenReturn(response);

        mockMvc.perform(get("/api/v1/category/{id}", categoryId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Electronics"));
    }
}