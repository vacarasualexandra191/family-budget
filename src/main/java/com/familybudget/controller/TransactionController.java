package com.familybudget.controller;

import com.familybudget.dto.TransactionForm;
import com.familybudget.entity.Transaction;
import com.familybudget.service.AccountService;
import com.familybudget.service.CategoryService;
import com.familybudget.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@RequestMapping("/transactions")
@RequiredArgsConstructor
@Slf4j
public class TransactionController {

    private final TransactionService transactionService;
    private final AccountService accountService;
    private final CategoryService categoryService;
    private final com.familybudget.repository.TagRepository tagRepository;

    @GetMapping
    public String list(@AuthenticationPrincipal UserDetails userDetails,
                       @RequestParam(defaultValue = "0") int page,
                       @RequestParam(defaultValue = "15") int size,
                       @RequestParam(defaultValue = "transactionDate") String sortBy,
                       @RequestParam(defaultValue = "desc") String sortDir,
                       Model model) {

        Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Page<Transaction> transactions = transactionService.findAllForUser(
                userDetails.getUsername(), PageRequest.of(page, size, sort));

        model.addAttribute("transactions", transactions);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", transactions.getTotalPages());
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("sortDir", sortDir);
        model.addAttribute("reverseSortDir", sortDir.equals("asc") ? "desc" : "asc");
        return "transactions/list";
    }

    @GetMapping("/new")
    public String createForm(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        model.addAttribute("transactionForm", new TransactionForm());
        model.addAttribute("accounts", accountService.findAllForUser(
                userDetails.getUsername(), PageRequest.of(0, 100)).getContent());
        model.addAttribute("categories", categoryService.findAll());
        model.addAttribute("tags", tagRepository.findAll());
        return "transactions/form";
    }

    @PostMapping("/new")
    public String create(@AuthenticationPrincipal UserDetails userDetails,
                         @Valid @ModelAttribute("transactionForm") TransactionForm form,
                         BindingResult result,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("accounts", accountService.findAllForUser(
                    userDetails.getUsername(), PageRequest.of(0, 100)).getContent());
            model.addAttribute("categories", categoryService.findAll());
            model.addAttribute("tags", tagRepository.findAll());
            return "transactions/form";
        }
        transactionService.create(form, userDetails.getUsername());
        redirectAttributes.addFlashAttribute("successMessage", "Tranzactie adaugata cu succes!");
        return "redirect:/transactions";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model,
                           @AuthenticationPrincipal UserDetails userDetails) {
        Transaction t = transactionService.findById(id);
        TransactionForm form = new TransactionForm();
        form.setId(t.getId());
        form.setAmount(t.getAmount());
        form.setTransactionDate(t.getTransactionDate());
        form.setDescription(t.getDescription());
        form.setAccountId(t.getAccount().getId());
        form.setCategoryId(t.getCategory().getId());
        form.setTagIds(t.getTags().stream()
                .map(tag -> tag.getId()).collect(java.util.stream.Collectors.toSet()));

        model.addAttribute("transactionForm", form);
        model.addAttribute("accounts", accountService.findAllForUser(
                userDetails.getUsername(), PageRequest.of(0, 100)).getContent());
        model.addAttribute("categories", categoryService.findAll());
        model.addAttribute("tags", tagRepository.findAll());
        return "transactions/form";
    }

    @PostMapping("/{id}/edit")
    public String update(@PathVariable Long id,
                         @Valid @ModelAttribute("transactionForm") TransactionForm form,
                         BindingResult result,
                         Model model,
                         @AuthenticationPrincipal UserDetails userDetails,
                         RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("accounts", accountService.findAllForUser(
                    userDetails.getUsername(), PageRequest.of(0, 100)).getContent());
            model.addAttribute("categories", categoryService.findAll());
            model.addAttribute("tags", tagRepository.findAll());
            return "transactions/form";
        }
        transactionService.update(id, form);
        redirectAttributes.addFlashAttribute("successMessage", "Tranzactie actualizata cu succes!");
        return "redirect:/transactions";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        transactionService.delete(id);
        redirectAttributes.addFlashAttribute("successMessage", "Tranzactie stearsa cu succes!");
        return "redirect:/transactions";
    }
}
