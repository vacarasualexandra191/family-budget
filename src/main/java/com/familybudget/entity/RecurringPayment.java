package com.familybudget.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "recurring_payments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecurringPayment {

    public enum Frequency {
        WEEKLY, MONTHLY, YEARLY
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Numele platii este obligatoriu")
    @Column(nullable = false, length = 100)
    private String name;

    @NotNull(message = "Suma este obligatorie")
    @Positive(message = "Suma trebuie sa fie pozitiva")
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Frequency frequency;

    @NotNull(message = "Data urmatoarei plati este obligatorie")
    @Column(name = "next_due_date", nullable = false)
    private LocalDate nextDueDate;

    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false, unique = true)
    private Account account;
}
