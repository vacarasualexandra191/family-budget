package com.familybudget.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RegistrationForm {

    @NotBlank(message = "Numele de utilizator este obligatoriu")
    @Size(min = 3, max = 50, message = "Numele de utilizator trebuie sa aiba intre 3 si 50 de caractere")
    private String username;

    @NotBlank(message = "Parola este obligatorie")
    @Size(min = 6, message = "Parola trebuie sa aiba minimum 6 caractere")
    private String password;

    @NotBlank(message = "Numele complet este obligatoriu")
    private String fullName;

    @NotBlank(message = "Email-ul este obligatoriu")
    @Email(message = "Adresa de email nu este valida")
    private String email;

    @NotBlank(message = "Numele familiei este obligatoriu")
    private String familyName;
}
