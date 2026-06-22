package com.familybudget.service.impl;

import com.familybudget.entity.Budget;
import com.familybudget.entity.Category;
import com.familybudget.entity.Transaction;
import com.familybudget.entity.User;
import com.familybudget.repository.BudgetRepository;
import com.familybudget.repository.TransactionRepository;
import com.familybudget.service.AdvisorService;
import com.familybudget.service.BudgetService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Motor de recomandari personalizate ("AI Agent" la nivel de runtime) care
 * analizeaza comportamentul financiar real al utilizatorului (tranzactii si
 * bugete existente in baza de date) si genereaza sfaturi text, similar unui
 * asistent financiar automat.
 *
 * Analiza este bazata pe reguli (rule-based reasoning) peste datele reale ale
 * utilizatorului, fara dependinta de un serviciu AI extern - astfel functioneaza
 * intotdeauna, fara chei API sau costuri, si este complet reproductibila la demo.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AdvisorServiceImpl implements AdvisorService {

    private final TransactionRepository transactionRepository;
    private final BudgetRepository budgetRepository;
    private final BudgetService budgetService;

    @Override
    public List<String> generateBudgetAdvice(User user) {
        List<String> advice = new ArrayList<>();

        if (user.getFamily() == null) {
            advice.add("Nu esti asociat cu nicio familie, asa ca nu putem analiza bugete comune. " +
                    "Adauga-te la o familie pentru recomandari personalizate.");
            return advice;
        }

        LocalDate now = LocalDate.now();
        int month = now.getMonthValue();
        int year = now.getYear();

        // 1. Analizeaza tranzactiile lunii curente
        var transactionsPage = transactionRepository.findByAccountOwnerUsernameAndTransactionDateBetween(
                user.getUsername(),
                now.withDayOfMonth(1),
                now.withDayOfMonth(now.lengthOfMonth()),
                PageRequest.of(0, 500)
        );
        List<Transaction> transactions = transactionsPage.getContent();

        if (transactions.isEmpty()) {
            advice.add("Nu ai nicio tranzactie inregistrata in luna curenta. " +
                    "Adauga tranzactiile pentru a primi recomandari personalizate de buget.");
            return advice;
        }

        // 2. Calculeaza total venituri vs cheltuieli
        BigDecimal totalIncome = BigDecimal.ZERO;
        BigDecimal totalExpense = BigDecimal.ZERO;
        Map<String, BigDecimal> expenseByCategory = new HashMap<>();

        for (Transaction t : transactions) {
            Category cat = t.getCategory();
            if (cat.getType() == Category.CategoryType.INCOME) {
                totalIncome = totalIncome.add(t.getAmount());
            } else {
                totalExpense = totalExpense.add(t.getAmount());
                expenseByCategory.merge(cat.getName(), t.getAmount(), BigDecimal::add);
            }
        }

        // 3. Regula: cheltuielile depasesc veniturile
        if (totalExpense.compareTo(totalIncome) > 0 && totalIncome.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal deficit = totalExpense.subtract(totalIncome);
            advice.add(String.format(
                    "⚠️ Atentie: cheltuielile din luna curenta (%.2f RON) depasesc veniturile (%.2f RON) " +
                    "cu %.2f RON. Recomandam reducerea cheltuielilor discretionare.",
                    totalExpense, totalIncome, deficit));
        }

        // 4. Regula: rata de economisire
        if (totalIncome.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal savingsRate = totalIncome.subtract(totalExpense)
                    .divide(totalIncome, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
            if (savingsRate.compareTo(BigDecimal.valueOf(20)) >= 0) {
                advice.add(String.format(
                        "✅ Felicitari! Economisesti %.1f%% din venituri luna asta - peste recomandarea standard de 20%%.",
                        savingsRate));
            } else if (savingsRate.compareTo(BigDecimal.ZERO) >= 0) {
                advice.add(String.format(
                        "💡 Rata ta de economisire este de %.1f%%. Specialistii financiari recomanda minim 20%%. " +
                        "Incearca sa identifici categorii unde poti reduce cheltuielile.",
                        savingsRate));
            }
        }

        // 5. Regula: categorii de cheltuieli fara buget definit
        var existingBudgets = budgetRepository.findByFamilyIdAndYearAndMonth(
                user.getFamily().getId(), year, month, PageRequest.of(0, 50)).getContent();
        var budgetedCategoryNames = existingBudgets.stream()
                .map(b -> b.getCategory().getName())
                .toList();

        for (Map.Entry<String, BigDecimal> entry : expenseByCategory.entrySet()) {
            if (!budgetedCategoryNames.contains(entry.getKey()) && entry.getValue().compareTo(BigDecimal.valueOf(50)) > 0) {
                advice.add(String.format(
                        "📊 Ai cheltuit %.2f RON pe '%s' luna asta, dar nu ai un buget definit pentru aceasta categorie. " +
                        "Recomandam sa stabilesti o limita lunara.",
                        entry.getValue(), entry.getKey()));
            }
        }

        // 6. Regula: bugete aproape de limita sau depasite
        for (Budget budget : existingBudgets) {
            BigDecimal spent = budgetService.calculateSpentAmount(budget);
            BigDecimal limit = budget.getLimitAmount();
            if (limit.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal pct = spent.multiply(BigDecimal.valueOf(100))
                        .divide(limit, 0, RoundingMode.HALF_UP);
                if (pct.compareTo(BigDecimal.valueOf(100)) >= 0) {
                    advice.add(String.format(
                            "🔴 Bugetul pentru '%s' a fost depasit: %.2f RON cheltuiti din %.2f RON alocati.",
                            budget.getCategory().getName(), spent, limit));
                } else if (pct.compareTo(BigDecimal.valueOf(85)) >= 0) {
                    advice.add(String.format(
                            "🟡 Bugetul pentru '%s' este aproape consumat (%.0f%%): %.2f RON din %.2f RON.",
                            budget.getCategory().getName(), pct, spent, limit));
                }
            }
        }

        // 7. Daca nu exista observatii negative, mesaj pozitiv implicit
        if (advice.isEmpty()) {
            advice.add("✅ Situatia financiara din luna curenta pare echilibrata. Continua sa monitorizezi cheltuielile!");
        }

        log.info("Generat {} recomandari pentru utilizatorul {}", advice.size(), user.getUsername());
        return advice;
    }
}
