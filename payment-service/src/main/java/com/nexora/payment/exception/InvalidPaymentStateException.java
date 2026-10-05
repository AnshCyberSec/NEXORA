package com.nexora.payment.exception;

import com.nexora.payment.enums.PaymentStatus;

public class InvalidPaymentStateException extends BusinessException {

    public InvalidPaymentStateException(String paymentId, PaymentStatus currentStatus) {
        super(
                String.format("Invalid payment state transition for payment %s. Current status: %s",
                        paymentId, currentStatus),
                "INVALID_PAYMENT_STATE",
                400
        );
    }

    public InvalidPaymentStateException(String message) {
        super(message, "INVALID_PAYMENT_STATE", 400);
    }
}