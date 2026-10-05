package com.nexora.wallet.dto.request;

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
public class DepositRequest {
    @NotBlank(message = "Reference ID is required")
    private String referenceId;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01",message = "Amount must be greater than 0")
    private BigDecimal amount;

    private String description;

}
