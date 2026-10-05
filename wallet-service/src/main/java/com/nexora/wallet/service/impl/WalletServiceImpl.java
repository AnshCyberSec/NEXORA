package com.nexora.wallet.service.impl;

import com.nexora.wallet.dto.internal.InternalDebitRequest;
import com.nexora.wallet.dto.internal.InternalDebitResponse;
import com.nexora.wallet.dto.request.DepositRequest;
import com.nexora.wallet.dto.request.WithdrawRequest;
import com.nexora.wallet.dto.response.TransactionResponse;
import com.nexora.wallet.dto.response.WalletResponse;
import com.nexora.wallet.entity.Wallet;
import com.nexora.wallet.entity.WalletTransaction;
import com.nexora.wallet.enums.TransactionStatus;
import com.nexora.wallet.enums.TransactionType;
import com.nexora.wallet.enums.WalletStatus;
import com.nexora.wallet.exception.InsufficientBalanceException;
import com.nexora.wallet.exception.WalletAlreadyExistsException;
import com.nexora.wallet.exception.WalletNotFoundException;
import com.nexora.wallet.exception.WalletOperationException;
import com.nexora.wallet.mapper.TransactionMapper;
import com.nexora.wallet.mapper.WalletMapper;
import com.nexora.wallet.repository.WalletRepository;
import com.nexora.wallet.repository.WalletTransactionRepository;
import com.nexora.wallet.service.WalletService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class WalletServiceImpl implements WalletService {

    private final WalletRepository walletRepository;
    private final WalletTransactionRepository transactionRepository;
    private final WalletMapper walletMapper;
    private final TransactionMapper transactionMapper;

    private static final String LOG_PREFIX = "[WALLET-SERVICE]";

    @Override
    @Transactional
    public WalletResponse createWallet(String userId) {
        log.info("{} Creating wallet for userId: {}", LOG_PREFIX, userId);

        if (walletRepository.existsByUserId(userId)) {
            log.warn("{} Wallet already exists for userId: {}", LOG_PREFIX, userId);
            throw new WalletAlreadyExistsException(userId);
        }

        Wallet wallet = Wallet.builder()
                .userId(userId)
                .balance(BigDecimal.ZERO)
                .status(WalletStatus.ACTIVE)
                .build();

        Wallet savedWallet = walletRepository.save(wallet);
        log.info("{} Wallet created successfully with id: {} for userId: {}",
                LOG_PREFIX, savedWallet.getId(), userId);

        return walletMapper.toResponse(savedWallet);
    }

    @Override
    @Transactional(readOnly = true)
    public WalletResponse getWalletByUserId(String userId) {
        log.debug("{} Fetching wallet for userId: {}", LOG_PREFIX, userId);

        Wallet wallet = walletRepository.findByUserId(userId)
                .orElseThrow(() -> {
                    log.warn("{} Wallet not found for userId: {}", LOG_PREFIX, userId);
                    return new WalletNotFoundException(userId);
                });

        return walletMapper.toResponse(wallet);
    }

    @Override
    @Transactional(readOnly = true)
    public WalletResponse getWalletById(String walletId) {
        log.debug("{} Fetching wallet by id: {}", LOG_PREFIX, walletId);

        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> {
                    log.warn("{} Wallet not found with id: {}", LOG_PREFIX, walletId);
                    return new WalletNotFoundException("id", walletId);
                });

        return walletMapper.toResponse(wallet);
    }

    @Override
    @Transactional
    public TransactionResponse deposit(DepositRequest request, String walletId) {
        log.info("{} Processing deposit for walletId: {}, amount: {}, referenceId: {}",
                LOG_PREFIX, walletId, request.getAmount(), request.getReferenceId());

        validateDepositRequest(request);
        Wallet wallet = getWalletAndValidate(walletId);

        // Duplicate check
        if (transactionRepository.existsByReferenceId(request.getReferenceId())) {
            log.warn("{} Duplicate transaction detected for referenceId: {}",
                    LOG_PREFIX, request.getReferenceId());
            throw new WalletOperationException("Duplicate transaction", "DUPLICATE_TRANSACTION", 409);
        }

        // Perform deposit
        BigDecimal oldBalance = wallet.getBalance();
        BigDecimal newBalance = oldBalance.add(request.getAmount());
        wallet.setBalance(newBalance);
        wallet.setUpdatedAt(LocalDateTime.now());

        try {
            walletRepository.save(wallet);
            log.info("{} Balance updated for walletId: {}, oldBalance: {}, newBalance: {}",
                    LOG_PREFIX, walletId, oldBalance, newBalance);
        } catch (OptimisticLockingFailureException e) {
            log.error("{} Optimistic lock failure for walletId: {}, retry manually", LOG_PREFIX, walletId);
            // Manual retry logic - fetch latest and update again
            Wallet latestWallet = walletRepository.findById(walletId)
                    .orElseThrow(() -> new WalletNotFoundException("id", walletId));
            BigDecimal latestBalance = latestWallet.getBalance().add(request.getAmount());
            latestWallet.setBalance(latestBalance);
            latestWallet.setUpdatedAt(LocalDateTime.now());
            walletRepository.save(latestWallet);

            // Update wallet reference for transaction
            wallet.setBalance(latestBalance);
            newBalance = latestBalance;
        }

        WalletTransaction transaction = createTransactionRecord(
                wallet.getId(),
                request.getReferenceId(),
                TransactionType.CREDIT,
                request.getAmount(),
                newBalance,
                request.getDescription(),
                TransactionStatus.SUCCESS
        );

        WalletTransaction savedTransaction = transactionRepository.save(transaction);
        log.info("{} Deposit completed for walletId: {}, transactionId: {}",
                LOG_PREFIX, walletId, savedTransaction.getId());

        return transactionMapper.toResponse(savedTransaction);
    }

    @Override
    @Transactional
    public TransactionResponse withdraw(WithdrawRequest request, String walletId) {
        log.info("{} Processing withdrawal for walletId: {}, amount: {}, referenceId: {}",
                LOG_PREFIX, walletId, request.getAmount(), request.getReferenceId());

        validateWithdrawRequest(request);
        Wallet wallet = getWalletAndValidate(walletId);

        if (transactionRepository.existsByReferenceId(request.getReferenceId())) {
            log.warn("{} Duplicate transaction detected for referenceId: {}",
                    LOG_PREFIX, request.getReferenceId());
            throw new WalletOperationException("Duplicate transaction", "DUPLICATE_TRANSACTION", 409);
        }

        if (wallet.getBalance().compareTo(request.getAmount()) < 0) {
            log.warn("{} Insufficient balance for walletId: {}, available: {}, required: {}",
                    LOG_PREFIX, walletId, wallet.getBalance(), request.getAmount());
            throw new InsufficientBalanceException(request.getAmount(), wallet.getBalance());
        }

        BigDecimal oldBalance = wallet.getBalance();
        BigDecimal newBalance = oldBalance.subtract(request.getAmount());
        wallet.setBalance(newBalance);
        wallet.setUpdatedAt(LocalDateTime.now());

        try {
            walletRepository.save(wallet);
            log.info("{} Balance updated for walletId: {}, oldBalance: {}, newBalance: {}",
                    LOG_PREFIX, walletId, oldBalance, newBalance);
        } catch (OptimisticLockingFailureException e) {
            log.error("{} Optimistic lock failure for walletId: {}, retry manually", LOG_PREFIX, walletId);
            // Manual retry logic
            Wallet latestWallet = walletRepository.findById(walletId)
                    .orElseThrow(() -> new WalletNotFoundException("id", walletId));

            if (latestWallet.getBalance().compareTo(request.getAmount()) < 0) {
                throw new InsufficientBalanceException(request.getAmount(), latestWallet.getBalance());
            }

            BigDecimal latestBalance = latestWallet.getBalance().subtract(request.getAmount());
            latestWallet.setBalance(latestBalance);
            latestWallet.setUpdatedAt(LocalDateTime.now());
            walletRepository.save(latestWallet);

            wallet.setBalance(latestBalance);
            newBalance = latestBalance;
        }

        WalletTransaction transaction = createTransactionRecord(
                wallet.getId(),
                request.getReferenceId(),
                TransactionType.DEBIT,
                request.getAmount(),
                newBalance,
                request.getDescription(),
                TransactionStatus.SUCCESS
        );

        WalletTransaction savedTransaction = transactionRepository.save(transaction);
        log.info("{} Withdrawal completed for walletId: {}, transactionId: {}",
                LOG_PREFIX, walletId, savedTransaction.getId());

        return transactionMapper.toResponse(savedTransaction);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TransactionResponse> getTransactionHistory(String walletId) {
        log.debug("{} Fetching transaction history for walletId: {}", LOG_PREFIX, walletId);

        if (!walletRepository.existsById(walletId)) {
            log.warn("{} Wallet not found with id: {}", LOG_PREFIX, walletId);
            throw new WalletNotFoundException("id", walletId);
        }

        List<WalletTransaction> transactions = transactionRepository.findByWalletIdOrderByCreatedAtDesc(walletId);
        log.debug("{} Found {} transactions for walletId: {}", LOG_PREFIX, transactions.size(), walletId);

        return transactions.stream()
                .map(transactionMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getBalance(String userId) {
        log.debug("{} Getting balance for userId: {}", LOG_PREFIX, userId);

        Wallet wallet = walletRepository.findByUserId(userId)
                .orElseThrow(() -> {
                    log.warn("{} Wallet not found for userId: {}", LOG_PREFIX, userId);
                    return new WalletNotFoundException(userId);
                });

        return wallet.getBalance();
    }

    @Override
    public InternalDebitResponse debitWallet(String walletId, InternalDebitRequest request) {
        log.info("{} Processing internal debit for walletId: {}, amount: {}, referenceId: {}",
                LOG_PREFIX, walletId, request.getAmount(), request.getReferenceId());

        if(transactionRepository.existsByReferenceId(request.getReferenceId())){
            log.warn("{} Duplicate internal debit detected for referenceId: {}",
                    LOG_PREFIX,request.getReferenceId());

            WalletTransaction existing = transactionRepository.findByReferenceId(request.getReferenceId())
                    .orElseThrow(()-> new WalletNotFoundException("referenceId",request.getReferenceId()));

            return InternalDebitResponse.builder()
                    .success(true)
                    .walletId(existing.getWalletId())
                    .transactionId(existing.getId())
                    .amount(existing.getAmount())
                    .balanceAfter(existing.getBalanceAfter())
                    .message("Duplicate request - returning existing transaction")
                    .build();

        }
        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> {
                    log.warn("{} Wallet not found with id: {}", LOG_PREFIX, walletId);
                    return new WalletNotFoundException("id", walletId);
                });

        validateWalletStatus(wallet);

        if (wallet.getBalance().compareTo(request.getAmount()) < 0) {
            log.warn("{} Insufficient balance for walletId: {}, available: {}, required: {}",
                    LOG_PREFIX, walletId, wallet.getBalance(), request.getAmount());

            return InternalDebitResponse.builder()
                    .success(false)
                    .walletId(walletId)
                    .amount(request.getAmount())
                    .balanceAfter(wallet.getBalance())
                    .message(String.format("Insufficient balance. Available: ₹%s, Required: ₹%s",
                            wallet.getBalance(), request.getAmount()))
                    .build();
        }

        BigDecimal oldBalance = wallet.getBalance();
        BigDecimal newBalance = oldBalance.subtract(request.getAmount());
        wallet.setBalance(newBalance);
        wallet.setUpdatedAt(LocalDateTime.now());
        walletRepository.save(wallet);


        log.info("{} Balance updated for walletId: {}, oldBalance: {}, newBalance: {}",
                LOG_PREFIX, walletId, oldBalance, newBalance);

        WalletTransaction transaction = WalletTransaction.builder()
                .walletId(wallet.getId())
                .referenceId(request.getReferenceId())
                .type(TransactionType.DEBIT)
                .amount(request.getAmount())
                .balanceAfter(newBalance)
                .description(request.getDescription())
                .status(TransactionStatus.SUCCESS)
                .build();

        WalletTransaction savedTransaction = transactionRepository.save(transaction);

        log.info("{} Internal debit completed for walletId: {}, transactionId: {}",
                LOG_PREFIX, walletId, savedTransaction.getId());

        return InternalDebitResponse.builder()
                .success(true)
                .walletId(walletId)
                .transactionId(savedTransaction.getId())
                .amount(request.getAmount())
                .balanceAfter(newBalance)
                .message("Debit successful")
                .build();
    }


    private Wallet getWalletAndValidate(String walletId) {
        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> {
                    log.warn("{} Wallet not found with id: {}", LOG_PREFIX, walletId);
                    return new WalletNotFoundException("id", walletId);
                });

        validateWalletStatus(wallet);
        return wallet;
    }

    private void validateWalletStatus(Wallet wallet) {
        if (wallet.getStatus() == WalletStatus.FROZEN) {
            log.warn("{} Wallet is frozen: {}", LOG_PREFIX, wallet.getId());
            throw new WalletOperationException("Wallet is frozen", "WALLET_FROZEN", 403);
        }
        if (wallet.getStatus() == WalletStatus.CLOSED) {
            log.warn("{} Wallet is closed: {}", LOG_PREFIX, wallet.getId());
            throw new WalletOperationException("Wallet is closed", "WALLET_CLOSED", 403);
        }
    }

    private void validateDepositRequest(DepositRequest request) {
        if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            log.warn("{} Invalid deposit amount: {}", LOG_PREFIX, request.getAmount());
            throw new WalletOperationException("Amount must be greater than zero", "INVALID_AMOUNT", 400);
        }
    }

    private void validateWithdrawRequest(WithdrawRequest request) {
        if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            log.warn("{} Invalid withdraw amount: {}", LOG_PREFIX, request.getAmount());
            throw new WalletOperationException("Amount must be greater than zero", "INVALID_AMOUNT", 400);
        }
    }

    private WalletTransaction createTransactionRecord(
            String walletId,
            String referenceId,
            TransactionType type,
            BigDecimal amount,
            BigDecimal balanceAfter,
            String description,
            TransactionStatus status) {

        return WalletTransaction.builder()
                .walletId(walletId)
                .referenceId(referenceId)
                .type(type)
                .amount(amount)
                .balanceAfter(balanceAfter)
                .description(description)
                .status(status)
                .build();
    }
}