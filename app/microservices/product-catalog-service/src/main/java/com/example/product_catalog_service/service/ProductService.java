package com.example.product_catalog_service.service;

import com.example.product_catalog_service.dto.CreateCategoryReqDTO;
import com.example.product_catalog_service.dto.CreateProductReqDTO;
import com.example.product_catalog_service.dto.CategoryRespDTO;
import com.example.product_catalog_service.dto.ProductDetailsRespDTO;
import com.example.product_catalog_service.entity.Category;
import com.example.product_catalog_service.entity.Product;
import com.example.product_catalog_service.entity.State;
import com.example.product_catalog_service.exception.ProductNotFoundException;
import com.example.product_catalog_service.model.CategorySummary;
import com.example.product_catalog_service.repo.CategoryRepository;
import com.example.product_catalog_service.repo.ProductRepository;
import com.example.product_catalog_service.search.ProductSearchIndex;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class ProductService {

    private final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<Map<String, Object>>() {};
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ObjectMapper objectMapper;
    private final ProductSearchIndex productSearchIndex;

    @Autowired
    public ProductService(ProductRepository productRepository,
                          CategoryRepository categoryRepository,
                          ObjectMapper objectMapper,
                          ProductSearchIndex productSearchIndex) {

        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.objectMapper = objectMapper;
        this.productSearchIndex = productSearchIndex;
    }

    public CategoryRespDTO createCategory(CreateCategoryReqDTO req) {
        Category category = new Category();
        category.setName(req.getName());
        category.setDescription(req.getDescription());
        category.setState(State.ACTIVE);
        category = categoryRepository.save(category);
        return new CategoryRespDTO(category.getId(), category.getName(), category.getDescription());
    }

    public List<CategoryRespDTO> listCategories() {
        return categoryRepository.findByState(State.ACTIVE).stream()
                .map(category -> new CategoryRespDTO(category.getId(), category.getName(), category.getDescription()))
                .toList();
    }

    public ProductDetailsRespDTO createProduct(CreateProductReqDTO req) {
        Category category = categoryRepository.findById(req.getCategoryId())
                .orElseThrow(() -> new IllegalArgumentException("Category not found: " + req.getCategoryId()));

        Product product = new Product();
        product.setName(req.getName());
        product.setDescription(req.getDescription());
        product.setPrice(req.getPrice());
        product.setCategory(category);
        product.setImageUrls(req.getImageUrls());
        product.setSpecifications(writeJson(req.getSpecifications()));
        product.setMetadata(writeJson(req.getMetadata()));
        product.setState(State.ACTIVE);
        Product saved = productRepository.save(product);
        productSearchIndex.index(saved);
        return toDto(saved);
    }

    public ProductDetailsRespDTO getProductDetails(Long productId) {
        Product product = productRepository
                .findProductDetails(productId, State.ACTIVE)
                .orElseThrow(() -> new ProductNotFoundException(productId));
        return toDto(product);
    }

    public List<ProductDetailsRespDTO> browseByCategory(Long categoryId) {
        if (!categoryRepository.existsById(categoryId)) {
            throw new IllegalArgumentException("Category not found: " + categoryId);
        }
        return productRepository.findActiveByCategory(categoryId, State.ACTIVE).stream()
                .map(this::toDto)
                .toList();
    }

    public List<ProductDetailsRespDTO> search(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            throw new IllegalArgumentException("Search keyword is required");
        }
        List<Long> ids = productSearchIndex.searchIds(keyword.trim());
        if (ids.isEmpty()) {
            return List.of();
        }
        Map<Long, Product> products = productRepository.findActiveByIds(ids, State.ACTIVE).stream()
                .collect(Collectors.toMap(Product::getId, Function.identity()));
        return ids.stream()
                .map(products::get)
                .filter(Objects::nonNull)
                .map(this::toDto)
                .toList();
    }

    private ProductDetailsRespDTO toDto(Product product) {
        CategorySummary category =
                new CategorySummary(
                        product.getCategory().getId(),
                        product.getCategory().getName()
                );

        return new ProductDetailsRespDTO(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                category,
                product.getImageUrls(),
                parseJson(product.getSpecifications()),
                parseJson(product.getMetadata()));
    }

    private Map<String, Object> parseJson(String json) {
        if (json == null || json.isBlank()) {
            return Collections.emptyMap();
        }
        try {
            return objectMapper.readValue(json, MAP_TYPE);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Invalid JSON in product data", e);
        }
    }

    private String writeJson(Map<String, Object> value) {
        if (value == null || value.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Specifications or metadata must be valid JSON");
        }
    }
}
