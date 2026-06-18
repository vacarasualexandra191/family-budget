package com.familybudget.controller;

import com.familybudget.dto.AccountForm;
import com.familybudget.entity.Account;
import com.familybudget.service.AccountService;
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

@Controller
@RequestMapping("/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @GetMapping
    public String list(@AuthenticationPrincipal UserDetails userDetails,
                       @RequestParam(defaultValue = "0") int page,
                       @RequestParam(defaultValue = "10") int size,
                       @RequestParam(defaultValue = "name") String sortBy,
                       @RequestParam(defaultValue = "asc") String sortDir,
                       Model model) {
        Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Page<Account> accounts = accountService.findAllForUser(
                userDetails.getUsername(), PageRequest.of(page, size, sort));

        model.addAttribute("accounts", accounts);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", accounts.getTotalPages());
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("sortDir", sortDir);
        model.addAttribute("reverseSortDir", sortDir.equals("asc") ? "desc" : "asc");
        model.addAttribute("accountTypes", Account.AccountType.values());
        return "accounts/list";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("accountForm", new AccountForm());
        model.addAttribute("accountTypes", Account.AccountType.values());
        return "accounts/form";
    }

    @PostMapping("/new")
    public String create(@AuthenticationPrincipal UserDetails userDetails,
                         @Valid @ModelAttribute("accountForm") AccountForm form,
                         BindingResult result, Model model,
                         RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("accountTypes", Account.AccountType.values());
            return "accounts/form";
        }
        accountService.create(form, userDetails.getUsername());
        redirectAttributes.addFlashAttribute("successMessage", "Cont creat cu succes!");
        return "redirect:/accounts";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        Account account = accountService.findById(id);
        AccountForm form = new AccountForm(
                account.getId(), account.getName(), account.getType(),
                account.getBalance(), account.getCurrency());
        model.addAttribute("accountForm", form);
        model.addAttribute("accountTypes", Account.AccountType.values());
        return "accounts/form";
    }

    @PostMapping("/{id}/edit")
    public String update(@PathVariable Long id,
                         @Valid @ModelAttribute("accountForm") AccountForm form,
                         BindingResult result, Model model,
                         RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("accountTypes", Account.AccountType.values());
            return "accounts/form";
        }
        accountService.update(id, form);
        redirectAttributes.addFlashAttribute("successMessage", "Cont actualizat cu succes!");
        return "redirect:/accounts";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        accountService.delete(id);
        redirectAttributes.addFlashAttribute("successMessage", "Cont sters cu succes!");
        return "redirect:/accounts";
    }
}
