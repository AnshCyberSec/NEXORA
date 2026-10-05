package com.nexora.notification.repository;

import com.nexora.notification.entity.Notification;
import com.nexora.notification.enums.NotificationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, String> {

    List<Notification> findByUserIdOrderByCreatedAtDesc(String userId);

    List<Notification> findByUserIdAndStatus(String userId, NotificationStatus status);

    List<Notification> findByReferenceId(String referenceId);

    List<Notification> findByStatus(NotificationStatus status);
}