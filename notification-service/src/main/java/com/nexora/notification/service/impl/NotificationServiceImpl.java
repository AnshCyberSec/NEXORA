package com.nexora.notification.service.impl;

import com.nexora.notification.dto.request.CreateNotificationRequest;
import com.nexora.notification.dto.response.NotificationResponse;
import com.nexora.notification.entity.Notification;
import com.nexora.notification.enums.NotificationStatus;
import com.nexora.notification.exception.NotificationNotFoundException;
import com.nexora.notification.exception.NotificationProcessingException;
import com.nexora.notification.mapper.NotificationMapper;
import com.nexora.notification.repository.NotificationRepository;
import com.nexora.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class NotificationServiceImpl implements NotificationService {

    private  final NotificationRepository notificationRepository;
    private final NotificationMapper notificationMapper;

    private static final String LOG_PREFIX = "[NOTIFICATION-SERVICE]";

    @Override
    public NotificationResponse createNotification(CreateNotificationRequest request) {
        log.info("{} Creating notification for userId: {},type: {}, channel: {}", LOG_PREFIX,request.getUserId(), request.getType(), request.getChannel());

        Notification notification = notificationMapper.toEntity(request);
        Notification savedNotification = notificationRepository.save(notification);

        log.info("{} Notification created successfully with id: {}, referenceId: {}",LOG_PREFIX, savedNotification.getId(),savedNotification.getReferenceId());

        try{
            processNotification(savedNotification.getId());
        }catch (Exception e){
            log.error("{} Failed to process notification: {}",LOG_PREFIX,savedNotification.getId(),e);
        }
        return notificationMapper.toResponse(savedNotification);
    }

    @Override
    @Transactional(readOnly = true)
    public NotificationResponse getNotificationById(String notificationId) {
        log.debug("{} Fetching notification by id: {}",LOG_PREFIX, notificationId);

        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(()->{
                    log.warn("{} Notification not found with id: {}",LOG_PREFIX,notificationId);
                    return new NotificationNotFoundException(notificationId);
                });
        return notificationMapper.toResponse(notification);

    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationResponse> getNotificationByUserId(String userId) {
        log.debug("{} Fetching notifications for userId: {}",LOG_PREFIX,userId);

        List<Notification> notifications = notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
        return notifications.stream()
                .map(notificationMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationResponse> getNotificationByUserIdAndStatus(String userId, NotificationStatus status) {
        log.debug("{} Fetching notifications for userId: {}, status: {}",LOG_PREFIX, userId,status);

        List<Notification> notifications = notificationRepository.findByUserIdAndStatus(userId,status);

        return notifications.stream()
                .map(notificationMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationResponse> getNotificationByReferenceId(String referenceId) {
        log.debug("{} Fetching notifications for referenceId: {}",LOG_PREFIX, referenceId);

        List<Notification> notifications = notificationRepository.findByReferenceId(referenceId);

        return  notifications.stream()
                .map(notificationMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public void processNotification(String notificationId) {
        log.info("{} Processing notification: {}", LOG_PREFIX, notificationId);

        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> {
                    log.warn("{} Notification not found with id: {}", LOG_PREFIX, notificationId);

                    return new NotificationNotFoundException(notificationId);
                });
        try{
            sendNotification(notification);
            notification.setStatus(NotificationStatus.SENT);
            notificationRepository.save(notification);

            log.info("{} Notfication sent successfully : {},channel : {}",LOG_PREFIX, notificationId,notification.getChannel());
        }catch (Exception e){
            log.error("{} Notification send successfully : {},error :{}",LOG_PREFIX,notificationId,e.getMessage());

            notification.setStatus(NotificationStatus.FAILED);
            notification.setFailureReason(e.getMessage());
            notificationRepository.save(notification);

            throw new NotificationProcessingException(
                    String.format("Failed to send notification: %s",notificationId),e
            );
        }
    }

    @Override
    public void retryFailedNotifications() {
        log.info("{} Retrying failed notifications", LOG_PREFIX);

        List<Notification> failedNotification = notificationRepository.findByStatus(NotificationStatus.FAILED);

        log.info("{} Found {} failed notification to retry",LOG_PREFIX,failedNotification.size());

        for (Notification notification : failedNotification){
            try{
                notification.setStatus(NotificationStatus.PENDING);
                notification.setFailureReason(null);
                notificationRepository.save(notification);

                processNotification(notification.getId());
                log.info("{} Retry successful for notification: {}", LOG_PREFIX, notification.getId());
            } catch (Exception e) {
                log.error("{} Retry failed for notification: {}", LOG_PREFIX, notification.getId(), e);
            }
        }
    }

//    **************************** Private Method
private void sendNotification(Notification notification) {
    // Simulate notification sending based on channel
    switch (notification.getChannel()) {
        case EMAIL:
            log.info("{} Sending EMAIL to: {}, subject: {}",
                    LOG_PREFIX, notification.getRecipient(), notification.getSubject());
            break;
        case SMS:
            log.info("{} Sending SMS to: {}, message: {}",
                    LOG_PREFIX, notification.getRecipient(), notification.getMessage());
            break;
        case PUSH:
            log.info("{} Sending PUSH to: {}, subject: {}",
                    LOG_PREFIX, notification.getRecipient(), notification.getSubject());
            break;
        case IN_APP:
            log.info("{} Creating IN_APP notification for userId: {}, subject: {}",
                    LOG_PREFIX, notification.getUserId(), notification.getSubject());
            break;
        default:
            throw new NotificationProcessingException("Unsupported notification channel: " + notification.getChannel());
    }
}
}
