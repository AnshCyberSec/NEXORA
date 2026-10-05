package com.nexora.payment.client;

import com.nexora.payment.dto.request.WalletDebitRequest;
import com.nexora.payment.dto.response.WalletDebitResponse;
import jakarta.validation.Valid;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "wallet-service")
public interface WalletClient {

    @PostMapping("/internal/wallets/{walletId}/debit")
    WalletDebitResponse debitWallet(
            @PathVariable("walletId") String walletId,
            @Valid @RequestBody WalletDebitRequest request  // ← @Valid add karo
    );
}
