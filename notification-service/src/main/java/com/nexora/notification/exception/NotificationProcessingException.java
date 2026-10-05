package com.nexora.notification.exception;

public class NotificationProcessingException extends BusinessException{
    public NotificationProcessingException(String message){
        super(message,"NOTIFICATION_PROCSSING_FAILED",500);
    }
    public NotificationProcessingException(String message, Throwable cause){
        super(message,"NOTIFICATION_PROCESSING_FAILED",500);
        initCause(cause);
    }
}
