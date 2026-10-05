package com.nexora.auth.exception;

public class UserAlreadyExistsException extends BusinessException{

    public UserAlreadyExistsException(String field,String value){
        super(
                String.format("User with %s '%s' already exists",field,value),
                "USER_ALREADY_EXISTS",
                409
        );
    }
}
