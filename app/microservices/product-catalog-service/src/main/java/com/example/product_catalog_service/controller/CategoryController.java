package com.example.product_catalog_service.controller;

import com.example.product_catalog_service.dto.CategoryRespDTO;
import com.example.product_catalog_service.dto.CreateCategoryReqDTO;
import com.example.product_catalog_service.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/categories")
public class CategoryController {

    private final ProductService productService;

    public CategoryController(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping
    public ResponseEntity<CategoryRespDTO> createCategory(@Valid @RequestBody CreateCategoryReqDTO req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(productService.createCategory(req));
    }

    @GetMapping
    public ResponseEntity<List<CategoryRespDTO>> listCategories() {
        return ResponseEntity.ok(productService.listCategories());
    }
}
