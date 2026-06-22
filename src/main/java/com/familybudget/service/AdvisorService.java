package com.familybudget.service;

import com.familybudget.entity.Budget;
import com.familybudget.entity.User;

import java.util.List;

public interface AdvisorService {

    /**
     * Genereaza recomandari personalizate de buget pentru utilizator,
     * bazate pe analiza tranzactiilor si bugetelor existente.
     */
    List<String> generateBudgetAdvice(User user);
}
