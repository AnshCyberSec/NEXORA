package com.nexora.payment.repository;

import com.nexora.payment.entity.Payment;
import com.nexora.payment.enums.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment,String> {
    Optional<Payment> findByReferenceId(String referenceId);

    List<Payment> findByUserIdOrderByCreatedAtDesc(String userId);

    List<Payment> findByWalletIdOrderByCreatedAtDesc(String walletId);

    List<Payment> findByUserIdAndStatus(String userId, PaymentStatus status);
}



