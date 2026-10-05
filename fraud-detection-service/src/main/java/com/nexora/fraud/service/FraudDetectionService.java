package com.nexora.fraud.service;

import com.nexora.fraud.dto.request.FraudCheckRequest;
import com.nexora.fraud.dto.response.FraudCheckResponse;
import com.nexora.fraud.enums.FraudDecision;
import com.nexora.fraud.enums.RiskLevel;

import java.time.LocalDateTime;
import java.util.List;

public interface FraudDetectionService {

    FraudCheckResponse checkFraud(FraudCheckRequest request);

    FraudCheckResponse getFraudAssessmentById(String assessmentId);

    FraudCheckResponse getFraudAssessmentByReferenceId(String referenceId);

    List<FraudCheckResponse> getFraudAssessmentsByUserId(String userId);

    List<FraudCheckResponse> getFraudAssessmentsByDecision(FraudDecision decision);

    List<FraudCheckResponse> getFraudAssessmentsByRiskLevel(RiskLevel riskLevel);

    List<FraudCheckResponse> getFraudAssessmentsByDateRange(LocalDateTime startDate, LocalDateTime endDate);
}