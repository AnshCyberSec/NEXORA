package com.nexora.notification.controller;

import com.nexora.notification.common.ApiResponse;
import com.nexora.notification.dto.request.CreateNotificationRequest;
import com.nexora.notification.dto.response.NotificationResponse;
import com.nexora.notification.enums.NotificationStatus;
import com.nexora.notification.service.NotificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {
    private final NotificationService notificationService;

    private static final String LOG_PREFIX = "[NOTIFICATION-CONTROLLER]";

    @PostMapping
    public ResponseEntity<ApiResponse<NotificationResponse>> createNotification(@Valid @RequestBody CreateNotificationRequest request){
        log.info("{} Create notification for userId:{},type : {},channel:{}",LOG_PREFIX,request.getUserId(),request.getType(),request.getChannel());

        NotificationResponse response = notificationService.createNotification(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response,"Notification created successfully"));
    }

    @GetMapping("/{notificationId}")
    public ResponseEntity<ApiResponse<NotificationResponse>> getNotificationById(@PathVariable String notificationId){
        log.debug("{} Get notification by id: {}",LOG_PREFIX, notificationId);

        NotificationResponse response = notificationService.getNotificationById(notificationId);
        return ResponseEntity.ok(ApiResponse.success(response,"Notification fetched successfully"));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<NotificationResponse>>> getNotificationsByUserId(
            @PathVariable String userId) {
        log.debug("{} Get notifications for userId: {}", LOG_PREFIX, userId);

        List<NotificationResponse> responses = notificationService.getNotificationByUserId(userId);
        return ResponseEntity.ok(ApiResponse.success(responses, "Notifications fetched successfully"));
    }

    @GetMapping("/user/{userId}/status/{status}")
    public ResponseEntity<ApiResponse<List<NotificationResponse>>> getNotificationsByUserIdAndStatus(
            @PathVariable String userId,
            @PathVariable NotificationStatus status) {
        log.debug("{} Get notifications for userId: {}, status: {}", LOG_PREFIX, userId, status);

        List<NotificationResponse> responses = notificationService.getNotificationByUserIdAndStatus(userId, status);
        return ResponseEntity.ok(ApiResponse.success(responses, "Notifications fetched successfully"));
    }

    @GetMapping("/reference/{referenceId}")
    public ResponseEntity<ApiResponse<List<NotificationResponse>>> getNotificationsByReferenceId(
            @PathVariable String referenceId) {
        log.debug("{} Get notifications for referenceId: {}", LOG_PREFIX, referenceId);

        List<NotificationResponse> responses = notificationService.getNotificationByReferenceId(referenceId);
        return ResponseEntity.ok(ApiResponse.success(responses, "Notifications fetched successfully"));
    }

    @PostMapping("/{notificationId}/retry")
    public ResponseEntity<ApiResponse<Void>> retryNotification(
            @PathVariable String notificationId) {
        log.info("{} Retry notification for id: {}", LOG_PREFIX, notificationId);

        notificationService.processNotification(notificationId);
        return ResponseEntity.ok(ApiResponse.success(null, "Notification retry initiated successfully"));
    }

    @PostMapping("/retry-failed")
    public ResponseEntity<ApiResponse<Void>> retryAllFailedNotifications() {
        log.info("{} Retry all failed notifications", LOG_PREFIX);

        notificationService.retryFailedNotifications();
        return ResponseEntity.ok(ApiResponse.success(null, "Failed notifications retry initiated successfully"));
    }

}
