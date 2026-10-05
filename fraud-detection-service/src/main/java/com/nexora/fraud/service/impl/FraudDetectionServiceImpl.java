package com.nexora.fraud.service.impl;

import com.nexora.fraud.dto.request.FraudCheckRequest;
import com.nexora.fraud.dto.response.FraudCheckResponse;
import com.nexora.fraud.entity.FraudAssessment;
import com.nexora.fraud.enums.FraudDecision;
import com.nexora.fraud.enums.FraudStatus;
import com.nexora.fraud.enums.RiskLevel;
import com.nexora.fraud.exception.FraudAssessmentNotFoundException;
import com.nexora.fraud.mapper.FraudAssessmentMapper;
import com.nexora.fraud.repository.FraudAssessmentRepository;
import com.nexora.fraud.service.FraudDetectionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class FraudDetectionServiceImpl implements FraudDetectionService {

    private final FraudAssessmentRepository fraudAssessmentRepository;
    private final FraudAssessmentMapper fraudAssessmentMapper;

    private static final String LOG_PREFIX = "[FRAUD-SERVICE]";

    // Risk thresholds
    private static final int LOW_RISK_THRESHOLD = 25;
    private static final int MEDIUM_RISK_THRESHOLD = 50;
    private static final int HIGH_RISK_THRESHOLD = 75;

    // Rule thresholds
    private static final BigDecimal HIGH_AMOUNT_THRESHOLD = new BigDecimal("10000");
    private static final BigDecimal VERY_HIGH_AMOUNT_THRESHOLD = new BigDecimal("50000");
    private static final int VELOCITY_LIMIT = 5; // Max transactions per hour
    private static final int FREQUENCY_LIMIT = 20; // Max transactions per day

    @Override
    public FraudCheckResponse checkFraud(FraudCheckRequest request) {
        log.info("{} Fraud check initiated for userId: {}, referenceId: {}, amount: {}",
                LOG_PREFIX, request.getUserId(), request.getReferenceId(), request.getAmount());

        // Check for duplicate assessment (idempotency)
        if (fraudAssessmentRepository.findByReferenceId(request.getReferenceId()).isPresent()) {
            log.info("{} Fraud assessment already exists for referenceId: {}",
                    LOG_PREFIX, request.getReferenceId());
            FraudAssessment existing = fraudAssessmentRepository.findByReferenceId(request.getReferenceId())
                    .orElseThrow(() -> new FraudAssessmentNotFoundException("referenceId", request.getReferenceId()));
            return fraudAssessmentMapper.toResponse(existing);
        }

        try {
            // Create assessment entity
            FraudAssessment assessment = fraudAssessmentMapper.toEntity(request);

            // Calculate risk score
            int riskScore = calculateRiskScore(request);
            RiskLevel riskLevel = determineRiskLevel(riskScore);
            FraudDecision decision = determineDecision(riskLevel, riskScore);

            // Set assessment fields
            assessment.setRiskScore(riskScore);
            assessment.setRiskLevel(riskLevel);
            assessment.setDecision(decision);
            assessment.setStatus(FraudStatus.COMPLETED);
            assessment.setDetails(buildDetails(request, riskScore, riskLevel, decision));

            FraudAssessment savedAssessment = fraudAssessmentRepository.save(assessment);

            log.info("{} Fraud check completed for referenceId: {}, riskScore: {}, riskLevel: {}, decision: {}",
                    LOG_PREFIX, request.getReferenceId(), riskScore, riskLevel, decision);

            return fraudAssessmentMapper.toResponse(savedAssessment);

        } catch (Exception e) {
            log.error("{} Fraud check failed for referenceId: {}",
                    LOG_PREFIX, request.getReferenceId(), e);

            // Save failed assessment
            FraudAssessment failedAssessment = fraudAssessmentMapper.toEntity(request);
            failedAssessment.setStatus(FraudStatus.FAILED);
            failedAssessment.setRiskScore(0);
            failedAssessment.setRiskLevel(RiskLevel.HIGH);
            failedAssessment.setDecision(FraudDecision.REVIEW);
            failedAssessment.setFailureReason(e.getMessage());
            fraudAssessmentRepository.save(failedAssessment);

            throw e;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public FraudCheckResponse getFraudAssessmentById(String assessmentId) {
        log.debug("{} Fetching fraud assessment by id: {}", LOG_PREFIX, assessmentId);

        FraudAssessment assessment = fraudAssessmentRepository.findById(assessmentId)
                .orElseThrow(() -> {
                    log.warn("{} Fraud assessment not found with id: {}", LOG_PREFIX, assessmentId);
                    return new FraudAssessmentNotFoundException(assessmentId);
                });

        return fraudAssessmentMapper.toResponse(assessment);
    }

    @Override
    @Transactional(readOnly = true)
    public FraudCheckResponse getFraudAssessmentByReferenceId(String referenceId) {
        log.debug("{} Fetching fraud assessment by referenceId: {}", LOG_PREFIX, referenceId);

        FraudAssessment assessment = fraudAssessmentRepository.findByReferenceId(referenceId)
                .orElseThrow(() -> {
                    log.warn("{} Fraud assessment not found with referenceId: {}", LOG_PREFIX, referenceId);
                    return new FraudAssessmentNotFoundException("referenceId", referenceId);
                });

        return fraudAssessmentMapper.toResponse(assessment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FraudCheckResponse> getFraudAssessmentsByUserId(String userId) {
        log.debug("{} Fetching fraud assessments for userId: {}", LOG_PREFIX, userId);

        List<FraudAssessment> assessments = fraudAssessmentRepository.findByUserIdOrderByCreatedAtDesc(userId);

        return assessments.stream()
                .map(fraudAssessmentMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<FraudCheckResponse> getFraudAssessmentsByDecision(FraudDecision decision) {
        log.debug("{} Fetching fraud assessments by decision: {}", LOG_PREFIX, decision);

        List<FraudAssessment> assessments = fraudAssessmentRepository.findByDecision(decision);

        return assessments.stream()
                .map(fraudAssessmentMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<FraudCheckResponse> getFraudAssessmentsByRiskLevel(RiskLevel riskLevel) {
        log.debug("{} Fetching fraud assessments by risk level: {}", LOG_PREFIX, riskLevel);

        List<FraudAssessment> assessments = fraudAssessmentRepository.findByRiskLevel(riskLevel);

        return assessments.stream()
                .map(fraudAssessmentMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<FraudCheckResponse> getFraudAssessmentsByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        log.debug("{} Fetching fraud assessments between {} and {}", LOG_PREFIX, startDate, endDate);

        List<FraudAssessment> assessments = fraudAssessmentRepository.findByCreatedAtBetween(startDate, endDate);

        return assessments.stream()
                .map(fraudAssessmentMapper::toResponse)
                .collect(Collectors.toList());
    }

    // ============ Private Helper Methods ============

    private int calculateRiskScore(FraudCheckRequest request) {
        int score = 0;
        List<String> triggeredRules = new ArrayList<>();

        // Rule 1: Amount-based risk
        if (request.getAmount().compareTo(VERY_HIGH_AMOUNT_THRESHOLD) >= 0) {
            score += 40;
            triggeredRules.add("VERY_HIGH_AMOUNT");
            log.debug("{} Rule triggered: VERY_HIGH_AMOUNT for amount: {}", LOG_PREFIX, request.getAmount());
        } else if (request.getAmount().compareTo(HIGH_AMOUNT_THRESHOLD) >= 0) {
            score += 20;
            triggeredRules.add("HIGH_AMOUNT");
            log.debug("{} Rule triggered: HIGH_AMOUNT for amount: {}", LOG_PREFIX, request.getAmount());
        }

        // Rule 2: Velocity check (transactions in last hour)
        LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(1);
        long recentTransactions = fraudAssessmentRepository.countByUserIdAndCreatedAtBetween(
                request.getUserId(), oneHourAgo, LocalDateTime.now());

        if (recentTransactions >= VELOCITY_LIMIT) {
            score += 25;
            triggeredRules.add("HIGH_VELOCITY");
            log.debug("{} Rule triggered: HIGH_VELOCITY for userId: {}, count: {}",
                    LOG_PREFIX, request.getUserId(), recentTransactions);
        }

        // Rule 3: Frequency check (transactions in last day)
        LocalDateTime oneDayAgo = LocalDateTime.now().minusDays(1);
        long dailyTransactions = fraudAssessmentRepository.countByUserIdAndCreatedAtBetween(
                request.getUserId(), oneDayAgo, LocalDateTime.now());

        if (dailyTransactions >= FREQUENCY_LIMIT) {
            score += 15;
            triggeredRules.add("HIGH_FREQUENCY");
            log.debug("{} Rule triggered: HIGH_FREQUENCY for userId: {}, count: {}",
                    LOG_PREFIX, request.getUserId(), dailyTransactions);
        }

        // Cap score at 100
        return Math.min(score, 100);
    }

    private RiskLevel determineRiskLevel(int riskScore) {
        if (riskScore >= HIGH_RISK_THRESHOLD) {
            return RiskLevel.CRITICAL;
        } else if (riskScore >= MEDIUM_RISK_THRESHOLD) {
            return RiskLevel.HIGH;
        } else if (riskScore >= LOW_RISK_THRESHOLD) {
            return RiskLevel.MEDIUM;
        } else {
            return RiskLevel.LOW;
        }
    }

    private FraudDecision determineDecision(RiskLevel riskLevel, int riskScore) {
        switch (riskLevel) {
            case CRITICAL:
                return FraudDecision.BLOCK;
            case HIGH:
                return FraudDecision.REVIEW;
            case MEDIUM:
                return riskScore >= 40 ? FraudDecision.REVIEW : FraudDecision.ALLOW;
            case LOW:
            default:
                return FraudDecision.ALLOW;
        }
    }

    private String buildDetails(FraudCheckRequest request, int riskScore, RiskLevel riskLevel, FraudDecision decision) {
        return String.format(
                "Fraud check completed. Amount: %s, Risk Score: %d, Risk Level: %s, Decision: %s",
                request.getAmount(), riskScore, riskLevel, decision
        );
    }
}