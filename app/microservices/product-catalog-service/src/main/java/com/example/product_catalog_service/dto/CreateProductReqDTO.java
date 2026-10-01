package com.example.product_catalog_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class CreateProductReqDTO {
    @NotBlank
    private String name;
    private String description;
    @NotNull
    private BigDecimal price;
    @NotNull
    private Long categoryId;
    private List<String> imageUrls;
    private Map<String, Object> specifications;
    private Map<String, Object> metadata;
}
