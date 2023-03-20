package com.fintech.service;

import com.fintech.domain.Transaction;
import com.fintech.dto.CursorPageResponse;
import com.fintech.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TransactionQueryService {

    private final TransactionRepository transactionRepository;

    public CursorPageResponse<Transaction> getTransactions(Long walletId, Long cursor, int limit) {
        List<Transaction> transactions;
        if (cursor == null) {
            transactions = transactionRepository.findByWalletIdOrderByIdDesc(walletId, PageRequest.of(0, limit + 1));
        } else {
            transactions = transactionRepository.findByWalletIdAndIdLessThanOrderByIdDesc(walletId, cursor, PageRequest.of(0, limit + 1));
        }

        boolean hasNext = transactions.size() > limit;
        if (hasNext) {
            transactions.remove(transactions.size() - 1);
        }

        Long nextCursor = transactions.isEmpty() ? null : transactions.get(transactions.size() - 1).getId();

        return new CursorPageResponse<>(transactions, nextCursor, hasNext);
    }
}
