package com.nexora.payment.dto.request;

import com.nexora.payment.enums.PaymentStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentStatusUpdateRequest {
    @NotNull(message = "Status is required")
    private PaymentStatus status;

    private String failureReason;

    private String transactionId;
}
