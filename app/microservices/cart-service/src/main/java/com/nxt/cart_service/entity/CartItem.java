package com.nxt.cart_service.entity;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

@Getter
@Setter
public class CartItem {

    private Long id;
    private Long productId;
    private String productName;
    private BigDecimal unitPrice;
    private int quantity;
    private Map<String, Object> attributes = new LinkedHashMap<>();
}
