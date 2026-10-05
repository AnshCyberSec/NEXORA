package com.nexora.transaction.service;

import com.nexora.transaction.dto.request.CreateTransactionRequest;
import com.nexora.transaction.dto.response.TransactionResponse;
import com.nexora.transaction.enums.TransactionStatus;
import com.nexora.transaction.enums.TransactionType;

import java.util.List;

public interface TransactionService {

    TransactionResponse createTransaction(CreateTransactionRequest request);

    TransactionResponse getTransactionById(String transactionId);

    TransactionResponse getTransactionByReferenceId(String referenceId);

    List<TransactionResponse> getTransactionsByUserId(String userId);

    List<TransactionResponse> getTransactionsByWalletId(String walletId);

    List<TransactionResponse> getTransactionsByUserIdAndType(String userId, TransactionType type);

    TransactionResponse updateTransactionStatus(String transactionId, TransactionStatus status, String failureReason);

    boolean existsByReferenceId(String referenceId);
}