package com.nexora.notification.service;

import com.nexora.notification.dto.request.CreateNotificationRequest;
import com.nexora.notification.dto.response.NotificationResponse;
import com.nexora.notification.entity.Notification;
import com.nexora.notification.enums.NotificationStatus;
import tools.jackson.databind.node.StringNode;

import java.util.List;

public interface NotificationService {
    NotificationResponse createNotification(CreateNotificationRequest request);

    NotificationResponse getNotificationById(String notificationId);

    List<NotificationResponse> getNotificationByUserId(String userId);

    List<NotificationResponse> getNotificationByUserIdAndStatus(String userId, NotificationStatus status);

    List<NotificationResponse> getNotificationByReferenceId(String referenceId);

    void processNotification(String notificationId);

    void retryFailedNotifications();
}
