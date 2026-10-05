package com.nexora.fraud.exception;

public class FraudCheckException extends BusinessException {

    public FraudCheckException(String message) {
        super(message, "FRAUD_CHECK_FAILED", 500);
    }

    public FraudCheckException(String message, Throwable cause) {
        super(message, "FRAUD_CHECK_FAILED", 500);
        initCause(cause);
    }
}