package com.familybudget.service;

import com.familybudget.dto.TransactionForm;
import com.familybudget.entity.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface TransactionService {

    Page<Transaction> findAllForUser(String username, Pageable pageable);

    Transaction findById(Long id);

    Transaction create(TransactionForm form, String username);

    Transaction update(Long id, TransactionForm form);

    void delete(Long id);
}
