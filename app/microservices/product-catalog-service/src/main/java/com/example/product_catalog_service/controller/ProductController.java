package com.example.product_catalog_service.controller;

import com.example.product_catalog_service.dto.CreateProductReqDTO;
import com.example.product_catalog_service.dto.ProductDetailsRespDTO;
import com.example.product_catalog_service.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping
    public ResponseEntity<ProductDetailsRespDTO> createProduct(@Valid @RequestBody CreateProductReqDTO req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(productService.createProduct(req));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductDetailsRespDTO> getProductDetails(@PathVariable Long id) {
        ProductDetailsRespDTO resp = productService.getProductDetails(id);
        return ResponseEntity.ok(resp);
    }

    @GetMapping("/category/{categoryId}")
    public ResponseEntity<List<ProductDetailsRespDTO>> browseByCategory(@PathVariable Long categoryId) {
        return ResponseEntity.ok(productService.browseByCategory(categoryId));
    }

    @GetMapping("/search")
    public ResponseEntity<List<ProductDetailsRespDTO>> search(@RequestParam("q") String keyword) {
        return ResponseEntity.ok(productService.search(keyword));
    }
}
