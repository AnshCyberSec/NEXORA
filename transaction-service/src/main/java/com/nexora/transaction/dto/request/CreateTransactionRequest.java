package com.nexora.transaction.dto.request;

import com.nexora.transaction.enums.TransactionSource;
import com.nexora.transaction.enums.TransactionType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateTransactionRequest {

    @NotBlank(message = "User ID is required")
    private String userId;

    @NotBlank(message = "Wallet ID is required")
    private String walletId;

    @NotBlank(message = "Reference ID is required")
    private String referenceId;

    @NotBlank(message = "Source ID is required")
    private String sourceId;

    @NotNull(message = "Transaction type is required")
    private TransactionType type;

    @NotNull(message = "Transaction source is required")
    private TransactionSource source;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    private BigDecimal amount;

    @NotNull(message = "Balance before is required")
    private BigDecimal balanceBefore;

    private String description;
}