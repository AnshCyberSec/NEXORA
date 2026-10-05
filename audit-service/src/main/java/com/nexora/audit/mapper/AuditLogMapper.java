package com.nexora.audit.mapper;

import com.nexora.audit.dto.request.CreateAuditRequest;
import com.nexora.audit.dto.response.AuditResponse;
import com.nexora.audit.entity.AuditLog;
import org.springframework.stereotype.Component;

@Component
public class AuditLogMapper {

    public AuditLog toEntity(CreateAuditRequest request) {
        return AuditLog.builder()
                .userId(request.getUserId())
                .referenceId(request.getReferenceId())
                .action(request.getAction())
                .source(request.getSource())
                .status(request.getStatus())
                .details(request.getDetails())
                .ipAddress(request.getIpAddress())
                .userAgent(request.getUserAgent())
                .failureReason(request.getFailureReason())
                .build();
    }

    public AuditResponse toResponse(AuditLog auditLog) {
        return AuditResponse.builder()
                .id(auditLog.getId())
                .userId(auditLog.getUserId())
                .referenceId(auditLog.getReferenceId())
                .action(auditLog.getAction())
                .source(auditLog.getSource())
                .status(auditLog.getStatus())
                .details(auditLog.getDetails())
                .ipAddress(auditLog.getIpAddress())
                .userAgent(auditLog.getUserAgent())
                .failureReason(auditLog.getFailureReason())
                .createdAt(auditLog.getCreatedAt())
                .updatedAt(auditLog.getUpdatedAt())
                .build();
    }
}