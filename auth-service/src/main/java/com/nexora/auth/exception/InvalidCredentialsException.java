package com.nexora.auth.exception;

public class InvalidCredentialsException extends BusinessException{
    public InvalidCredentialsException(){
        super(
                "Invalid username or password",
                "INVALID_CREDENTIALS",
                401
        );
    }
}
