package com.familybudget.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TransactionForm {

    private Long id;

    @NotNull(message = "Suma este obligatorie")
    @Positive(message = "Suma trebuie sa fie un numar pozitiv")
    private BigDecimal amount;

    @NotNull(message = "Data este obligatorie")
    private LocalDate transactionDate = LocalDate.now();

    private String description;

    @NotNull(message = "Trebuie sa selectezi un cont")
    private Long accountId;

    @NotNull(message = "Trebuie sa selectezi o categorie")
    private Long categoryId;

    private Set<Long> tagIds = new HashSet<>();
}
