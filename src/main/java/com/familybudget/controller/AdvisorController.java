package com.familybudget.controller;

import com.familybudget.entity.User;
import com.familybudget.service.AdvisorService;
import com.familybudget.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;


@Controller
@RequiredArgsConstructor
public class AdvisorController {

    private final AdvisorService advisorService;
    private final UserService userService;

    @GetMapping("/advisor")
    public String advisorPage(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User user = userService.findByUsername(userDetails.getUsername());
        var advice = advisorService.generateBudgetAdvice(user);

        model.addAttribute("advice", advice);
        model.addAttribute("user", user);
        return "advisor";
    }
}
