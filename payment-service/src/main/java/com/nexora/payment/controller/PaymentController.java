package com.nexora.payment.controller;

import com.nexora.payment.common.ApiResponse;
import com.nexora.payment.dto.request.CreatePaymentRequest;
import com.nexora.payment.dto.request.PaymentStatusUpdateRequest;
import com.nexora.payment.dto.response.PaymentResponse;
import com.nexora.payment.dto.response.PaymentStatusResponse;
import com.nexora.payment.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    private static final String LOG_PREFIX = "[PAYMENT-CONTROLLER]";

    @PostMapping
    public ResponseEntity<ApiResponse<PaymentResponse>> createPayment(
            @Valid @RequestBody CreatePaymentRequest request) {
        log.info("{} Create payment request for userId: {}, referenceId: {}, amount: {}",
                LOG_PREFIX, request.getUserId(), request.getReferenceId(), request.getAmount());

        PaymentResponse response = paymentService.createPayment(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Payment created successfully"));
    }

    @GetMapping("/{paymentId}")
    public ResponseEntity<ApiResponse<PaymentResponse>> getPaymentById(
            @PathVariable String paymentId) {
        log.debug("{} Get payment by id: {}", LOG_PREFIX, paymentId);

        PaymentResponse response = paymentService.getPaymentById(paymentId);
        return ResponseEntity.ok(ApiResponse.success(response, "Payment fetched successfully"));
    }

    @GetMapping("/reference/{referenceId}")
    public ResponseEntity<ApiResponse<PaymentResponse>> getPaymentByReferenceId(
            @PathVariable String referenceId) {
        log.debug("{} Get payment by referenceId: {}", LOG_PREFIX, referenceId);

        PaymentResponse response = paymentService.getPaymentByReferenceId(referenceId);
        return ResponseEntity.ok(ApiResponse.success(response, "Payment fetched successfully"));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<PaymentResponse>>> getPaymentsByUserId(
            @PathVariable String userId) {
        log.debug("{} Get payments for userId: {}", LOG_PREFIX, userId);

        List<PaymentResponse> responses = paymentService.getPaymentsByUserId(userId);
        return ResponseEntity.ok(ApiResponse.success(responses, "Payments fetched successfully"));
    }

    @GetMapping("/wallet/{walletId}")
    public ResponseEntity<ApiResponse<List<PaymentResponse>>> getPaymentsByWalletId(
            @PathVariable String walletId) {
        log.debug("{} Get payments for walletId: {}", LOG_PREFIX, walletId);

        List<PaymentResponse> responses = paymentService.getPaymentsByWalletId(walletId);
        return ResponseEntity.ok(ApiResponse.success(responses, "Payments fetched successfully"));
    }

    @PatchMapping("/{paymentId}/status")
    public ResponseEntity<ApiResponse<PaymentStatusResponse>> updatePaymentStatus(
            @PathVariable String paymentId,
            @Valid @RequestBody PaymentStatusUpdateRequest request) {
        log.info("{} Update payment status for paymentId: {}, newStatus: {}",
                LOG_PREFIX, paymentId, request.getStatus());

        PaymentStatusResponse response = paymentService.updatePaymentStatus(paymentId, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Payment status updated successfully"));
    }

    @PostMapping("/{paymentId}/cancel")
    public ResponseEntity<ApiResponse<PaymentResponse>> cancelPayment(
            @PathVariable String paymentId) {
        log.info("{} Cancel payment for paymentId: {}", LOG_PREFIX, paymentId);

        PaymentResponse response = paymentService.cancelPayment(paymentId);
        return ResponseEntity.ok(ApiResponse.success(response, "Payment cancelled successfully"));
    }

    @PostMapping("/{paymentId}/process")
    public ResponseEntity<ApiResponse<PaymentResponse>> processPayment(
            @PathVariable String paymentId) {
        log.info("{} Process payment for paymentId: {}", LOG_PREFIX, paymentId);

        PaymentResponse response = paymentService.processPayment(paymentId);
        return ResponseEntity.ok(ApiResponse.success(response, "Payment processing started successfully"));
    }
}