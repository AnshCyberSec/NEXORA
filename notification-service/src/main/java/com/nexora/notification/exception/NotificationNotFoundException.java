package com.nexora.notification.exception;

public class NotificationNotFoundException extends BusinessException{

    public NotificationNotFoundException(String id){
        super(
                String.format("Notification not found with id : %s",id),
                "NOTIFICATION_NOT_FOUND",
                404
        );
    }

    public NotificationNotFoundException(String field, String value){
        super(
                String.format("Notification with %s '%s' not found",field,value),
                "NOTIFICATION_NOT_FOUND",
                404
        );
    }
}
