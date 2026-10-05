package com.nexora.wallet.controller;

import com.nexora.wallet.dto.internal.InternalDebitRequest;
import com.nexora.wallet.dto.internal.InternalDebitResponse;
import com.nexora.wallet.service.WalletService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/internal/wallets")
@RequiredArgsConstructor
public class WalletInternalController {
    private final WalletService walletService;

    private static final String LOG_PREFIX = "[WALLET-INTERNAL]";

    @PostMapping("{walletId}/debit")
    public ResponseEntity<InternalDebitResponse> debitWallet(@PathVariable String walletId, @Valid@RequestBody InternalDebitRequest request){
        log.info("{} Internal debit request for walletId: {}, amount: {}, referenceId: {}",
                LOG_PREFIX, walletId, request.getAmount(), request.getReferenceId());

        InternalDebitResponse response = walletService.debitWallet(walletId, request);

        log.info("{} Internal debit completed for walletId: {}, success: {}",
                LOG_PREFIX, walletId, response.isSuccess());

        return ResponseEntity.ok(response);
    }

}
