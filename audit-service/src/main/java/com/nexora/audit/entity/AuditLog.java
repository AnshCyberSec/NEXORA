package com.nexora.audit.entity;

import com.nexora.audit.enums.AuditAction;
import com.nexora.audit.enums.AuditSource;
import com.nexora.audit.enums.AuditStatus;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "audit_log")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLog extends BaseEntity{

    @Column(nullable = false,length = 50)
    private String userId;

    @Column(nullable = false,length = 50)
    private String referenceId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false,length = 30)
    private AuditAction action;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false,length = 30)
    private AuditSource source;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false,length = 20)
    private AuditStatus status;

    @Column(length = 1000)
    private String details;

    @Column(length = 100)
    private String ipAddress;

    @Column(length = 100)
    private String userAgent;

    private String failureReason;
}
