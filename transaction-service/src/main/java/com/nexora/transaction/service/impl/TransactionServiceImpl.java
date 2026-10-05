package com.nexora.transaction.service.impl;

import com.nexora.transaction.dto.request.CreateTransactionRequest;
import com.nexora.transaction.dto.response.TransactionResponse;
import com.nexora.transaction.entity.Transaction;
import com.nexora.transaction.enums.TransactionStatus;
import com.nexora.transaction.enums.TransactionType;
import com.nexora.transaction.exception.InvalidTransactionException;
import com.nexora.transaction.exception.TransactionNotFoundException;
import com.nexora.transaction.mapper.TransactionMapper;
import com.nexora.transaction.repository.TransactionRepository;
import com.nexora.transaction.service.TransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;


@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final TransactionMapper transactionMapper;

    private static final String LOG_PREFIX = "[TRANSACTION-SERVICE]";

    @Override
    public TransactionResponse createTransaction(CreateTransactionRequest request) {
        log.info("{} Creating transaction for userId: {}, referenceId: {}, amount: {}",
                LOG_PREFIX, request.getUserId(), request.getReferenceId(), request.getAmount());

        // Check for duplicate (idempotency)
        if (transactionRepository.existsByReferenceId(request.getReferenceId())) {
            log.warn("{} Transaction already exists with referenceId: {}", LOG_PREFIX, request.getReferenceId());
            throw new InvalidTransactionException("Transaction already exists", "DUPLICATE_TRANSACTION", 409);
        }

        // Validate balance
        if (request.getType() == TransactionType.DEBIT &&
                request.getBalanceBefore().compareTo(request.getAmount()) < 0) {
            log.warn("{} Insufficient balance: before={}, amount={}",
                    LOG_PREFIX, request.getBalanceBefore(), request.getAmount());
            throw new InvalidTransactionException("Insufficient balance", "INSUFFICIENT_BALANCE", 400);
        }

        // Calculate balance after
        Transaction transaction = transactionMapper.toEntity(request);

        if (request.getType() == TransactionType.CREDIT) {
            transaction.setBalanceAfter(request.getBalanceBefore().add(request.getAmount()));
        } else {
            transaction.setBalanceAfter(request.getBalanceBefore().subtract(request.getAmount()));
        }

        Transaction savedTransaction = transactionRepository.save(transaction);
        log.info("{} Transaction created successfully with id: {}, referenceId: {}",
                LOG_PREFIX, savedTransaction.getId(), savedTransaction.getReferenceId());

        return transactionMapper.toResponse(savedTransaction);
    }

    @Override
    @Transactional(readOnly = true)
    public TransactionResponse getTransactionById(String transactionId) {
        log.debug("{} Fetching transaction by id: {}", LOG_PREFIX, transactionId);

        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> {
                    log.warn("{} Transaction not found with id: {}", LOG_PREFIX, transactionId);
                    return new TransactionNotFoundException(transactionId);
                });

        return transactionMapper.toResponse(transaction);
    }

    @Override
    @Transactional(readOnly = true)
    public TransactionResponse getTransactionByReferenceId(String referenceId) {
        log.debug("{} Fetching transaction by referenceId: {}", LOG_PREFIX, referenceId);

        Transaction transaction = transactionRepository.findByReferenceId(referenceId)
                .orElseThrow(() -> {
                    log.warn("{} Transaction not found with referenceId: {}", LOG_PREFIX, referenceId);
                    return new TransactionNotFoundException("referenceId", referenceId);
                });

        return transactionMapper.toResponse(transaction);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TransactionResponse> getTransactionsByUserId(String userId) {
        log.debug("{} Fetching transactions for userId: {}", LOG_PREFIX, userId);

        List<Transaction> transactions = transactionRepository.findByUserIdOrderByCreatedAtDesc(userId);

        return transactions.stream()
                .map(transactionMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TransactionResponse> getTransactionsByWalletId(String walletId) {
        log.debug("{} Fetching transactions for walletId: {}", LOG_PREFIX, walletId);

        List<Transaction> transactions = transactionRepository.findByWalletIdOrderByCreatedAtDesc(walletId);

        return transactions.stream()
                .map(transactionMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TransactionResponse> getTransactionsByUserIdAndType(String userId, TransactionType type) {
        log.debug("{} Fetching transactions for userId: {}, type: {}", LOG_PREFIX, userId, type);

        List<Transaction> transactions = transactionRepository.findByUserIdAndType(userId, type);

        return transactions.stream()
                .map(transactionMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public TransactionResponse updateTransactionStatus(String transactionId, TransactionStatus status, String failureReason) {
        log.info("{} Updating transaction status for transactionId: {}, newStatus: {}",
                LOG_PREFIX, transactionId, status);

        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> {
                    log.warn("{} Transaction not found with id: {}", LOG_PREFIX, transactionId);
                    return new TransactionNotFoundException(transactionId);
                });

        // Validate status transition
        validateStatusTransition(transaction.getStatus(), status);

        if (status == TransactionStatus.SUCCESS) {
            transaction.markSuccess();
        } else if (status == TransactionStatus.FAILED) {
            transaction.markFailed(failureReason != null ? failureReason : "Unknown error");
        } else {
            transaction.setStatus(status);
        }

        try {
            Transaction updatedTransaction = transactionRepository.save(transaction);
            log.info("{} Transaction status updated successfully for transactionId: {}, newStatus: {}",
                    LOG_PREFIX, transactionId, updatedTransaction.getStatus());
            return transactionMapper.toResponse(updatedTransaction);
        } catch (OptimisticLockingFailureException e) {
            log.error("{} Optimistic lock failure for transactionId: {}", LOG_PREFIX, transactionId);
            throw new RuntimeException("Transaction update failed due to concurrent modification", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByReferenceId(String referenceId) {
        return transactionRepository.existsByReferenceId(referenceId);
    }



    private void validateStatusTransition(TransactionStatus currentStatus, TransactionStatus newStatus) {
        if (currentStatus == TransactionStatus.SUCCESS || currentStatus == TransactionStatus.FAILED) {
            if (currentStatus != newStatus) {
                log.warn("{} Cannot change status from {} to {} (final state)",
                        LOG_PREFIX, currentStatus, newStatus);
                throw new InvalidTransactionException(
                        String.format("Cannot change status from %s to %s. Transaction is in final state.",
                                currentStatus, newStatus)
                );
            }
        }
    }
}