package com.familybudget.service;

import com.familybudget.dto.AccountForm;
import com.familybudget.entity.Account;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AccountService {

    Page<Account> findAllForUser(String username, Pageable pageable);

    Account findById(Long id);

    Account create(AccountForm form, String username);

    Account update(Long id, AccountForm form);

    void delete(Long id);
}
