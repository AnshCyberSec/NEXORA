package com.nexora.audit.repository;

import com.nexora.audit.entity.AuditLog;
import com.nexora.audit.enums.AuditAction;
import com.nexora.audit.enums.AuditSource;
import com.nexora.audit.enums.AuditStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, String> {
    List<AuditLog> findByUserIdOrderByCreatedAtDesc(String userId);

    List<AuditLog> findByReferenceId(String referenceId);

    List<AuditLog> findByAction(AuditAction action);

    List<AuditLog> findBySource(AuditSource source);

    List<AuditLog> findByStatus(AuditStatus status);

    List<AuditLog> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);

    List<AuditLog> findByUserIdAndAction(String userId,AuditAction action);
}
