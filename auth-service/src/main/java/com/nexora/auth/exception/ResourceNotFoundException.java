package com.nexora.auth.exception;

public class ResourceNotFoundException extends BusinessException{

    public ResourceNotFoundException(String resource,String field,String value){
        super(
                String.format("%s with %s '%s' not found",resource,field,value),
                "RESOURCE_NOT_FOUND",
                404
        );
    }
}
