package com.familybudget.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.JdbcUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

import javax.sql.DataSource;

/**
 * Configuratie de securitate.
 * Cerinte acoperite:
 *  - Autentificare JDBC: JdbcUserDetailsManager cu query-uri custom peste tabelele
 *    "users" / "roles" / "user_roles" (acelasi model de date JPA, fara schema separata).
 *  - Minimum 2 roluri: ROLE_USER, ROLE_ADMIN.
 *  - Protejarea endpoint-urilor bazata pe rol.
 *  - Pagina de login custom + logout functional.
 *  - Password encoding cu BCrypt.
 *  - Remember-me functionality.
 *  - CSRF protection activa (implicit in Spring Security, NU este dezactivata).
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * JdbcUserDetailsManager configurat cu query-uri SQL explicite peste schema noastra
     * (users, roles, user_roles), in loc de schema implicita Spring Security.
     * Aceasta este implementarea "Autentificare JDBC" ceruta.
     */
    @Bean
    public JdbcUserDetailsManager userDetailsManager(DataSource dataSource) {
        JdbcUserDetailsManager manager = new JdbcUserDetailsManager(dataSource);

        manager.setUsersByUsernameQuery(
                "SELECT username, password, enabled FROM users WHERE username = ?"
        );

        manager.setAuthoritiesByUsernameQuery(
                "SELECT u.username, r.name AS authority " +
                "FROM users u " +
                "JOIN user_roles ur ON ur.user_id = u.id " +
                "JOIN roles r ON r.id = ur.role_id " +
                "WHERE u.username = ?"
        );

        return manager;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                    .requestMatchers("/css/**", "/js/**", "/webjars/**", "/register", "/login", "/test-hash").permitAll()
                .requestMatchers("/actuator/health").permitAll()
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .requestMatchers("/categories/**").hasAnyRole("ADMIN", "USER")
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .defaultSuccessUrl("/dashboard", true)
                .failureUrl("/login?error=true")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout=true")
                .permitAll()
            )
            .rememberMe(remember -> remember
                .key("family-budget-remember-me-key")
                .tokenValiditySeconds(7 * 24 * 60 * 60) // 7 zile
            );
            // CSRF protection este activata implicit (nu se dezactiveaza) -
            // formularele Thymeleaf includ automat tokenul via th:action.

        return http.build();
    }
}
