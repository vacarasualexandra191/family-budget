package com.familybudget.service;

import com.familybudget.dto.BudgetForm;
import com.familybudget.entity.Budget;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;

public interface BudgetService {

    Page<Budget> findAllForFamily(Long familyId, Pageable pageable);

    Budget findById(Long id);

    Budget create(BudgetForm form, Long familyId);

    Budget update(Long id, BudgetForm form);

    void delete(Long id);

    BigDecimal calculateSpentAmount(Budget budget);
}
