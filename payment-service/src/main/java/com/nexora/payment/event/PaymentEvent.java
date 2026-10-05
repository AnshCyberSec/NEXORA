package com.nexora.payment.event;

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
public class PaymentEvent {
    private String paymentId;

    private  String userId;

    private String walletId;

    private String referenceId;

    private BigDecimal amount;

    private PaymentMethod method;

    private PaymentStatus status;

    private String transactionId;

    private LocalDateTime eventTime;

    private String eventType;
}
