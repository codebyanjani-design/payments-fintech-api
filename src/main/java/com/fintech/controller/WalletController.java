package com.fintech.controller;

import com.fintech.domain.Transaction;
import com.fintech.domain.Wallet;
import com.fintech.dto.CursorPageResponse;
import com.fintech.dto.TransactionRequest;
import com.fintech.dto.TransferRequest;
import com.fintech.dto.WalletRequest;
import com.fintech.service.TransactionQueryService;
import com.fintech.service.WalletService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/wallets")
@RequiredArgsConstructor
public class WalletController {

    private final WalletService walletService;
    private final TransactionQueryService transactionQueryService;

    @PostMapping
    public ResponseEntity<Wallet> createWallet(@Valid @RequestBody WalletRequest request) {
        Wallet wallet = walletService.createWallet(request.getUserId(), request.getInitialBalance());
        return ResponseEntity.status(HttpStatus.CREATED).body(wallet);
    }

    @PostMapping("/{id}/transactions")
    public ResponseEntity<Transaction> processTransaction(
            @PathVariable Long id,
            @Valid @RequestBody TransactionRequest request) {
        Transaction transaction = walletService.processCreditOrDebit(
                id, request.getAmount(), request.getType(), request.getReferenceId());
        return ResponseEntity.status(HttpStatus.CREATED).body(transaction);
    }

    @PostMapping("/{id}/transfers")
    public ResponseEntity<Transaction> processTransfer(
            @PathVariable Long id,
            @Valid @RequestBody TransferRequest request) {
        Transaction transaction = walletService.processTransfer(
                id, request.getTargetWalletId(), request.getAmount(), request.getReferenceId());
        return ResponseEntity.status(HttpStatus.CREATED).body(transaction);
    }

    @GetMapping("/{id}/transactions")
    public ResponseEntity<CursorPageResponse<Transaction>> getTransactions(
            @PathVariable Long id,
            @RequestParam(required = false) Long cursor,
            @RequestParam(defaultValue = "10") int limit) {
        CursorPageResponse<Transaction> response = transactionQueryService.getTransactions(id, cursor, limit);
        return ResponseEntity.ok(response);
    }
}
