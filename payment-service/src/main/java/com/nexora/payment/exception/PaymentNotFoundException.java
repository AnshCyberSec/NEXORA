package com.nexora.payment.exception;

public class PaymentNotFoundException extends BusinessException {

    public PaymentNotFoundException(String id) {
        super(
                String.format("Payment not found with id: %s", id),
                "PAYMENT_NOT_FOUND",
                404
        );
    }

    public PaymentNotFoundException(String field, String value) {
        super(
                String.format("Payment with %s '%s' not found", field, value),
                "PAYMENT_NOT_FOUND",
                404
        );
    }
}