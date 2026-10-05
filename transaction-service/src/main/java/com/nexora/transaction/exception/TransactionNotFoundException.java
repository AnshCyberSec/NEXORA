package com.nexora.transaction.exception;

public class TransactionNotFoundException extends BusinessException {

    public TransactionNotFoundException(String id) {
        super(
                String.format("Transaction not found with id: %s", id),
                "TRANSACTION_NOT_FOUND",
                404
        );
    }

    public TransactionNotFoundException(String field, String value) {
        super(
                String.format("Transaction with %s '%s' not found", field, value),
                "TRANSACTION_NOT_FOUND",
                404
        );
    }
}