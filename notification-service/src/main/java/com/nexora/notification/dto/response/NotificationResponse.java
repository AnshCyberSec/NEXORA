package com.nexora.notification.dto.response;

import com.nexora.notification.enums.NotificationChannel;
import com.nexora.notification.enums.NotificationStatus;
import com.nexora.notification.enums.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationResponse {
    private String id;
    private String userId;
    private String referenceId;
    private NotificationType type;
    private NotificationChannel channel;
    private String subject;
    private String message;
    private String recipient;
    private NotificationStatus status;
    private String failureReason;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
