package com.nexora.notification.exception;

import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException{
    private final String errorCode;
    private final int statusCode;

    protected BusinessException(String message, String errorCode,int statusCode){
        super(message);
        this.errorCode = errorCode;
        this.statusCode = statusCode;
    }
}
