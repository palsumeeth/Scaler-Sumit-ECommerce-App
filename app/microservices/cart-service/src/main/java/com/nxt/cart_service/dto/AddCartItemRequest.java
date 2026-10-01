package com.nxt.cart_service.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Map;

@Getter
@Setter
public class AddCartItemRequest {
    @NotNull
    private Long userId;
    @NotNull
    private Long productId;
    @NotBlank
    private String productName;
    @NotNull
    private BigDecimal unitPrice;
    @NotNull
    @Min(1)
    private Integer quantity;
    private Map<String, Object> attributes;
}
