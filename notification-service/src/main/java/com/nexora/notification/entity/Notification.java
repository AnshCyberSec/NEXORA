package com.nexora.notification.entity;

import com.nexora.notification.enums.NotificationChannel;
import com.nexora.notification.enums.NotificationStatus;
import com.nexora.notification.enums.NotificationType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "notifications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification extends BaseEntity{
    @Column(nullable = false)
    private String userId;

    @Column(nullable = false, length = 50)
    private String referenceId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private NotificationType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private NotificationChannel channel;

    @Column(nullable = false,length = 200)
    private String subject;

    @Column(nullable = false, length = 1000)
    private String message;

    @Column(length = 100)
    private String recipient;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false ,length = 20)
    @Builder.Default
    private NotificationStatus status = NotificationStatus.PENDING;

    private String failureReason;
}