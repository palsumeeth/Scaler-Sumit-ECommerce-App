package com.nxt.order_service.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class CheckoutItemRequest {
    @NotNull
    private Long productId;
    @NotBlank
    private String productName;
    @NotNull
    private BigDecimal unitPrice;
    @NotNull
    @Min(1)
    private Integer quantity;
}
