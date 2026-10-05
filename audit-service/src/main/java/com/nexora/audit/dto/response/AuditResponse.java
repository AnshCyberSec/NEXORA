package com.nexora.audit.dto.response;

import com.nexora.audit.enums.AuditAction;
import com.nexora.audit.enums.AuditSource;
import com.nexora.audit.enums.AuditStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditResponse {
    private String id;
    private String userId;
    private String referenceId;
    private AuditAction action;
    private AuditSource source;
    private AuditStatus status;
    private String details;
    private String ipAddress;
    private String userAgent;
    private String failureReason;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
