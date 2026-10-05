package com.nexora.audit.service;

import com.nexora.audit.dto.request.CreateAuditRequest;
import com.nexora.audit.dto.response.AuditResponse;
import com.nexora.audit.enums.AuditAction;
import com.nexora.audit.enums.AuditSource;
import com.nexora.audit.enums.AuditStatus;

import java.time.LocalDateTime;
import java.util.List;

public interface AuditService {

    AuditResponse createAuditLog(CreateAuditRequest request);

    AuditResponse getAuditLogById(String auditLogId);

    List<AuditResponse> getAuditLogsByUserId(String userId);

    List<AuditResponse> getAuditLogsByReferenceId(String referenceId);

    List<AuditResponse> getAuditLogsByAction(AuditAction action);

    List<AuditResponse> getAuditLogsBySource(AuditSource source);

    List<AuditResponse> getAuditLogsByStatus(AuditStatus status);

    List<AuditResponse> getAuditLogsByDateRange(LocalDateTime startDate, LocalDateTime endDate);

    List<AuditResponse> getAuditLogsByUserIdAndAction(String userId, AuditAction action);
}