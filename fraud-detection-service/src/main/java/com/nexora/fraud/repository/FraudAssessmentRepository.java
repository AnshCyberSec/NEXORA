package com.nexora.fraud.repository;

import com.nexora.fraud.entity.FraudAssessment;
import com.nexora.fraud.enums.FraudDecision;
import com.nexora.fraud.enums.FraudStatus;
import com.nexora.fraud.enums.RiskLevel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface FraudAssessmentRepository extends JpaRepository<FraudAssessment, String> {

    Optional<FraudAssessment> findByReferenceId(String referenceId);

    List<FraudAssessment> findByUserIdOrderByCreatedAtDesc(String userId);

    List<FraudAssessment> findByTransactionId(String transactionId);

    List<FraudAssessment> findByDecision(FraudDecision decision);

    List<FraudAssessment> findByRiskLevel(RiskLevel riskLevel);

    List<FraudAssessment> findByStatus(FraudStatus status);

    List<FraudAssessment> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);

    long countByUserIdAndCreatedAtBetween(String userId, LocalDateTime startDate, LocalDateTime endDate);
}