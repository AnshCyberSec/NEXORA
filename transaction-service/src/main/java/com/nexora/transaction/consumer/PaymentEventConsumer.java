package com.nexora.transaction.consumer;

import com.nexora.transaction.dto.request.CreateTransactionRequest;
import com.nexora.transaction.enums.TransactionSource;
import com.nexora.transaction.enums.TransactionStatus;
import com.nexora.transaction.enums.TransactionType;
import com.nexora.transaction.event.PaymentEvent;
import com.nexora.transaction.service.TransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentEventConsumer {

    private final TransactionService transactionService;

    private static final String LOG_PREFIX = "[TRANSACTION-CONSUMER]";

    @KafkaListener(
            topics = "payment-events",
            groupId = "transaction-group"
    )
    public void consumePaymentEvent(PaymentEvent event) {
        log.info("{} ═══════════════════════════════════════════════", LOG_PREFIX);
        log.info("{}  Received payment event from Kafka", LOG_PREFIX);
        log.info("{}    Payment ID: {}", LOG_PREFIX, event.getPaymentId());
        log.info("{}    User ID: {}", LOG_PREFIX, event.getUserId());
        log.info("{}    Amount: ₹{}", LOG_PREFIX, event.getAmount());
        log.info("{}    Event Type: {}", LOG_PREFIX, event.getEventType());
        log.info("{} ═══════════════════════════════════════════════", LOG_PREFIX);

        try {

            if (!"PAYMENT_SUCCESS".equals(event.getEventType())) {
                log.info("{} Skipping ledger entry for event type: {}",
                        LOG_PREFIX, event.getEventType());
                return;
            }

            createLedgerEntry(event);
            log.info("{}  Transaction ledger entry created for payment: {}",
                    LOG_PREFIX, event.getPaymentId());

        } catch (Exception e) {
            log.error("{}  Failed to create ledger entry for payment: {}",
                    LOG_PREFIX, event.getPaymentId(), e);
        }
    }

    private void createLedgerEntry(PaymentEvent event) {
        // Reference ID for transaction - use paymentId to ensure idempotency
        String transactionReferenceId = "TXN-PAY-" + event.getPaymentId();

        CreateTransactionRequest request = CreateTransactionRequest.builder()
                .userId(event.getUserId())
                .walletId(event.getWalletId())
                .referenceId(transactionReferenceId)
                .sourceId(event.getPaymentId())
                .type(TransactionType.DEBIT)
                .source(TransactionSource.PAYMENT)
                .amount(event.getAmount())
                .balanceBefore(event.getAmount())
                .description(String.format(
                        "Payment transaction for paymentId: %s, referenceId: %s",
                        event.getPaymentId(), event.getReferenceId()
                ))
                .build();

        var response = transactionService.createTransaction(request);

        log.info("{}  Ledger entry created: transactionId={}, referenceId={}, amount=₹{}",
                LOG_PREFIX, response.getId(), response.getReferenceId(), response.getAmount());
    }
}