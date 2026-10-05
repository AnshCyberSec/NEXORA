package com.nexora.transaction.mapper;

import com.nexora.transaction.dto.request.CreateTransactionRequest;
import com.nexora.transaction.dto.response.TransactionResponse;
import com.nexora.transaction.entity.Transaction;
import org.springframework.stereotype.Component;

@Component
public class TransactionMapper {

    public Transaction toEntity(CreateTransactionRequest request) {
        return Transaction.builder()
                .userId(request.getUserId())
                .walletId(request.getWalletId())
                .referenceId(request.getReferenceId())
                .sourceId(request.getSourceId())
                .type(request.getType())
                .source(request.getSource())
                .amount(request.getAmount())
                .balanceBefore(request.getBalanceBefore())
                .description(request.getDescription())
                .build();
    }

    public TransactionResponse toResponse(Transaction transaction) {
        return TransactionResponse.builder()
                .id(transaction.getId())
                .userId(transaction.getUserId())
                .walletId(transaction.getWalletId())
                .referenceId(transaction.getReferenceId())
                .sourceId(transaction.getSourceId())
                .type(transaction.getType())
                .source(transaction.getSource())
                .amount(transaction.getAmount())
                .balanceBefore(transaction.getBalanceBefore())
                .balanceAfter(transaction.getBalanceAfter())
                .description(transaction.getDescription())
                .status(transaction.getStatus())
                .failureReason(transaction.getFailureReason())
                .createdAt(transaction.getCreatedAt())
                .updatedAt(transaction.getUpdatedAt())
                .build();
    }
}