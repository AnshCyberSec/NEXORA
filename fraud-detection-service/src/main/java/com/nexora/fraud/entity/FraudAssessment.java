package com.nexora.fraud.entity;

import com.nexora.fraud.enums.FraudDecision;
import com.nexora.fraud.enums.FraudStatus;
import com.nexora.fraud.enums.RiskLevel;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "fraud_assessments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FraudAssessment extends BaseEntity {

    @Column(nullable = false, length = 50)
    private String userId;

    @Column(nullable = false, length = 50)
    private String referenceId;

    @Column(nullable = false, length = 50)
    private String transactionId;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false)
    private Integer riskScore;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RiskLevel riskLevel;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private FraudDecision decision;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private FraudStatus status = FraudStatus.PENDING;

    @Column(length = 1000)
    private String details;

    @Column(length = 100)
    private String ipAddress;

    @Column(length = 100)
    private String userAgent;

    private String failureReason;
}