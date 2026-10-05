package com.nexora.wallet.repository;

import com.nexora.wallet.entity.WalletTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WalletTransactionRepository extends JpaRepository<WalletTransaction, String> {

    List<WalletTransaction> findByWalletIdOrderByCreatedAtDesc(String walletId);

    boolean existsByReferenceId(String referenceId);

    Optional<WalletTransaction> findByReferenceId(String referenceId);
}