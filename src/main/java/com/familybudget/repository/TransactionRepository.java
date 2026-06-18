package com.familybudget.repository;

import com.familybudget.entity.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    Page<Transaction> findByAccountId(Long accountId, Pageable pageable);

    Page<Transaction> findByAccountOwnerUsername(String username, Pageable pageable);

    Page<Transaction> findByCategoryId(Long categoryId, Pageable pageable);

    Page<Transaction> findByTransactionDateBetween(LocalDate start, LocalDate end, Pageable pageable);

    Page<Transaction> findByAccountOwnerUsernameAndTransactionDateBetween(
            String username, LocalDate start, LocalDate end, Pageable pageable);

    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t " +
            "WHERE t.category.id = :categoryId " +
            "AND t.account.owner.family.id = :familyId " +
            "AND EXTRACT(MONTH FROM t.transactionDate) = :month " +
            "AND EXTRACT(YEAR FROM t.transactionDate) = :year")
    BigDecimal sumByCategoryAndFamilyAndMonth(
            @Param("categoryId") Long categoryId,
            @Param("familyId") Long familyId,
            @Param("month") Integer month,
            @Param("year") Integer year);
}