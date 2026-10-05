package com.nexora.payment.exception;

public class PaymentAlreadyExistsException extends BusinessException {

    public PaymentAlreadyExistsException(String referenceId) {
        super(
                String.format("Payment already exists with referenceId: %s", referenceId),
                "PAYMENT_ALREADY_EXISTS",
                409
        );
    }
}