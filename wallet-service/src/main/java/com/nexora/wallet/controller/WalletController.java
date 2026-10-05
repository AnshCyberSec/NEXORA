package com.nexora.wallet.controller;

import com.nexora.wallet.common.ApiResponse;
import com.nexora.wallet.dto.request.DepositRequest;
import com.nexora.wallet.dto.request.WithdrawRequest;
import com.nexora.wallet.dto.response.TransactionResponse;
import com.nexora.wallet.dto.response.WalletResponse;
import com.nexora.wallet.service.WalletService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/wallets")
@RequiredArgsConstructor
public class WalletController {

    private final WalletService walletService;

    private static final String LOG_PREFIX = "[WALLET-CONTROLLER]";

    @PostMapping
    public ResponseEntity<ApiResponse<WalletResponse>> createWallet(
            @RequestHeader("X-User-Id") String userId) {
        log.info("{} Create wallet request for userId: {}", LOG_PREFIX, userId);

        WalletResponse response = walletService.createWallet(userId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Wallet created successfully"));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<WalletResponse>> getWalletByUserId(
            @PathVariable String userId) {
        log.debug("{} Get wallet for userId: {}", LOG_PREFIX, userId);

        WalletResponse response = walletService.getWalletByUserId(userId);
        return ResponseEntity.ok(ApiResponse.success(response, "Wallet fetched successfully"));
    }

    @GetMapping("/{walletId}")
    public ResponseEntity<ApiResponse<WalletResponse>> getWalletById(
            @PathVariable String walletId) {
        log.debug("{} Get wallet by id: {}", LOG_PREFIX, walletId);

        WalletResponse response = walletService.getWalletById(walletId);
        return ResponseEntity.ok(ApiResponse.success(response, "Wallet fetched successfully"));
    }

    @GetMapping("/{walletId}/balance")
    public ResponseEntity<ApiResponse<BigDecimal>> getBalance(
            @PathVariable String walletId) {
        log.debug("{} Get balance for walletId: {}", LOG_PREFIX, walletId);

        WalletResponse wallet = walletService.getWalletById(walletId);
        return ResponseEntity.ok(ApiResponse.success(wallet.getBalance(), "Balance fetched successfully"));
    }

    @PostMapping("/{walletId}/deposit")
    public ResponseEntity<ApiResponse<TransactionResponse>> deposit(
            @PathVariable String walletId,
            @Valid @RequestBody DepositRequest request) {
        log.info("{} Deposit request for walletId: {}, amount: {}",
                LOG_PREFIX, walletId, request.getAmount());

        TransactionResponse response = walletService.deposit(request, walletId);
        return ResponseEntity.ok(ApiResponse.success(response, "Deposit successful"));
    }

    @PostMapping("/{walletId}/withdraw")
    public ResponseEntity<ApiResponse<TransactionResponse>> withdraw(
            @PathVariable String walletId,
            @Valid @RequestBody WithdrawRequest request) {
        log.info("{} Withdraw request for walletId: {}, amount: {}",
                LOG_PREFIX, walletId, request.getAmount());

        TransactionResponse response = walletService.withdraw(request, walletId);
        return ResponseEntity.ok(ApiResponse.success(response, "Withdrawal successful"));
    }

    @GetMapping("/{walletId}/transactions")
    public ResponseEntity<ApiResponse<List<TransactionResponse>>> getTransactionHistory(
            @PathVariable String walletId) {
        log.debug("{} Get transaction history for walletId: {}", LOG_PREFIX, walletId);

        List<TransactionResponse> transactions = walletService.getTransactionHistory(walletId);
        return ResponseEntity.ok(ApiResponse.success(transactions, "Transaction history fetched successfully"));
    }
}