package com.familybudget.controller;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {
    @GetMapping("/test-hash")
    public String hash() {
        return new BCryptPasswordEncoder().encode("admin123");
    }
}