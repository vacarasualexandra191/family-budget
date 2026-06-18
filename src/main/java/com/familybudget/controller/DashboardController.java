package com.familybudget.controller;

import com.familybudget.entity.Budget;
import com.familybudget.entity.Transaction;
import com.familybudget.entity.User;
import com.familybudget.service.AccountService;
import com.familybudget.service.BudgetService;
import com.familybudget.service.TransactionService;
import com.familybudget.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedHashMap;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class DashboardController {

    private final TransactionService transactionService;
    private final AccountService accountService;
    private final BudgetService budgetService;
    private final UserService userService;

    @GetMapping({"/", "/dashboard"})
    public String dashboard(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        String username = userDetails.getUsername();
        User user = userService.findByUsername(username);

        Pageable recentPageable = PageRequest.of(0, 10,
                Sort.by(Sort.Direction.DESC, "transactionDate"));
        Page<Transaction> recentTransactions = transactionService.findAllForUser(username, recentPageable);

        var accounts = accountService.findAllForUser(username, PageRequest.of(0, 50));

        // Budget progress: Budget -> procent (0-100)
        Map<Budget, Integer> budgetProgress = new LinkedHashMap<>();
        if (user.getFamily() != null) {
            Page<Budget> budgets = budgetService.findAllForFamily(
                    user.getFamily().getId(), PageRequest.of(0, 20));
            budgets.forEach(b -> {
                BigDecimal spent = budgetService.calculateSpentAmount(b);
                int pct = 0;
                if (b.getLimitAmount().signum() > 0) {
                    pct = spent.multiply(BigDecimal.valueOf(100))
                            .divide(b.getLimitAmount(), 0, RoundingMode.HALF_UP)
                            .min(BigDecimal.valueOf(100))
                            .intValue();
                }
                budgetProgress.put(b, pct);
            });
        }

        model.addAttribute("user", user);
        model.addAttribute("recentTransactions", recentTransactions.getContent());
        model.addAttribute("accounts", accounts.getContent());
        model.addAttribute("budgetProgress", budgetProgress);

        return "dashboard";
    }
}