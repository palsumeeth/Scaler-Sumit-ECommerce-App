package com.example.product_catalog_service.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateCategoryReqDTO {
    @NotBlank
    private String name;
    private String description;
}
