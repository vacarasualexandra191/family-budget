package com.familybudget.dto;

import com.familybudget.entity.Account;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AccountForm {

    private Long id;

    @NotBlank(message = "Numele contului este obligatoriu")
    private String name;

    @NotNull(message = "Tipul contului este obligatoriu")
    private Account.AccountType type;

    @NotNull(message = "Soldul initial este obligatoriu")
    private BigDecimal balance;

    private String currency = "RON";
}
