package com.nexora.notification.mapper;

import com.nexora.notification.dto.request.CreateNotificationRequest;
import com.nexora.notification.dto.response.NotificationResponse;
import com.nexora.notification.entity.Notification;
import org.springframework.stereotype.Component;

@Component
public class NotificationMapper {
    public Notification toEntity(CreateNotificationRequest request){
        return  Notification.builder()
                .userId(request.getUserId())
                .referenceId(request.getReferenceId())
                .type(request.getType())
                .channel(request.getChannel())
                .subject(request.getSubject())
                .message(request.getMessage())
                .recipient(request.getRecipient())
                .build();
    }

    public NotificationResponse toResponse(Notification notification){
        return NotificationResponse.builder()
                .id(notification.getId())
                .userId(notification.getUserId())
                .referenceId(notification.getReferenceId())
                .type(notification.getType())
                .channel(notification.getChannel())
                .subject(notification.getSubject())
                .recipient(notification.getRecipient())
                .status(notification.getStatus())
                .failureReason(notification.getFailureReason())
                .createdAt(notification.getCreatedAt())
                .updatedAt(notification.getUpdatedAt())
                .build();
    }
}
