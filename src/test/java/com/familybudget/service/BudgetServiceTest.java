package com.familybudget.service;

import com.familybudget.dto.BudgetForm;
import com.familybudget.entity.*;
import com.familybudget.exception.DuplicateResourceException;
import com.familybudget.exception.ResourceNotFoundException;
import com.familybudget.repository.BudgetRepository;
import com.familybudget.repository.CategoryRepository;
import com.familybudget.repository.FamilyRepository;
import com.familybudget.repository.TransactionRepository;
import com.familybudget.service.impl.BudgetServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BudgetServiceTest {

    @Mock BudgetRepository budgetRepository;
    @Mock CategoryRepository categoryRepository;
    @Mock FamilyRepository familyRepository;
    @Mock TransactionRepository transactionRepository;

    @InjectMocks BudgetServiceImpl budgetService;

    private Family family;
    private Category category;

    @BeforeEach
    void setUp() {
        family = Family.builder().id(1L).name("Test Family").build();
        category = new Category();
        category.setId(1L);
        category.setName("Mancare");
        category.setType(Category.CategoryType.EXPENSE);
    }

    @Test
    @DisplayName("create salveaza bugetul cand nu exista duplicat")
    void create_savesWhenNoDuplicate() {
        BudgetForm form = new BudgetForm(null, new BigDecimal("500.00"), 6, 2024, 1L);

        when(familyRepository.findById(1L)).thenReturn(Optional.of(family));
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(budgetRepository.findByFamilyIdAndCategoryIdAndYearAndMonth(1L, 1L, 2024, 6))
                .thenReturn(Optional.empty());
        Budget saved = Budget.builder()
                .id(1L).limitAmount(form.getLimitAmount())
                .month(form.getMonth()).year(form.getYear())
                .category(category).family(family).build();
        when(budgetRepository.save(any())).thenReturn(saved);

        Budget result = budgetService.create(form, 1L);

        assertThat(result.getLimitAmount()).isEqualByComparingTo("500.00");
        verify(budgetRepository).save(any(Budget.class));
    }

    @Test
    @DisplayName("create arunca DuplicateResourceException cand bugetul exista deja")
    void create_throwsDuplicateWhenExists() {
        BudgetForm form = new BudgetForm(null, BigDecimal.TEN, 6, 2024, 1L);
        Budget existing = Budget.builder().id(99L).build();

        when(familyRepository.findById(1L)).thenReturn(Optional.of(family));
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(budgetRepository.findByFamilyIdAndCategoryIdAndYearAndMonth(1L, 1L, 2024, 6))
                .thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> budgetService.create(form, 1L))
                .isInstanceOf(DuplicateResourceException.class);
        verify(budgetRepository, never()).save(any());
    }

    @Test
    @DisplayName("findById arunca exceptie pentru id inexistent")
    void findById_throwsNotFound() {
        when(budgetRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> budgetService.findById(999L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("calculateSpentAmount returneaza zero cand nu sunt tranzactii")
    void calculateSpentAmount_returnsZeroWhenNoTransactions() {
        Budget budget = Budget.builder()
                .id(1L).month(6).year(2024)
                .category(category).family(family).build();

        when(transactionRepository.sumByCategoryAndFamilyAndMonth(1L, 1L, 6, 2024))
                .thenReturn(null);

        BigDecimal result = budgetService.calculateSpentAmount(budget);

        assertThat(result).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("delete sterge bugetul existent")
    void delete_deletesExistingBudget() {
        Budget budget = Budget.builder().id(1L).build();
        when(budgetRepository.findById(1L)).thenReturn(Optional.of(budget));

        budgetService.delete(1L);

        verify(budgetRepository).delete(budget);
    }
}
