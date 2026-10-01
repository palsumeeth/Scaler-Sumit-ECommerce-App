package com.nxt.payment_service.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PaymentDTO {
    private Long amount;
    private String name;
    private String phoneNumber;
    private String orderId;
}