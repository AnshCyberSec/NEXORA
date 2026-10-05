package com.nexora.fraud.mapper;

import com.nexora.fraud.dto.request.FraudCheckRequest;
import com.nexora.fraud.dto.response.FraudCheckResponse;
import com.nexora.fraud.entity.FraudAssessment;
import org.springframework.stereotype.Component;

@Component
public class FraudAssessmentMapper {

    public FraudAssessment toEntity(FraudCheckRequest request) {
        return FraudAssessment.builder()
                .userId(request.getUserId())
                .referenceId(request.getReferenceId())
                .transactionId(request.getTransactionId())
                .amount(request.getAmount())
                .ipAddress(request.getIpAddress())
                .userAgent(request.getUserAgent())
                .build();
    }

    public FraudCheckResponse toResponse(FraudAssessment assessment) {
        return FraudCheckResponse.builder()
                .id(assessment.getId())
                .userId(assessment.getUserId())
                .referenceId(assessment.getReferenceId())
                .transactionId(assessment.getTransactionId())
                .amount(assessment.getAmount())
                .riskScore(assessment.getRiskScore())
                .riskLevel(assessment.getRiskLevel())
                .decision(assessment.getDecision())
                .status(assessment.getStatus())
                .details(assessment.getDetails())
                .ipAddress(assessment.getIpAddress())
                .userAgent(assessment.getUserAgent())
                .failureReason(assessment.getFailureReason())
                .createdAt(assessment.getCreatedAt())
                .updatedAt(assessment.getUpdatedAt())
                .build();
    }
}