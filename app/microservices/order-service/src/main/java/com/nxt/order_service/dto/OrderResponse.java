package com.nxt.order_service.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Getter
@AllArgsConstructor
public class OrderResponse {
    private Long orderId;
    private Long userId;
    private String email;
    private String status;
    private String paymentMethod;
    private String addressLine;
    private String city;
    private String postalCode;
    private String country;
    private BigDecimal totalAmount;
    private String receiptId;
    private Instant createdAt;
    private List<OrderItemResponse> items;
}
