package com.nexora.fraud.controller;

import com.nexora.fraud.common.ApiResponse;
import com.nexora.fraud.dto.request.FraudCheckRequest;
import com.nexora.fraud.dto.response.FraudCheckResponse;
import com.nexora.fraud.enums.FraudDecision;
import com.nexora.fraud.enums.RiskLevel;
import com.nexora.fraud.service.FraudDetectionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/fraud")
@RequiredArgsConstructor
public class FraudDetectionController {

    private final FraudDetectionService fraudDetectionService;

    private static final String LOG_PREFIX = "[FRAUD-CONTROLLER]";

    @PostMapping("/check")
    public ResponseEntity<ApiResponse<FraudCheckResponse>> checkFraud(
            @Valid @RequestBody FraudCheckRequest request) {
        log.info("{} Fraud check request for userId: {}, referenceId: {}, amount: {}",
                LOG_PREFIX, request.getUserId(), request.getReferenceId(), request.getAmount());

        FraudCheckResponse response = fraudDetectionService.checkFraud(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Fraud check completed successfully"));
    }

    @GetMapping("/{assessmentId}")
    public ResponseEntity<ApiResponse<FraudCheckResponse>> getFraudAssessmentById(
            @PathVariable String assessmentId) {
        log.debug("{} Get fraud assessment by id: {}", LOG_PREFIX, assessmentId);

        FraudCheckResponse response = fraudDetectionService.getFraudAssessmentById(assessmentId);
        return ResponseEntity.ok(ApiResponse.success(response, "Fraud assessment fetched successfully"));
    }

    @GetMapping("/reference/{referenceId}")
    public ResponseEntity<ApiResponse<FraudCheckResponse>> getFraudAssessmentByReferenceId(
            @PathVariable String referenceId) {
        log.debug("{} Get fraud assessment by referenceId: {}", LOG_PREFIX, referenceId);

        FraudCheckResponse response = fraudDetectionService.getFraudAssessmentByReferenceId(referenceId);
        return ResponseEntity.ok(ApiResponse.success(response, "Fraud assessment fetched successfully"));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<FraudCheckResponse>>> getFraudAssessmentsByUserId(
            @PathVariable String userId) {
        log.debug("{} Get fraud assessments for userId: {}", LOG_PREFIX, userId);

        List<FraudCheckResponse> responses = fraudDetectionService.getFraudAssessmentsByUserId(userId);
        return ResponseEntity.ok(ApiResponse.success(responses, "Fraud assessments fetched successfully"));
    }

    @GetMapping("/decision/{decision}")
    public ResponseEntity<ApiResponse<List<FraudCheckResponse>>> getFraudAssessmentsByDecision(
            @PathVariable FraudDecision decision) {
        log.debug("{} Get fraud assessments by decision: {}", LOG_PREFIX, decision);

        List<FraudCheckResponse> responses = fraudDetectionService.getFraudAssessmentsByDecision(decision);
        return ResponseEntity.ok(ApiResponse.success(responses, "Fraud assessments fetched successfully"));
    }

    @GetMapping("/risk-level/{riskLevel}")
    public ResponseEntity<ApiResponse<List<FraudCheckResponse>>> getFraudAssessmentsByRiskLevel(
            @PathVariable RiskLevel riskLevel) {
        log.debug("{} Get fraud assessments by risk level: {}", LOG_PREFIX, riskLevel);

        List<FraudCheckResponse> responses = fraudDetectionService.getFraudAssessmentsByRiskLevel(riskLevel);
        return ResponseEntity.ok(ApiResponse.success(responses, "Fraud assessments fetched successfully"));
    }

    @GetMapping("/date-range")
    public ResponseEntity<ApiResponse<List<FraudCheckResponse>>> getFraudAssessmentsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        log.debug("{} Get fraud assessments between {} and {}", LOG_PREFIX, startDate, endDate);

        List<FraudCheckResponse> responses = fraudDetectionService.getFraudAssessmentsByDateRange(startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success(responses, "Fraud assessments fetched successfully"));
    }
}