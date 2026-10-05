package com.nexora.payment.dto.response;

import com.nexora.payment.enums.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentStatusResponse {
    private String id;
    private String referenceId;
    private PaymentStatus status;
    private String failureReason;
    private String transactionId;
    private LocalDateTime updatedAt;
}

