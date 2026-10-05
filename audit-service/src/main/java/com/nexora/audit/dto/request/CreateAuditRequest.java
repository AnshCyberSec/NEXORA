package com.nexora.audit.dto.request;

import com.nexora.audit.enums.AuditAction;
import com.nexora.audit.enums.AuditSource;
import com.nexora.audit.enums.AuditStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateAuditRequest {

    @NotBlank(message = "User ID is required")
    @Size(max = 50, message = "User ID must not exceed 50 characters")
    private String userId;

    @NotBlank(message = "Reference ID is required")
    @Size(max = 50,message = "Reference ID must not exceed 50 characters")
    private String referenceId;

    @NotNull(message = "Action is required")
    private AuditAction action;

    @NotNull(message = "Source is required")
    private AuditSource source;

    @NotNull(message = "Status is required")
    private AuditStatus status;

    @Size(max = 1000, message = "Details must not exceed 1000 characters")
    private String details;

    @Size(max = 100, message = "IP address must not exceed 100 characters")
    private String ipAddress;

    @Size(max = 100,message = "User agent must not exceed 100 characters")
    private String userAgent;

    @Size(max = 500, message = "Failure reason must not exceed 500 characters")
    private String failureReason;
}
