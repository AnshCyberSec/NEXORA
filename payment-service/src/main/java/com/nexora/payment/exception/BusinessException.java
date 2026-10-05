package com.nexora.payment.exception;

import lombok.Getter;

@Getter
public abstract class BusinessException extends RuntimeException {

    private final String errorCode;
    private final int statusCode;

    protected BusinessException(String message, String errorCode, int statusCode) {
        super(message);
        this.errorCode = errorCode;
        this.statusCode = statusCode;
    }
}