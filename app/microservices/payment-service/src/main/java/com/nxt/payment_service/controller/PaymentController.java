package com.nxt.payment_service.controller;

import com.nxt.payment_service.dto.PaymentDTO;
import com.nxt.payment_service.dto.ReceiptResponse;
import com.nxt.payment_service.service.PaymentService;
import com.nxt.payment_service.service.PaymentServiceImpl;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Log4j2
@RestController
@RequestMapping("/v1/payment")
public class PaymentController {

    private final PaymentService paymentService;
    private final PaymentServiceImpl paymentServiceImpl;

    @Autowired
    public PaymentController(PaymentService paymentService, PaymentServiceImpl paymentServiceImpl) {
        this.paymentService = paymentService;
        this.paymentServiceImpl = paymentServiceImpl;
    }

    @PostMapping("/initialize")
    public String initializePayment(@RequestBody PaymentDTO paymentDTO){
        log.info("[PaymentController][initializePayment] ");
        return paymentService
                .getPaymentLink(paymentDTO.getAmount(),
                                paymentDTO.getOrderId(),
                                paymentDTO.getPhoneNumber(),
                                paymentDTO.getName());
    }

    @GetMapping("/receipt/{orderId}")
    public ReceiptResponse receipt(@PathVariable String orderId) {
        return paymentServiceImpl.getReceipt(orderId);
    }
}