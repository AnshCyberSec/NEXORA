package com.nexora.wallet.dto.internal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InternalDebitResponse {
    private boolean success;
    private String walletId;
    private String transactionId;
    private BigDecimal amount;
    private BigDecimal balanceAfter;
    private String message;
}
