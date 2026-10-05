package com.nexora.payment.mapper;

import com.nexora.payment.dto.request.CreatePaymentRequest;
import com.nexora.payment.dto.response.PaymentResponse;
import com.nexora.payment.dto.response.PaymentStatusResponse;
import com.nexora.payment.entity.Payment;
import com.nexora.payment.enums.PaymentStatus;
import org.springframework.stereotype.Component;

@Component
public class PaymentMapper {

    public Payment toEntity(CreatePaymentRequest request) {
        return Payment.builder()
                .userId(request.getUserId())
                .walletId(request.getWalletId())
                .referenceId(request.getReferenceId())
                .amount(request.getAmount())
                .method(request.getMethod())
                .description(request.getDescription())
                .status(PaymentStatus.PENDING)
                .build();
    }

    public PaymentResponse toResponse(Payment payment) {
        return PaymentResponse.builder()
                .id(payment.getId())
                .userId(payment.getUserId())
                .walletId(payment.getWalletId())
                .referenceId(payment.getReferenceId())
                .amount(payment.getAmount())
                .currency(payment.getCurrency())
                .method(payment.getMethod())
                .status(payment.getStatus())
                .description(payment.getDescription())
                .failureReason(payment.getFailureReason())
                .transactionId(payment.getTransactionId())
                .createdAt(payment.getCreatedAt())
                .updatedAt(payment.getUpdatedAt())
                .build();
    }

    public PaymentStatusResponse toStatusResponse(Payment payment) {
        return PaymentStatusResponse.builder()
                .id(payment.getId())
                .referenceId(payment.getReferenceId())
                .status(payment.getStatus())
                .failureReason(payment.getFailureReason())
                .transactionId(payment.getTransactionId())
                .updatedAt(payment.getUpdatedAt())
                .build();
    }
}