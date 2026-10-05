package com.nexora.wallet.service;

import com.nexora.wallet.dto.internal.InternalDebitRequest;
import com.nexora.wallet.dto.internal.InternalDebitResponse;
import com.nexora.wallet.dto.request.DepositRequest;
import com.nexora.wallet.dto.request.WithdrawRequest;
import com.nexora.wallet.dto.response.TransactionResponse;
import com.nexora.wallet.dto.response.WalletResponse;

import java.math.BigDecimal;
import java.util.List;

public interface WalletService {
    WalletResponse createWallet(String userId);

    WalletResponse getWalletByUserId(String userId);

    WalletResponse getWalletById(String walletId);

    TransactionResponse deposit(DepositRequest request,String walletId);

    TransactionResponse withdraw(WithdrawRequest request,String walletId);

    List<TransactionResponse> getTransactionHistory(String walletId);

    BigDecimal getBalance(String userId);

    InternalDebitResponse debitWallet(String walletId, InternalDebitRequest request);
}
