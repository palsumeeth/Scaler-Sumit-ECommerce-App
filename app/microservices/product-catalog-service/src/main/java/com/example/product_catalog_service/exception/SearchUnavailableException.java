package com.example.product_catalog_service.exception;

public class SearchUnavailableException extends RuntimeException {

    public SearchUnavailableException() {
        super("Product search is unavailable");
    }
}
