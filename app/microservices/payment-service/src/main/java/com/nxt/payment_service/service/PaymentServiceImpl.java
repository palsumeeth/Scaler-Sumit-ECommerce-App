package com.nxt.payment_service.service;

import com.nxt.payment_service.client.PaymentGatewayClient;
import com.nxt.payment_service.dto.ReceiptResponse;
import com.nxt.payment_service.entity.PaymentTransaction;
import com.nxt.payment_service.repo.PaymentTransactionRepository;
import com.nxt.payment_service.strategy.PaymentGatewayStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.NoSuchElementException;

@Service
public class PaymentServiceImpl implements PaymentService {

    private final PaymentGatewayStrategy paymentGatewayStrategy;
    private final PaymentTransactionRepository paymentTransactionRepository;

    @Autowired
    public PaymentServiceImpl(PaymentGatewayStrategy paymentGatewayStrategy,
                              PaymentTransactionRepository paymentTransactionRepository) {
        this.paymentGatewayStrategy = paymentGatewayStrategy;
        this.paymentTransactionRepository = paymentTransactionRepository;
    }

    @Override
    public String getPaymentLink(Long amount, String orderId, String phoneNumber, String name) {
        PaymentGatewayClient paymentGateway = paymentGatewayStrategy.getOptimalPaymentGateway();
        PaymentTransaction transaction = new PaymentTransaction();
        transaction.setOrderId(orderId);
        transaction.setAmount(amount);
        transaction.setPhoneNumber(phoneNumber);
        transaction.setCustomerName(name);
        transaction.setGateway(paymentGateway.getClass().getSimpleName());
        transaction.setStatus("PENDING");
        transaction.setCreatedAt(Instant.now());

        try {
            String link = paymentGateway.getPaymentLink(amount, orderId, phoneNumber, name);
            transaction.setPaymentLink(link);
            paymentTransactionRepository.save(transaction);
            return link;
        } catch (RuntimeException ex) {
            transaction.setStatus("FAILED");
            paymentTransactionRepository.save(transaction);
            throw ex;
        }
    }

    public void rememberCustomerEmail(String orderId, String email) {
        paymentTransactionRepository.findFirstByOrderIdOrderByCreatedAtDesc(orderId)
                .ifPresent(transaction -> {
                    transaction.setEmail(email);
                    paymentTransactionRepository.save(transaction);
                });
    }

    public ReceiptResponse getReceipt(String orderId) {
        PaymentTransaction transaction = paymentTransactionRepository
                .findFirstByOrderIdOrderByCreatedAtDesc(orderId)
                .orElseThrow(() -> new NoSuchElementException("No payment found for order " + orderId));
        return new ReceiptResponse(
                transaction.getReceiptId(),
                transaction.getOrderId(),
                transaction.getAmount(),
                transaction.getGateway(),
                transaction.getStatus(),
                transaction.getCreatedAt()
        );
    }
}
