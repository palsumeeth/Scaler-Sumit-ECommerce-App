package com.nxt.payment_service.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Instant;

@Getter
@AllArgsConstructor
public class ReceiptResponse {
    private String receiptId;
    private String orderId;
    private Long amount;
    private String gateway;
    private String status;
    private Instant createdAt;
}
