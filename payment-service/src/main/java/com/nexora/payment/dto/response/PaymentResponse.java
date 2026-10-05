package com.nexora.payment.dto.response;

import com.nexora.payment.enums.PaymentMethod;
import com.nexora.payment.enums.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponse {
    private String id;
    private String userId;
    private String walletId;
    private String referenceId;
    private BigDecimal amount;
    private String currency;
    private PaymentMethod method;
    private PaymentStatus status;
    private String description;
    private String failureReason;
    private String transactionId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
