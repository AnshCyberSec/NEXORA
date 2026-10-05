package com.nexora.transaction.exception;

public class InvalidTransactionException extends BusinessException {

    public InvalidTransactionException(String message) {
        super(message, "INVALID_TRANSACTION", 400);
    }

    public InvalidTransactionException(String message, String errorCode) {
        super(message, errorCode, 400);
    }

    public InvalidTransactionException(String message, String errorCode, int statusCode) {
        super(message, errorCode, statusCode);
    }
}