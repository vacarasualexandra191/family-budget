package com.familybudget.service.impl;

import com.familybudget.dto.AccountForm;
import com.familybudget.entity.Account;
import com.familybudget.entity.User;
import com.familybudget.exception.ResourceNotFoundException;
import com.familybudget.repository.AccountRepository;
import com.familybudget.repository.UserRepository;
import com.familybudget.service.AccountService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;
    private final UserRepository userRepository;

    @Override
    public Page<Account> findAllForUser(String username, Pageable pageable) {
        return accountRepository.findByOwnerUsername(username, pageable);
    }

    @Override
    public Account findById(Long id) {
        return accountRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Contul", id));
    }

    @Override
    @Transactional
    public Account create(AccountForm form, String username) {
        User owner = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Utilizatorul " + username + " nu a fost gasit"));

        Account account = Account.builder()
                .name(form.getName())
                .type(form.getType())
                .balance(form.getBalance())
                .currency(form.getCurrency() == null ? "RON" : form.getCurrency())
                .owner(owner)
                .build();

        Account saved = accountRepository.save(account);
        log.info("Cont creat: id={}, nume={}, owner={}", saved.getId(), saved.getName(), username);
        return saved;
    }

    @Override
    @Transactional
    public Account update(Long id, AccountForm form) {
        Account existing = findById(id);
        existing.setName(form.getName());
        existing.setType(form.getType());
        existing.setCurrency(form.getCurrency());
        // Soldul nu se editeaza manual aici - se schimba doar prin tranzactii;
        // permitem totusi o corectie explicita daca utilizatorul o cere.
        existing.setBalance(form.getBalance());

        Account updated = accountRepository.save(existing);
        log.info("Cont actualizat: id={}", updated.getId());
        return updated;
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Account account = findById(id);
        accountRepository.delete(account);
        log.info("Cont sters: id={}", id);
    }
}
