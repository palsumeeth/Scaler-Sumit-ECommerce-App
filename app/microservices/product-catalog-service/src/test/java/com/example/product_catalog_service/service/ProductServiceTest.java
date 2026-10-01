package com.example.product_catalog_service.service;

import com.example.product_catalog_service.dto.ProductDetailsRespDTO;
import com.example.product_catalog_service.entity.Category;
import com.example.product_catalog_service.entity.Product;
import com.example.product_catalog_service.entity.State;
import com.example.product_catalog_service.exception.ProductNotFoundException;
import com.example.product_catalog_service.repo.ProductRepository;
import com.example.product_catalog_service.search.ProductSearchIndex;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private ProductSearchIndex productSearchIndex;

    @InjectMocks
    private ProductService productService;

    @Test
    void shouldReturnProductDetailsRespSuccessfullyWhenProductExistsAndActive() throws JsonProcessingException {
        Product product = new Product();
        product.setId(100L);
        product.setName("iPhone");
        product.setDescription("Latest model");
        product.setImageUrls(List.of("img1", "img2"));
        product.setSpecifications("{\"ram\":\"8GB\"}");
        product.setMetadata("{\"featured\":true}");

        Category category = new Category();
        category.setId(1L);
        category.setName("Smartphones");
        product.setCategory(category);

        when(productRepository.findProductDetails(100L, State.ACTIVE)).thenReturn(Optional.of(product));
        when(objectMapper.readValue(
                anyString(),
                ArgumentMatchers.<TypeReference<Map<String, Object>>>any()
        )).thenReturn(Map.of("key", "value"));

        ProductDetailsRespDTO resp = productService.getProductDetails(100L);

        assertEquals(100L, resp.getId());
        assertEquals("iPhone", resp.getName());
        assertEquals("Latest model", resp.getDescription());
        assertEquals("Smartphones", resp.getCategory().getName());
        assertEquals(List.of("img1", "img2"), resp.getImages());
        assertEquals(Map.of("key", "value"), resp.getSpecifications());
        assertEquals(Map.of("key", "value"), resp.getMetadata());

        verify(productRepository).findProductDetails(100L, State.ACTIVE);
        verify(objectMapper, times(2))
                .readValue(anyString(), ArgumentMatchers.<TypeReference<Map<String, Object>>>any());
    }

    @Test
    void shouldThrowWhenProductNotFound() {
        when(productRepository.findProductDetails(100L, State.ACTIVE))
                .thenReturn(Optional.empty());

        assertThrows(ProductNotFoundException.class,
                () -> productService.getProductDetails(100L));
    }

    @Test
    void shouldReturnEmptyMapsWhenJsonIsNull() {
        Product product = new Product();
        product.setId(1L);
        product.setName("Test");
        product.setDescription("Desc");
        product.setImageUrls(List.of());
        product.setSpecifications(null);
        product.setMetadata(null);

        Category category = new Category();
        category.setId(1L);
        category.setName("Cat");
        product.setCategory(category);

        when(productRepository.findProductDetails(1L, State.ACTIVE))
                .thenReturn(Optional.of(product));

        ProductDetailsRespDTO resp = productService.getProductDetails(1L);

        assertEquals(Map.of(), resp.getSpecifications());
        assertEquals(Map.of(), resp.getMetadata());
    }
}