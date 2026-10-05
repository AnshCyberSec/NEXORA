package com.nexora.transaction.controller;

import com.nexora.transaction.common.ApiResponse;
import com.nexora.transaction.dto.request.CreateTransactionRequest;
import com.nexora.transaction.dto.response.TransactionResponse;
import com.nexora.transaction.enums.TransactionStatus;
import com.nexora.transaction.enums.TransactionType;
import com.nexora.transaction.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    private static final String LOG_PREFIX = "[TRANSACTION-CONTROLLER]";

    @PostMapping
    public ResponseEntity<ApiResponse<TransactionResponse>> createTransaction(
            @Valid @RequestBody CreateTransactionRequest request) {
        log.info("{} Create transaction for userId: {}, referenceId: {}, amount: {}",
                LOG_PREFIX, request.getUserId(), request.getReferenceId(), request.getAmount());

        TransactionResponse response = transactionService.createTransaction(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Transaction created successfully"));
    }

    @GetMapping("/{transactionId}")
    public ResponseEntity<ApiResponse<TransactionResponse>> getTransactionById(
            @PathVariable String transactionId) {
        log.debug("{} Get transaction by id: {}", LOG_PREFIX, transactionId);

        TransactionResponse response = transactionService.getTransactionById(transactionId);
        return ResponseEntity.ok(ApiResponse.success(response, "Transaction fetched successfully"));
    }

    @GetMapping("/reference/{referenceId}")
    public ResponseEntity<ApiResponse<TransactionResponse>> getTransactionByReferenceId(
            @PathVariable String referenceId) {
        log.debug("{} Get transaction by referenceId: {}", LOG_PREFIX, referenceId);

        TransactionResponse response = transactionService.getTransactionByReferenceId(referenceId);
        return ResponseEntity.ok(ApiResponse.success(response, "Transaction fetched successfully"));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<TransactionResponse>>> getTransactionsByUserId(
            @PathVariable String userId) {
        log.debug("{} Get transactions for userId: {}", LOG_PREFIX, userId);

        List<TransactionResponse> responses = transactionService.getTransactionsByUserId(userId);
        return ResponseEntity.ok(ApiResponse.success(responses, "Transactions fetched successfully"));
    }

    @GetMapping("/wallet/{walletId}")
    public ResponseEntity<ApiResponse<List<TransactionResponse>>> getTransactionsByWalletId(
            @PathVariable String walletId) {
        log.debug("{} Get transactions for walletId: {}", LOG_PREFIX, walletId);

        List<TransactionResponse> responses = transactionService.getTransactionsByWalletId(walletId);
        return ResponseEntity.ok(ApiResponse.success(responses, "Transactions fetched successfully"));
    }

    @GetMapping("/user/{userId}/type/{type}")
    public ResponseEntity<ApiResponse<List<TransactionResponse>>> getTransactionsByUserIdAndType(
            @PathVariable String userId,
            @PathVariable TransactionType type) {
        log.debug("{} Get transactions for userId: {}, type: {}", LOG_PREFIX, userId, type);

        List<TransactionResponse> responses = transactionService.getTransactionsByUserIdAndType(userId, type);
        return ResponseEntity.ok(ApiResponse.success(responses, "Transactions fetched successfully"));
    }

    @PatchMapping("/{transactionId}/status")
    public ResponseEntity<ApiResponse<TransactionResponse>> updateTransactionStatus(
            @PathVariable String transactionId,
            @RequestParam TransactionStatus status,
            @RequestParam(required = false) String failureReason) {
        log.info("{} Update transaction status for transactionId: {}, newStatus: {}",
                LOG_PREFIX, transactionId, status);

        TransactionResponse response = transactionService.updateTransactionStatus(
                transactionId, status, failureReason);
        return ResponseEntity.ok(ApiResponse.success(response, "Transaction status updated successfully"));
    }

    @GetMapping("/exists/reference/{referenceId}")
    public ResponseEntity<ApiResponse<Boolean>> existsByReferenceId(
            @PathVariable String referenceId) {
        log.debug("{} Check if transaction exists with referenceId: {}", LOG_PREFIX, referenceId);

        boolean exists = transactionService.existsByReferenceId(referenceId);
        return ResponseEntity.ok(ApiResponse.success(exists, "Transaction existence checked"));
    }
}