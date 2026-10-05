package com.nexora.fraud.dto.response;

import com.nexora.fraud.enums.FraudDecision;
import com.nexora.fraud.enums.FraudStatus;
import com.nexora.fraud.enums.RiskLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FraudCheckResponse {
    private String id;
    private String userId;
    private String referenceId;
    private String transactionId;
    private BigDecimal amount;
    private Integer riskScore;
    private RiskLevel riskLevel;
    private FraudDecision decision;
    private FraudStatus status;
    private String details;
    private String ipAddress;
    private String userAgent;
    private String failureReason;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}