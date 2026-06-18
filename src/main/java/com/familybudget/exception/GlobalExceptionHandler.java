package com.familybudget.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

/**
 * Trateaza centralizat exceptiile aruncate din controllere MVC (Thymeleaf views).
 * Pentru exceptii din API REST (daca exista), Spring va folosi raspunsuri JSON implicite
 * sau pot fi extinse separat cu @ResponseBody.
 */
@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public String handleNotFound(ResourceNotFoundException ex, Model model, HttpServletRequest request) {
        log.warn("Resursa negasita la {}: {}", request.getRequestURI(), ex.getMessage());
        model.addAttribute("errorMessage", ex.getMessage());
        model.addAttribute("status", 404);
        return "error/404";
    }

    @ExceptionHandler(DuplicateResourceException.class)
    public String handleDuplicate(DuplicateResourceException ex, Model model, HttpServletRequest request) {
        log.warn("Resursa duplicata la {}: {}", request.getRequestURI(), ex.getMessage());
        model.addAttribute("errorMessage", ex.getMessage());
        model.addAttribute("status", 409);
        return "error/generic";
    }

    @ExceptionHandler(InsufficientFundsException.class)
    public String handleInsufficientFunds(InsufficientFundsException ex, Model model, HttpServletRequest request) {
        log.warn("Fonduri insuficiente la {}: {}", request.getRequestURI(), ex.getMessage());
        model.addAttribute("errorMessage", ex.getMessage());
        model.addAttribute("status", 400);
        return "error/generic";
    }

    @ExceptionHandler(AccessDeniedException.class)
    public String handleAccessDenied(AccessDeniedException ex, Model model, HttpServletRequest request) {
        log.warn("Acces interzis la {}: {}", request.getRequestURI(), ex.getMessage());
        model.addAttribute("errorMessage", "Nu ai permisiunea necesara pentru aceasta actiune.");
        model.addAttribute("status", 403);
        return "error/403";
    }

    @ExceptionHandler(Exception.class)
    public String handleGeneric(Exception ex, Model model, HttpServletRequest request) {
        log.error("Eroare neasteptata la {}: {}", request.getRequestURI(), ex.getMessage(), ex);
        model.addAttribute("errorMessage", "A aparut o eroare neasteptata. Va rugam incercati din nou.");
        model.addAttribute("status", 500);
        return "error/500";
    }
}
