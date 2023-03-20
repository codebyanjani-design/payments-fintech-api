package com.fintech.service;

import com.fintech.domain.Transaction;
import com.fintech.domain.TransactionType;
import com.fintech.domain.Wallet;
import com.fintech.exception.InsufficientFundsException;
import com.fintech.exception.DuplicateTransactionException;
import com.fintech.exception.ResourceNotFoundException;
import com.fintech.repository.TransactionRepository;
import com.fintech.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Slf4j
public class WalletService {

    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;

    @Transactional
    public Wallet createWallet(String userId, BigDecimal initialBalance) {
        if (walletRepository.findByUserId(userId).isPresent()) {
            throw new IllegalArgumentException("Wallet already exists for user: " + userId);
        }
        Wallet wallet = Wallet.builder()
                .userId(userId)
                .balance(initialBalance != null ? initialBalance : BigDecimal.ZERO)
                .build();
        return walletRepository.save(wallet);
    }

    @Transactional
    public Transaction processTransfer(Long sourceWalletId, Long targetWalletId, BigDecimal amount, String referenceId) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Transfer amount must be greater than zero");
        }
        if (sourceWalletId.equals(targetWalletId)) {
            throw new IllegalArgumentException("Source and target wallets must be different");
        }
        
        // Prevent deadlocks by always locking the smaller ID first
        Long firstLockId = sourceWalletId < targetWalletId ? sourceWalletId : targetWalletId;
        Long secondLockId = sourceWalletId < targetWalletId ? targetWalletId : sourceWalletId;

        // Lock wallets using PESSIMISTIC_WRITE
        Wallet firstWallet = walletRepository.findByIdForUpdate(firstLockId)
                .orElseThrow(() -> new ResourceNotFoundException("Wallet not found: " + firstLockId));
        Wallet secondWallet = walletRepository.findByIdForUpdate(secondLockId)
                .orElseThrow(() -> new ResourceNotFoundException("Wallet not found: " + secondLockId));

        Wallet sourceWallet = firstLockId.equals(sourceWalletId) ? firstWallet : secondWallet;
        Wallet targetWallet = firstLockId.equals(targetWalletId) ? firstWallet : secondWallet;

        if (sourceWallet.getBalance().compareTo(amount) < 0) {
            throw new InsufficientFundsException("Insufficient funds in source wallet");
        }

        // Idempotency check
        if (transactionRepository.existsByReferenceId(referenceId + "_DEBIT")) {
            throw new DuplicateTransactionException("Transaction with reference ID already exists");
        }

        // Update balances
        sourceWallet.setBalance(sourceWallet.getBalance().subtract(amount));
        targetWallet.setBalance(targetWallet.getBalance().add(amount));

        walletRepository.save(sourceWallet);
        walletRepository.save(targetWallet);

        // Record transactions
        Transaction debitTx = Transaction.builder()
                .walletId(sourceWallet.getId())
                .type(TransactionType.DEBIT)
                .amount(amount)
                .referenceId(referenceId + "_DEBIT")
                .build();
        transactionRepository.save(debitTx);

        Transaction creditTx = Transaction.builder()
                .walletId(targetWallet.getId())
                .type(TransactionType.CREDIT)
                .amount(amount)
                .referenceId(referenceId + "_CREDIT")
                .build();
        transactionRepository.save(creditTx);

        return debitTx;
    }

    @Transactional
    public Transaction processCreditOrDebit(Long walletId, BigDecimal amount, TransactionType type, String referenceId) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero");
        }

        // Idempotency check
        if (transactionRepository.existsByReferenceId(referenceId)) {
            throw new DuplicateTransactionException("Transaction already exists");
        }

        // Pessimistic Write Lock prevents double-spending
        Wallet wallet = walletRepository.findByIdForUpdate(walletId)
                .orElseThrow(() -> new ResourceNotFoundException("Wallet not found"));

        if (type == TransactionType.DEBIT) {
            if (wallet.getBalance().compareTo(amount) < 0) {
                throw new InsufficientFundsException("Insufficient funds");
            }
            wallet.setBalance(wallet.getBalance().subtract(amount));
        } else {
            wallet.setBalance(wallet.getBalance().add(amount));
        }

        walletRepository.save(wallet);

        Transaction transaction = Transaction.builder()
                .walletId(walletId)
                .type(type)
                .amount(amount)
                .referenceId(referenceId)
                .build();
        
        return transactionRepository.save(transaction);
    }
}
