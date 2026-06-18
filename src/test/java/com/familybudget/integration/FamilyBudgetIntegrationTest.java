package com.familybudget.integration;

import com.familybudget.dto.AccountForm;
import com.familybudget.dto.BudgetForm;
import com.familybudget.dto.RegistrationForm;
import com.familybudget.dto.TransactionForm;
import com.familybudget.entity.*;
import com.familybudget.repository.*;
import com.familybudget.service.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class FamilyBudgetIntegrationTest {

    @Autowired UserService userService;
    @Autowired AccountService accountService;
    @Autowired TransactionService transactionService;
    @Autowired BudgetService budgetService;
    @Autowired CategoryRepository categoryRepository;
    @Autowired RoleRepository roleRepository;

    private User testUser;
    private Account testAccount;
    private Category expenseCategory;

    @BeforeEach
    void setUp() {
        if (roleRepository.findByName("ROLE_USER").isEmpty()) {
            roleRepository.save(new Role("ROLE_USER"));
        }
        if (roleRepository.findByName("ROLE_ADMIN").isEmpty()) {
            roleRepository.save(new Role("ROLE_ADMIN"));
        }

        // Creeaza categorie direct
        expenseCategory = new Category();
        expenseCategory.setName("Mancare_" + System.currentTimeMillis());
        expenseCategory.setType(Category.CategoryType.EXPENSE);
        expenseCategory = categoryRepository.save(expenseCategory);

        RegistrationForm form = new RegistrationForm(
                "integtest_" + System.currentTimeMillis(),
                "parola123", "Ion Test", System.currentTimeMillis() + "@test.ro", "Familia Test");
        testUser = userService.register(form);

        AccountForm accountForm = new AccountForm(null, "Cont Test",
                Account.AccountType.BANK_ACCOUNT, new BigDecimal("2000.00"), "RON");
        testAccount = accountService.create(accountForm, testUser.getUsername());
    }

    @Test
    @DisplayName("Scenariu 1: inregistrare utilizator si creare cont")
    void scenario1_registerAndCreateAccount() {
        assertThat(testUser.getId()).isNotNull();
        assertThat(testUser.getFamily()).isNotNull();
        assertThat(testAccount.getId()).isNotNull();
        assertThat(testAccount.getBalance()).isEqualByComparingTo("2000.00");
    }

    @Test
    @DisplayName("Scenariu 2: adaugare cheltuiala si verificare sold")
    void scenario2_addExpenseAndVerifyBalance() {
        TransactionForm form = new TransactionForm();
        form.setAmount(new BigDecimal("350.00"));
        form.setTransactionDate(LocalDate.now());
        form.setAccountId(testAccount.getId());
        form.setCategoryId(expenseCategory.getId());
        form.setDescription("Test cheltuiala");

        Transaction transaction = transactionService.create(form, testUser.getUsername());

        assertThat(transaction.getId()).isNotNull();
        assertThat(transaction.getAmount()).isEqualByComparingTo("350.00");

        Account refreshed = accountService.findById(testAccount.getId());
        assertThat(refreshed.getBalance()).isEqualByComparingTo("1650.00");
    }

    @Test
    @DisplayName("Scenariu 3: creare buget si verificare suma cheltuita")
    void scenario3_createBudgetAndTrackSpending() {
        BudgetForm budgetForm = new BudgetForm(null, new BigDecimal("500.00"),
                LocalDate.now().getMonthValue(), LocalDate.now().getYear(),
                expenseCategory.getId());
        Budget budget = budgetService.create(budgetForm, testUser.getFamily().getId());

        TransactionForm txForm = new TransactionForm();
        txForm.setAmount(new BigDecimal("200.00"));
        txForm.setTransactionDate(LocalDate.now());
        txForm.setAccountId(testAccount.getId());
        txForm.setCategoryId(expenseCategory.getId());
        transactionService.create(txForm, testUser.getUsername());

        BigDecimal spent = budgetService.calculateSpentAmount(budget);
        assertThat(spent).isEqualByComparingTo("200.00");
        assertThat(spent).isLessThan(budget.getLimitAmount());
    }
}