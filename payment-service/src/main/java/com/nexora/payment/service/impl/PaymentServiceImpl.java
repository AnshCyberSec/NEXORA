package com.nexora.payment.service.impl;

import com.nexora.payment.client.WalletClient;
import com.nexora.payment.dto.request.CreatePaymentRequest;
import com.nexora.payment.dto.request.PaymentStatusUpdateRequest;
import com.nexora.payment.dto.request.WalletDebitRequest;
import com.nexora.payment.dto.response.PaymentResponse;
import com.nexora.payment.dto.response.PaymentStatusResponse;
import com.nexora.payment.dto.response.WalletDebitResponse;
import com.nexora.payment.entity.Payment;
import com.nexora.payment.enums.PaymentStatus;
import com.nexora.payment.event.PaymentEvent;
import com.nexora.payment.exception.InvalidPaymentStateException;
import com.nexora.payment.exception.PaymentAlreadyExistsException;
import com.nexora.payment.exception.PaymentNotFoundException;
import com.nexora.payment.mapper.PaymentMapper;
import com.nexora.payment.producer.PaymentEventProducer;
import com.nexora.payment.repository.PaymentRepository;
import com.nexora.payment.resilience.Resilience4jService;
import com.nexora.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentMapper paymentMapper;
    private final PaymentEventProducer paymentEventProducer;
    private final Resilience4jService resilience4jService;

    private static final String LOG_PREFIX = "[PAYMENT-SERVICE]";

    @Override
    public PaymentResponse createPayment(CreatePaymentRequest request) {
        log.info("{} Creating payment for userId: {}, referenceId: {}, amount: {}",
                LOG_PREFIX, request.getUserId(), request.getReferenceId(), request.getAmount());

        // Idempotency check
        if (paymentRepository.findByReferenceId(request.getReferenceId()).isPresent()) {
            log.warn("{} Payment already exists with referenceId: {}", LOG_PREFIX, request.getReferenceId());
            throw new PaymentAlreadyExistsException(request.getReferenceId());
        }

        // ✅ Call Wallet Service with Resilience4j
        log.info("{} Calling Wallet Service with resilience. walletId: {}, amount: {}",
                LOG_PREFIX, request.getWalletId(), request.getAmount());

        WalletDebitRequest walletRequest = WalletDebitRequest.builder()
                .referenceId("PAY-WALLET-" + request.getReferenceId())
                .amount(request.getAmount())
                .description("Payment debit: " + request.getReferenceId())
                .build();

        WalletDebitResponse walletResponse = resilience4jService
                .debitWalletWithResilience(request.getWalletId(), walletRequest);

        if (!walletResponse.isSuccess()) {
            log.warn("{} Wallet debit failed for referenceId: {}, reason: {}",
                    LOG_PREFIX, request.getReferenceId(), walletResponse.getMessage());
            throw new RuntimeException("Payment failed: " + walletResponse.getMessage());
        }

        log.info("{} Wallet debit successful. TransactionId: {}, BalanceAfter: ₹{}",
                LOG_PREFIX, walletResponse.getTransactionId(), walletResponse.getBalanceAfter());

        // Save payment
        Payment payment = paymentMapper.toEntity(request);
        Payment savedPayment = paymentRepository.save(payment);

        // Publish event to Kafka
        PaymentEvent event = PaymentEvent.builder()
                .paymentId(savedPayment.getId())
                .userId(savedPayment.getUserId())
                .walletId(savedPayment.getWalletId())
                .referenceId(savedPayment.getReferenceId())
                .amount(savedPayment.getAmount())
                .method(savedPayment.getMethod())
                .status(savedPayment.getStatus())
                .eventType("PAYMENT_CREATED")
                .eventTime(LocalDateTime.now())
                .build();

        paymentEventProducer.publishPaymentEvent(event);

        log.info("{} Payment created successfully with id: {}, referenceId: {}",
                LOG_PREFIX, savedPayment.getId(), savedPayment.getReferenceId());

        return paymentMapper.toResponse(savedPayment);
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentResponse getPaymentById(String paymentId) {
        log.debug("{} Fetching payment by id: {}", LOG_PREFIX, paymentId);

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> {
                    log.warn("{} Payment not found with id: {}", LOG_PREFIX, paymentId);
                    return new PaymentNotFoundException(paymentId);
                });

        return paymentMapper.toResponse(payment);
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentResponse getPaymentByReferenceId(String referenceId) {
        log.debug("{} Fetching payment by referenceId: {}", LOG_PREFIX, referenceId);

        Payment payment = paymentRepository.findByReferenceId(referenceId)
                .orElseThrow(() -> {
                    log.warn("{} Payment not found with referenceId: {}", LOG_PREFIX, referenceId);
                    return new PaymentNotFoundException("referenceId", referenceId);
                });

        return paymentMapper.toResponse(payment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentResponse> getPaymentsByUserId(String userId) {
        log.debug("{} Fetching payments for userId: {}", LOG_PREFIX, userId);

        List<Payment> payments = paymentRepository.findByUserIdOrderByCreatedAtDesc(userId);

        return payments.stream()
                .map(paymentMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentResponse> getPaymentsByWalletId(String walletId) {
        log.debug("{} Fetching payments for walletId: {}", LOG_PREFIX, walletId);

        List<Payment> payments = paymentRepository.findByWalletIdOrderByCreatedAtDesc(walletId);

        return payments.stream()
                .map(paymentMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public PaymentStatusResponse updatePaymentStatus(String paymentId, PaymentStatusUpdateRequest request) {
        log.info("{} Updating payment status for paymentId: {}, newStatus: {}",
                LOG_PREFIX, paymentId, request.getStatus());

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> {
                    log.warn("{} Payment not found with id: {}", LOG_PREFIX, paymentId);
                    return new PaymentNotFoundException(paymentId);
                });

        // Validate status transition
        validateStatusTransition(payment.getStatus(), request.getStatus());

        // Update status
        payment.setStatus(request.getStatus());

        if (request.getFailureReason() != null) {
            payment.setFailureReason(request.getFailureReason());
        }

        if (request.getTransactionId() != null) {
            payment.setTransactionId(request.getTransactionId());
        }

        try {
            Payment updatedPayment = paymentRepository.save(payment);


            PaymentEvent event = PaymentEvent.builder()
                    .paymentId(updatedPayment.getId())
                    .userId(updatedPayment.getUserId())
                    .walletId(updatedPayment.getWalletId())
                    .referenceId(updatedPayment.getReferenceId())
                    .amount(updatedPayment.getAmount())
                    .method(updatedPayment.getMethod())
                    .status(updatedPayment.getStatus())
                    .transactionId(updatedPayment.getTransactionId())
                    .eventType("PAYMENT_" + updatedPayment.getStatus().name())
                    .eventTime(LocalDateTime.now())
                    .build();

            paymentEventProducer.publishPaymentEvent(event);

            log.info("{} Payment status updated successfully for paymentId: {}, newStatus: {}",
                    LOG_PREFIX, paymentId, updatedPayment.getStatus());
            return paymentMapper.toStatusResponse(updatedPayment);
        } catch (OptimisticLockingFailureException e) {
            log.error("{} Optimistic lock failure for paymentId: {}", LOG_PREFIX, paymentId);
            throw new RuntimeException("Payment update failed due to concurrent modification", e);
        }
    }

    @Override
    public PaymentResponse cancelPayment(String paymentId) {
        log.info("{} Cancelling payment for paymentId: {}", LOG_PREFIX, paymentId);

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> {
                    log.warn("{} Payment not found with id: {}", LOG_PREFIX, paymentId);
                    return new PaymentNotFoundException(paymentId);
                });

        // Only PENDING payments can be canceled
        if (payment.getStatus() != PaymentStatus.PENDING) {
            log.warn("{} Cannot cancel payment {} with status: {}",
                    LOG_PREFIX, paymentId, payment.getStatus());
            throw new InvalidPaymentStateException(
                    String.format("Cannot cancel payment with status: %s. Only PENDING payments can be cancelled.",
                            payment.getStatus())
            );
        }

        payment.setStatus(PaymentStatus.CANCELLED);
        Payment cancelledPayment = paymentRepository.save(payment);

        log.info("{} Payment cancelled successfully for paymentId: {}", LOG_PREFIX, paymentId);
        return paymentMapper.toResponse(cancelledPayment);
    }

    @Override
    public PaymentResponse processPayment(String paymentId) {
        log.info("{} Processing payment for paymentId: {}", LOG_PREFIX, paymentId);

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> {
                    log.warn("{} Payment not found with id: {}", LOG_PREFIX, paymentId);
                    return new PaymentNotFoundException(paymentId);
                });

        // Only PENDING payments can be processed
        if (payment.getStatus() != PaymentStatus.PENDING) {
            log.warn("{} Cannot process payment {} with status: {}",
                    LOG_PREFIX, paymentId, payment.getStatus());
            throw new InvalidPaymentStateException(
                    String.format("Cannot process payment with status: %s. Only PENDING payments can be processed.",
                            payment.getStatus())
            );
        }

        payment.setStatus(PaymentStatus.PROCESSING);
        Payment processingPayment = paymentRepository.save(payment);

        log.info("{} Payment processing started for paymentId: {}", LOG_PREFIX, paymentId);
        return paymentMapper.toResponse(processingPayment);
    }

    //  Private Helper Methods

    private void validateStatusTransition(PaymentStatus currentStatus, PaymentStatus newStatus) {
        // If payment is in final state, cannot change
        if (isFinalState(currentStatus) && currentStatus != newStatus) {
            log.warn("{} Invalid status transition from {} to {} (final state)",
                    LOG_PREFIX, currentStatus, newStatus);
            throw new InvalidPaymentStateException(
                    String.format("Cannot change status from %s to %s. Payment is in final state.",
                            currentStatus, newStatus)
            );
        }

        // Validate specific transitions
        switch (currentStatus) {
            case PENDING:
                if (newStatus != PaymentStatus.PROCESSING &&
                        newStatus != PaymentStatus.CANCELLED &&
                        newStatus != PaymentStatus.FAILED) {
                    throw new InvalidPaymentStateException(
                            String.format("Invalid transition from %s to %s", currentStatus, newStatus)
                    );
                }
                break;
            case PROCESSING:
                if (newStatus != PaymentStatus.SUCCESS &&
                        newStatus != PaymentStatus.FAILED) {
                    throw new InvalidPaymentStateException(
                            String.format("Invalid transition from %s to %s", currentStatus, newStatus)
                    );
                }
                break;
            default:
                // Any other transition is invalid
                if (currentStatus != newStatus) {
                    throw new InvalidPaymentStateException(
                            String.format("Invalid transition from %s to %s", currentStatus, newStatus)
                    );
                }
        }
    }

    private boolean isFinalState(PaymentStatus status) {
        return status == PaymentStatus.SUCCESS ||
                status == PaymentStatus.FAILED ||
                status == PaymentStatus.REFUNDED ||
                status == PaymentStatus.CANCELLED;
    }
}