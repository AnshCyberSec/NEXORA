package com.nexora.wallet.exception;

import java.math.BigDecimal;

public class InsufficientBalanceException extends BusinessException{
    public InsufficientBalanceException(BigDecimal required,BigDecimal available){
        super(String.format("Insufficient balance: Required ₹%s, Available ₹%s",required,available),
                "INSUFFICIENT_BALANCE",
                400
        );
    }
}
