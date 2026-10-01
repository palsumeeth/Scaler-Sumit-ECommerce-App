package com.nxt.payment_service.repo;

import com.nxt.payment_service.entity.PaymentTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentTransactionRepository extends JpaRepository<PaymentTransaction, Long> {
    Optional<PaymentTransaction> findFirstByOrderIdOrderByCreatedAtDesc(String orderId);

    List<PaymentTransaction> findByStatus(String status);
}
