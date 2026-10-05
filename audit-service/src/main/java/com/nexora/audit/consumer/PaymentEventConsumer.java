package com.nexora.audit.consumer;

import com.nexora.audit.dto.request.CreateAuditRequest;
import com.nexora.audit.enums.AuditAction;
import com.nexora.audit.enums.AuditSource;
import com.nexora.audit.enums.AuditStatus;
import com.nexora.audit.event.PaymentEvent;
import com.nexora.audit.service.AuditService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentEventConsumer {

    private final AuditService auditService;

    private static final String LOG_PREFIX = "[AUDIT-CONSUMER]";

    @KafkaListener(
            topics = "payment-events",
            groupId = "audit-group"
    )
    public void consumePaymentEvent(PaymentEvent event) {
        log.info("{} ═══════════════════════════════════════════════", LOG_PREFIX);
        log.info("{}   Received payment event from Kafka", LOG_PREFIX);
        log.info("{}    Payment ID: {}", LOG_PREFIX, event.getPaymentId());
        log.info("{}    User ID: {}", LOG_PREFIX, event.getUserId());
        log.info("{}    Amount: ₹{}", LOG_PREFIX, event.getAmount());
        log.info("{}    Event Type: {}", LOG_PREFIX, event.getEventType());
        log.info("{} ═══════════════════════════════════════════════", LOG_PREFIX);

        try {
            saveAuditLog(event);
            log.info("{}  Audit log saved for payment: {}",
                    LOG_PREFIX, event.getPaymentId());
        } catch (Exception e) {
            log.error("{}  Failed to save audit log for payment: {}",
                    LOG_PREFIX, event.getPaymentId(), e);
        }
    }

    private void saveAuditLog(PaymentEvent event) {
        AuditAction action = mapEventTypeToAction(event.getEventType());
        AuditStatus status = "PAYMENT_FAILED".equals(event.getEventType())
                ? AuditStatus.FAILURE
                : AuditStatus.SUCCESS;

        CreateAuditRequest request = CreateAuditRequest.builder()
                .userId(event.getUserId())
                .referenceId(event.getReferenceId())
                .action(action)
                .source(AuditSource.PAYMENT_SERVICE)
                .status(status)
                .details(String.format(
                        "Payment event received from Kafka. PaymentId: %s, Amount: ₹%s, EventType: %s",
                        event.getPaymentId(), event.getAmount(), event.getEventType()
                ))
                .build();

        auditService.createAuditLog(request);

        log.info("{}  Audit log created: action={}, status={}, referenceId={}",
                LOG_PREFIX, action, status, event.getReferenceId());
    }

    private AuditAction mapEventTypeToAction(String eventType) {
        if (eventType == null) {
            return AuditAction.PAYMENT_CREATED;
        }

        switch (eventType) {
            case "PAYMENT_CREATED":
                return AuditAction.PAYMENT_CREATED;
            case "PAYMENT_PROCESSING":
                return AuditAction.PAYMENT_PROCESSED;
            case "PAYMENT_SUCCESS":
                return AuditAction.PAYMENT_SUCCESS;
            case "PAYMENT_FAILED":
                return AuditAction.PAYMENT_FAILED;
            case "PAYMENT_CANCELLED":
                return AuditAction.PAYMENT_CANCELLED;
            case "PAYMENT_REFUNDED":
                return AuditAction.PAYMENT_REFUNDED;
            default:
                return AuditAction.PAYMENT_CREATED;
        }
    }
}