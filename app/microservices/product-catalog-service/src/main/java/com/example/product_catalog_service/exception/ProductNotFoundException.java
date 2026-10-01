package com.example.product_catalog_service.exception;

public class ProductNotFoundException extends RuntimeException {
    public ProductNotFoundException(Long productId) {
      super("Product not found with id: " + productId);
    }
}
