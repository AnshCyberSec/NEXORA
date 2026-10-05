package com.nexora.wallet.dto.response;

import com.nexora.wallet.enums.TransactionStatus;
import com.nexora.wallet.enums.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionResponse {
    private String id;
    private String walletId;
    private String referenceId;
    private TransactionType type;
    private BigDecimal amount;
    private BigDecimal balanceAfter;
    private String description;
    private TransactionStatus status;
    private LocalDateTime createdAt;
}
