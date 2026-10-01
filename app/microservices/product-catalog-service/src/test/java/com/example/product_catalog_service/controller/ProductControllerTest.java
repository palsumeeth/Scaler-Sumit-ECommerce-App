package com.example.product_catalog_service.controller;

import com.example.product_catalog_service.dto.ProductDetailsRespDTO;
import com.example.product_catalog_service.exception.ProductNotFoundException;
import com.example.product_catalog_service.model.CategorySummary;
import com.example.product_catalog_service.service.ProductService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ProductController.class)
public class ProductControllerTest {

    @MockBean
    private ProductService productService;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldReturn200WhenProductExists() throws Exception {
        ProductDetailsRespDTO dto = new ProductDetailsRespDTO(
                100L,
                "iPhone",
                "Latest model",
                java.math.BigDecimal.valueOf(999),
                new CategorySummary(1L, "Smartphones"),
                List.of("img1"),
                Map.of(),
                Map.of()
        );

        when(productService.getProductDetails(100L)).thenReturn(dto);

        mockMvc.perform(get("/products/100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(100L))
                .andExpect(jsonPath("$.name").value("iPhone"))
                .andExpect(jsonPath("$.category.name").value("Smartphones"));
    }

    @Test
    void shouldReturn404WhenProductNotFound() throws Exception {
        when(productService.getProductDetails(100L)).thenThrow(new ProductNotFoundException(100L));

        mockMvc.perform(get("/products/100"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("NOT_FOUND"));
    }
}