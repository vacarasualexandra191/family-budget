package com.familybudget.service;

import com.familybudget.dto.TransactionForm;
import com.familybudget.entity.*;
import com.familybudget.exception.ResourceNotFoundException;
import com.familybudget.repository.AccountRepository;
import com.familybudget.repository.CategoryRepository;
import com.familybudget.repository.TagRepository;
import com.familybudget.repository.TransactionRepository;
import com.familybudget.service.impl.TransactionServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.security.access.AccessDeniedException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock TransactionRepository transactionRepository;
    @Mock AccountRepository accountRepository;
    @Mock CategoryRepository categoryRepository;
    @Mock TagRepository tagRepository;

    @InjectMocks TransactionServiceImpl transactionService;

    private User owner;
    private Account account;
    private Category expenseCategory;
    private Category incomeCategory;

    @BeforeEach
    void setUp() {
        Family family = Family.builder().id(1L).name("Test Family").build();
        owner = User.builder().id(1L).username("testuser").family(family).build();
        account = Account.builder()
                .id(1L).name("Test Account")
                .balance(new BigDecimal("1000.00"))
                .type(Account.AccountType.BANK_ACCOUNT)
                .owner(owner)
                .build();
        expenseCategory = new Category();
        expenseCategory.setId(1L);
        expenseCategory.setName("Mancare");
        expenseCategory.setType(Category.CategoryType.EXPENSE);

        incomeCategory = new Category();
        incomeCategory.setId(2L);
        incomeCategory.setName("Salariu");
        incomeCategory.setType(Category.CategoryType.INCOME);
    }

    @Test
    @DisplayName("findAllForUser returneaza tranzactiile paginat")
    void findAllForUser_returnsPaginatedTransactions() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Transaction> page = new PageImpl<>(List.of(new Transaction()));
        when(transactionRepository.findByAccountOwnerUsername("testuser", pageable)).thenReturn(page);

        Page<Transaction> result = transactionService.findAllForUser("testuser", pageable);

        assertThat(result.getTotalElements()).isEqualTo(1);
        verify(transactionRepository).findByAccountOwnerUsername("testuser", pageable);
    }

    @Test
    @DisplayName("findById arunca ResourceNotFoundException pentru id inexistent")
    void findById_throwsNotFoundException() {
        when(transactionRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> transactionService.findById(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("999");
    }

    @Test
    @DisplayName("create scade soldul contului pentru o cheltuiala")
    void create_decreasesBalanceForExpense() {
        TransactionForm form = new TransactionForm();
        form.setAmount(new BigDecimal("150.00"));
        form.setTransactionDate(LocalDate.now());
        form.setAccountId(1L);
        form.setCategoryId(1L);

        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(expenseCategory));
        Transaction saved = Transaction.builder()
                .id(10L).amount(form.getAmount())
                .account(account).category(expenseCategory)
                .transactionDate(LocalDate.now()).build();
        when(transactionRepository.save(any(Transaction.class))).thenReturn(saved);
        when(accountRepository.save(any(Account.class))).thenReturn(account);

        transactionService.create(form, "testuser");

        assertThat(account.getBalance()).isEqualByComparingTo("850.00");
        verify(accountRepository, times(1)).save(account);
    }

    @Test
    @DisplayName("create creste soldul contului pentru un venit")
    void create_increasesBalanceForIncome() {
        TransactionForm form = new TransactionForm();
        form.setAmount(new BigDecimal("3000.00"));
        form.setTransactionDate(LocalDate.now());
        form.setAccountId(1L);
        form.setCategoryId(2L);

        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));
        when(categoryRepository.findById(2L)).thenReturn(Optional.of(incomeCategory));
        Transaction saved = Transaction.builder()
                .id(11L).amount(form.getAmount())
                .account(account).category(incomeCategory)
                .transactionDate(LocalDate.now()).build();
        when(transactionRepository.save(any())).thenReturn(saved);
        when(accountRepository.save(any())).thenReturn(account);

        transactionService.create(form, "testuser");

        assertThat(account.getBalance()).isEqualByComparingTo("4000.00");
    }

    @Test
    @DisplayName("create arunca AccessDeniedException cand contul apartine altcuiva")
    void create_throwsAccessDenied_whenAccountNotOwnedByUser() {
        User otherUser = User.builder().id(2L).username("otheruser").build();
        Account otherAccount = Account.builder()
                .id(2L).name("Other").balance(BigDecimal.ZERO)
                .type(Account.AccountType.CASH).owner(otherUser).build();

        TransactionForm form = new TransactionForm();
        form.setAmount(BigDecimal.TEN);
        form.setTransactionDate(LocalDate.now());
        form.setAccountId(2L);
        form.setCategoryId(1L);

        when(accountRepository.findById(2L)).thenReturn(Optional.of(otherAccount));

        assertThatThrownBy(() -> transactionService.create(form, "testuser"))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    @DisplayName("delete reverseaza modificarea de sold si sterge tranzactia")
    void delete_reversesBalanceAndDeletes() {
        Transaction existing = Transaction.builder()
                .id(5L).amount(new BigDecimal("200.00"))
                .account(account).category(expenseCategory)
                .transactionDate(LocalDate.now()).build();

        when(transactionRepository.findById(5L)).thenReturn(Optional.of(existing));
        when(accountRepository.save(any())).thenReturn(account);

        transactionService.delete(5L);

        assertThat(account.getBalance()).isEqualByComparingTo("1200.00");
        verify(transactionRepository).delete(existing);
    }
}
