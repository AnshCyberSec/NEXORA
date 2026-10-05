package com.nexora.notification.consumer;

import com.nexora.notification.event.PaymentEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentEventConsumer {

    private static final String LOG_PREFIX = "[NOTIFICATION-CONSUMER]";

    @KafkaListener(
            topics = "payment-events",
            groupId = "notification-group"
    )
    public void consumePaymentEvent(PaymentEvent event) {
        log.info("{} ═══════════════════════════════════════════════", LOG_PREFIX);
        log.info("{} 📩 Received payment event from Kafka", LOG_PREFIX);
        log.info("{}    Payment ID: {}", LOG_PREFIX, event.getPaymentId());
        log.info("{}    User ID: {}", LOG_PREFIX, event.getUserId());
        log.info("{}    Amount: ₹{}", LOG_PREFIX, event.getAmount());
        log.info("{}    Status: {}", LOG_PREFIX, event.getStatus());
        log.info("{}    Event Type: {}", LOG_PREFIX, event.getEventType());
        log.info("{}    Event Time: {}", LOG_PREFIX, event.getEventTime());
        log.info("{} ═══════════════════════════════════════════════", LOG_PREFIX);

        try {
            processNotification(event);
            log.info("{}  Payment event processed successfully: {}",
                    LOG_PREFIX, event.getPaymentId());
        } catch (Exception e) {
            log.error("{}  Failed to process payment event: {}",
                    LOG_PREFIX, event.getPaymentId(), e);
        }
    }

    private void processNotification(PaymentEvent event) {
        String eventType = event.getEventType();

        switch (eventType) {
            case "PAYMENT_CREATED":
                log.info("{}  Sending 'Payment Initiated' notification to user: {}",
                        LOG_PREFIX, event.getUserId());
                // TODO: NotificationService.createNotification() call karo
                break;

            case "PAYMENT_SUCCESS":
                log.info("{}  Sending 'Payment Success' email to user: {}",
                        LOG_PREFIX, event.getUserId());
                // TODO: Success notification create karo
                break;

            case "PAYMENT_FAILED":
                log.info("{}  Sending 'Payment Failed' SMS to user: {}",
                        LOG_PREFIX, event.getUserId());
                // TODO: Failure notification create karo
                break;

            case "PAYMENT_CANCELLED":
                log.info("{}  Sending 'Payment Cancelled' notification to user: {}",
                        LOG_PREFIX, event.getUserId());
                break;

            case "PAYMENT_REFUNDED":
                log.info("{}  Sending 'Payment Refunded' notification to user: {}",
                        LOG_PREFIX, event.getUserId());
                break;

            case "PAYMENT_PROCESSING":
                log.info("{}  Payment is being processed: {}",
                        LOG_PREFIX, event.getPaymentId());
                break;

            default:
                log.info("{}  Generic notification for event: {}",
                        LOG_PREFIX, eventType);
        }
    }
}