package com.familybudget.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BudgetForm {

    private Long id;

    @NotNull(message = "Suma limita este obligatorie")
    @Positive(message = "Suma limita trebuie sa fie pozitiva")
    private BigDecimal limitAmount;

    @NotNull(message = "Luna este obligatorie")
    @Min(value = 1, message = "Luna trebuie sa fie intre 1 si 12")
    @Max(value = 12, message = "Luna trebuie sa fie intre 1 si 12")
    private Integer month;

    @NotNull(message = "Anul este obligatoriu")
    @Min(value = 2000, message = "Anul trebuie sa fie valid")
    private Integer year;

    @NotNull(message = "Trebuie sa selectezi o categorie")
    private Long categoryId;
}
