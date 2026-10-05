package com.nexora.fraud.consumer;

import com.nexora.fraud.dto.request.FraudCheckRequest;
import com.nexora.fraud.dto.response.FraudCheckResponse;
import com.nexora.fraud.event.PaymentEvent;
import com.nexora.fraud.service.FraudDetectionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentEventConsumer {

    private final FraudDetectionService fraudDetectionService;

    private static final String LOG_PREFIX = "[FRAUD-CONSUMER]";

    @KafkaListener(
            topics = "payment-events",
            groupId = "fraud-group"
    )
    public void consumePaymentEvent(PaymentEvent event) {
        log.info("{} ═══════════════════════════════════════════════", LOG_PREFIX);
        log.info("{}    Received payment event from Kafka", LOG_PREFIX);
        log.info("{}    Payment ID: {}", LOG_PREFIX, event.getPaymentId());
        log.info("{}    User ID: {}", LOG_PREFIX, event.getUserId());
        log.info("{}    Amount: ₹{}", LOG_PREFIX, event.getAmount());
        log.info("{}    Event Type: {}", LOG_PREFIX, event.getEventType());
        log.info("{} ═══════════════════════════════════════════════", LOG_PREFIX);

        try {
            performFraudCheck(event);
            log.info("{}  Fraud check completed for payment: {}",
                    LOG_PREFIX, event.getPaymentId());
        } catch (Exception e) {
            log.error("{}  Failed to perform fraud check for payment: {}",
                    LOG_PREFIX, event.getPaymentId(), e);
        }
    }

    private void performFraudCheck(PaymentEvent event) {

        if (!"PAYMENT_CREATED".equals(event.getEventType())) {
            log.info("{} Skipping fraud check for event type: {}",
                    LOG_PREFIX, event.getEventType());
            return;
        }

        FraudCheckRequest request = FraudCheckRequest.builder()
                .userId(event.getUserId())
                .referenceId(event.getReferenceId())
                .transactionId(event.getPaymentId())
                .amount(event.getAmount())
                .build();

        FraudCheckResponse response = fraudDetectionService.checkFraud(request);

        log.info("{}  Fraud check result for payment: {}", LOG_PREFIX, event.getPaymentId());
        log.info("{}    Risk Score: {}", LOG_PREFIX, response.getRiskScore());
        log.info("{}    Risk Level: {}", LOG_PREFIX, response.getRiskLevel());
        log.info("{}    Decision: {}", LOG_PREFIX, response.getDecision());


        switch (response.getDecision()) {
            case BLOCK:
                log.warn("{}  FRAUD DETECTED - Payment {} BLOCKED. Risk: {}",
                        LOG_PREFIX, event.getPaymentId(), response.getRiskLevel());
                break;
            case REVIEW:
                log.warn("{}  Suspicious payment {} flagged for REVIEW. Risk: {}",
                        LOG_PREFIX, event.getPaymentId(), response.getRiskLevel());
                break;
            case ALLOW:
                log.info("{}  Payment {} cleared. Risk: {}",
                        LOG_PREFIX, event.getPaymentId(), response.getRiskLevel());
                break;
        }
    }
}