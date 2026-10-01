package com.example.product_catalog_service.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CategoryRespDTO {
    private Long id;
    private String name;
    private String description;
}
