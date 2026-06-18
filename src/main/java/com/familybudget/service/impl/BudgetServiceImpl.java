package com.familybudget.service.impl;

import com.familybudget.dto.BudgetForm;
import com.familybudget.entity.Budget;
import com.familybudget.entity.Category;
import com.familybudget.entity.Family;
import com.familybudget.exception.DuplicateResourceException;
import com.familybudget.exception.ResourceNotFoundException;
import com.familybudget.repository.BudgetRepository;
import com.familybudget.repository.CategoryRepository;
import com.familybudget.repository.FamilyRepository;
import com.familybudget.repository.TransactionRepository;
import com.familybudget.service.BudgetService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Slf4j
public class BudgetServiceImpl implements BudgetService {

    private final BudgetRepository budgetRepository;
    private final CategoryRepository categoryRepository;
    private final FamilyRepository familyRepository;
    private final TransactionRepository transactionRepository;

    @Override
    public Page<Budget> findAllForFamily(Long familyId, Pageable pageable) {
        return budgetRepository.findByFamilyId(familyId, pageable);
    }

    @Override
    public Budget findById(Long id) {
        return budgetRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Bugetul", id));
    }

    @Override
    @Transactional
    public Budget create(BudgetForm form, Long familyId) {
        Family family = familyRepository.findById(familyId)
                .orElseThrow(() -> ResourceNotFoundException.of("Familia", familyId));
        Category category = categoryRepository.findById(form.getCategoryId())
                .orElseThrow(() -> ResourceNotFoundException.of("Categoria", form.getCategoryId()));

        budgetRepository.findByFamilyIdAndCategoryIdAndYearAndMonth(
                familyId, category.getId(), form.getYear(), form.getMonth()
        ).ifPresent(b -> {
            throw new DuplicateResourceException(
                    "Exista deja un buget pentru categoria " + category.getName() +
                    " in luna " + form.getMonth() + "/" + form.getYear());
        });

        Budget budget = Budget.builder()
                .limitAmount(form.getLimitAmount())
                .month(form.getMonth())
                .year(form.getYear())
                .category(category)
                .family(family)
                .build();

        Budget saved = budgetRepository.save(budget);
        log.info("Buget creat: id={}, categorie={}, limita={}", saved.getId(), category.getName(), form.getLimitAmount());
        return saved;
    }

    @Override
    @Transactional
    public Budget update(Long id, BudgetForm form) {
        Budget existing = findById(id);
        Category category = categoryRepository.findById(form.getCategoryId())
                .orElseThrow(() -> ResourceNotFoundException.of("Categoria", form.getCategoryId()));

        existing.setLimitAmount(form.getLimitAmount());
        existing.setMonth(form.getMonth());
        existing.setYear(form.getYear());
        existing.setCategory(category);

        Budget updated = budgetRepository.save(existing);
        log.info("Buget actualizat: id={}", updated.getId());
        return updated;
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Budget budget = findById(id);
        budgetRepository.delete(budget);
        log.info("Buget sters: id={}", id);
    }

    @Override
    public BigDecimal calculateSpentAmount(Budget budget) {
        BigDecimal sum = transactionRepository.sumByCategoryAndFamilyAndMonth(
                budget.getCategory().getId(),
                budget.getFamily().getId(),
                budget.getMonth(),
                budget.getYear()
        );
        return sum == null ? BigDecimal.ZERO : sum;
    }
}
