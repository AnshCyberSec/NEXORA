package com.nexora.transaction.repository;

import com.nexora.transaction.entity.Transaction;
import com.nexora.transaction.enums.TransactionStatus;
import com.nexora.transaction.enums.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, String> {

    Optional<Transaction> findByReferenceId(String referenceId);

    List<Transaction> findByUserIdOrderByCreatedAtDesc(String userId);

    List<Transaction> findByWalletIdOrderByCreatedAtDesc(String walletId);

    List<Transaction> findByUserIdAndType(String userId, TransactionType type);

    List<Transaction> findByWalletIdAndStatus(String walletId, TransactionStatus status);

    boolean existsByReferenceId(String referenceId);
}