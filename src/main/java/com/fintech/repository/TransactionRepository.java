package com.fintech.repository;

import com.fintech.domain.Transaction;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    
    @Query("SELECT t FROM Transaction t WHERE t.walletId = :walletId AND t.id < :lastSeenId ORDER BY t.id DESC")
    List<Transaction> findByWalletIdAndIdLessThanOrderByIdDesc(
            @Param("walletId") Long walletId, 
            @Param("lastSeenId") Long lastSeenId, 
            Pageable pageable);

    @Query("SELECT t FROM Transaction t WHERE t.walletId = :walletId ORDER BY t.id DESC")
    List<Transaction> findByWalletIdOrderByIdDesc(
            @Param("walletId") Long walletId, 
            Pageable pageable);
            
    boolean existsByReferenceId(String referenceId);
}
