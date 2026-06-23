package com.familybudget.service.impl;

import com.familybudget.dto.TransactionForm;
import com.familybudget.entity.Account;
import com.familybudget.entity.Category;
import com.familybudget.entity.Tag;
import com.familybudget.entity.Transaction;
import com.familybudget.exception.ResourceNotFoundException;
import com.familybudget.repository.AccountRepository;
import com.familybudget.repository.CategoryRepository;
import com.familybudget.repository.TagRepository;
import com.familybudget.repository.TransactionRepository;
import com.familybudget.service.TransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final CategoryRepository categoryRepository;
    private final TagRepository tagRepository;
    private final NotificationClientService notificationClientService;

    @Override
    public Page<Transaction> findAllForUser(String username, Pageable pageable) {
        return transactionRepository.findByAccountOwnerUsername(username, pageable);
    }

    @Override
    public Transaction findById(Long id) {
        return transactionRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Tranzactia", id));
    }

    @Override
    @Transactional
    public Transaction create(TransactionForm form, String username) {
        Account account = accountRepository.findById(form.getAccountId())
                .orElseThrow(() -> ResourceNotFoundException.of("Contul", form.getAccountId()));

        if (!account.getOwner().getUsername().equals(username)) {
            log.warn("Utilizatorul {} a incercat sa creeze o tranzactie pe contul altcuiva ({})",
                    username, account.getId());
            throw new org.springframework.security.access.AccessDeniedException(
                    "Nu poti adauga tranzactii pe un cont care nu iti apartine");
        }

        Category category = categoryRepository.findById(form.getCategoryId())
                .orElseThrow(() -> ResourceNotFoundException.of("Categoria", form.getCategoryId()));

        Transaction transaction = Transaction.builder()
                .amount(form.getAmount())
                .transactionDate(form.getTransactionDate())
                .description(form.getDescription())
                .account(account)
                .category(category)
                .tags(resolveTags(form.getTagIds()))
                .build();

        applyBalanceChange(account, transaction.getAmount(), category.getType());

        Transaction saved = transactionRepository.save(transaction);
        log.info("Tranzactie creata: id={}, suma={}, cont={}, categorie={}",
                saved.getId(), saved.getAmount(), account.getId(), category.getName());


        if (account.getBalance().signum() < 0) {
            notificationClientService.notify(
                    username,
                    "NEGATIVE_BALANCE_ALERT",
                    String.format("Contul '%s' are sold negativ (%.2f %s) dupa tranzactia '%s'.",
                            account.getName(), account.getBalance(), account.getCurrency(),
                            category.getName())
            );
        } else {
            notificationClientService.notify(
                    username,
                    "TRANSACTION_CONFIRMATION",
                    String.format("Tranzactie inregistrata: %.2f RON pe categoria '%s'.",
                            saved.getAmount(), category.getName())
            );
        }

        return saved;
    }

    @Override
    @Transactional
    public Transaction update(Long id, TransactionForm form) {
        Transaction existing = findById(id);

        // Reverseaza efectul vechii tranzactii asupra soldului, apoi aplica noua valoare
        reverseBalanceChange(existing.getAccount(), existing.getAmount(), existing.getCategory().getType());

        Category category = categoryRepository.findById(form.getCategoryId())
                .orElseThrow(() -> ResourceNotFoundException.of("Categoria", form.getCategoryId()));

        existing.setAmount(form.getAmount());
        existing.setTransactionDate(form.getTransactionDate());
        existing.setDescription(form.getDescription());
        existing.setCategory(category);
        existing.setTags(resolveTags(form.getTagIds()));

        applyBalanceChange(existing.getAccount(), existing.getAmount(), category.getType());

        Transaction updated = transactionRepository.save(existing);
        log.info("Tranzactie actualizata: id={}", updated.getId());
        return updated;
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Transaction transaction = findById(id);
        reverseBalanceChange(transaction.getAccount(), transaction.getAmount(), transaction.getCategory().getType());
        transactionRepository.delete(transaction);
        log.info("Tranzactie stearsa: id={}", id);
    }

    private Set<Tag> resolveTags(Set<Long> tagIds) {
        if (tagIds == null || tagIds.isEmpty()) {
            return new HashSet<>();
        }
        return tagIds.stream()
                .map(tagId -> tagRepository.findById(tagId)
                        .orElseThrow(() -> ResourceNotFoundException.of("Tag-ul", tagId)))
                .collect(Collectors.toSet());
    }

    private void applyBalanceChange(Account account, BigDecimal amount, Category.CategoryType type) {
        BigDecimal delta = type == Category.CategoryType.INCOME ? amount : amount.negate();
        account.setBalance(account.getBalance().add(delta));
        accountRepository.save(account);
    }

    private void reverseBalanceChange(Account account, BigDecimal amount, Category.CategoryType type) {
        BigDecimal delta = type == Category.CategoryType.INCOME ? amount.negate() : amount;
        account.setBalance(account.getBalance().add(delta));
        accountRepository.save(account);
    }
}
