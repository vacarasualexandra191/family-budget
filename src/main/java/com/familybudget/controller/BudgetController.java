package com.familybudget.controller;

import com.familybudget.dto.BudgetForm;
import com.familybudget.entity.Budget;
import com.familybudget.entity.User;
import com.familybudget.service.BudgetService;
import com.familybudget.service.CategoryService;
import com.familybudget.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

@Controller
@RequestMapping("/budgets")
@RequiredArgsConstructor
public class BudgetController {

    private final BudgetService budgetService;
    private final CategoryService categoryService;
    private final UserService userService;

    @GetMapping
    public String list(@AuthenticationPrincipal UserDetails userDetails,
                       @RequestParam(defaultValue = "0") int page,
                       @RequestParam(defaultValue = "10") int size,
                       @RequestParam(defaultValue = "year") String sortBy,
                       @RequestParam(defaultValue = "desc") String sortDir,
                       Model model) {
        User user = userService.findByUsername(userDetails.getUsername());
        if (user.getFamily() == null) {
            model.addAttribute("warningMessage", "Nu esti asociat cu nicio familie.");
            model.addAttribute("sortBy", sortBy);
            model.addAttribute("sortDir", sortDir);
            return "budgets/list";
        }

        Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Page<Budget> budgets = budgetService.findAllForFamily(
                user.getFamily().getId(), PageRequest.of(page, size, sort));

        Map<Budget, Integer> budgetProgress = new LinkedHashMap<>();
        budgets.forEach(b -> {
            BigDecimal spent = budgetService.calculateSpentAmount(b);
            int pct = 0;
            if (b.getLimitAmount().signum() > 0) {
                pct = spent.multiply(java.math.BigDecimal.valueOf(100))
                        .divide(b.getLimitAmount(), 0, java.math.RoundingMode.HALF_UP)
                        .min(java.math.BigDecimal.valueOf(100))
                        .intValue();
            }
            budgetProgress.put(b, pct);
        });

        model.addAttribute("budgets", budgets);
        model.addAttribute("budgetProgress", budgetProgress);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", budgets.getTotalPages());
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("sortDir", sortDir);
        model.addAttribute("reverseSortDir", sortDir.equals("asc") ? "desc" : "asc");
        return "budgets/list";
    }


    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("budgetForm", new BudgetForm());
        model.addAttribute("categories", categoryService.findAll());
        return "budgets/form";
    }

    @PostMapping("/new")
    public String create(@AuthenticationPrincipal UserDetails userDetails,
                         @Valid @ModelAttribute("budgetForm") BudgetForm form,
                         BindingResult result, Model model,
                         RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("categories", categoryService.findAll());
            return "budgets/form";
        }
        User user = userService.findByUsername(userDetails.getUsername());
        budgetService.create(form, user.getFamily().getId());
        redirectAttributes.addFlashAttribute("successMessage", "Buget creat cu succes!");
        return "redirect:/budgets";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        Budget b = budgetService.findById(id);
        BudgetForm form = new BudgetForm(b.getId(), b.getLimitAmount(),
                b.getMonth(), b.getYear(), b.getCategory().getId());
        model.addAttribute("budgetForm", form);
        model.addAttribute("categories", categoryService.findAll());
        return "budgets/form";
    }

    @PostMapping("/{id}/edit")
    public String update(@PathVariable Long id,
                         @Valid @ModelAttribute("budgetForm") BudgetForm form,
                         BindingResult result, Model model,
                         RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("categories", categoryService.findAll());
            return "budgets/form";
        }
        budgetService.update(id, form);
        redirectAttributes.addFlashAttribute("successMessage", "Buget actualizat cu succes!");
        return "redirect:/budgets";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        budgetService.delete(id);
        redirectAttributes.addFlashAttribute("successMessage", "Buget sters cu succes!");
        return "redirect:/budgets";
    }
}
