package com.nexora.wallet.exception;

import lombok.Getter;

@Getter
public class WalletOperationException extends BusinessException {

    public WalletOperationException(String message, String errorCode, int statusCode) {
        super(message, errorCode, statusCode);
    }

    public WalletOperationException(String message) {
        super(message, "WALLET_OPERATION_FAILED", 400);
    }
}