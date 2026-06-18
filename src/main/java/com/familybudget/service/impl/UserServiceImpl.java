package com.familybudget.service.impl;

import com.familybudget.dto.RegistrationForm;
import com.familybudget.entity.Family;
import com.familybudget.entity.Role;
import com.familybudget.entity.User;
import com.familybudget.exception.DuplicateResourceException;
import com.familybudget.exception.ResourceNotFoundException;
import com.familybudget.repository.FamilyRepository;
import com.familybudget.repository.RoleRepository;
import com.familybudget.repository.UserRepository;
import com.familybudget.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final FamilyRepository familyRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public User register(RegistrationForm form) {
        if (userRepository.existsByUsername(form.getUsername())) {
            throw new DuplicateResourceException("Numele de utilizator '" + form.getUsername() + "' este deja folosit");
        }
        if (userRepository.existsByEmail(form.getEmail())) {
            throw new DuplicateResourceException("Adresa de email este deja folosita");
        }

        Family family = Family.builder()
                .name(form.getFamilyName())
                .build();
        family = familyRepository.save(family);

        Role userRole = roleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> new ResourceNotFoundException("Rolul ROLE_USER nu a fost gasit. Verifica datele de initializare."));

        User user = User.builder()
                .username(form.getUsername())
                .password(passwordEncoder.encode(form.getPassword()))
                .fullName(form.getFullName())
                .email(form.getEmail())
                .enabled(true)
                .family(family)
                .roles(new HashSet<>(java.util.Set.of(userRole)))
                .build();

        User saved = userRepository.save(user);
        log.info("Utilizator nou inregistrat: username={}, familie={}", saved.getUsername(), family.getName());
        return saved;
    }

    @Override
    public Page<User> findAll(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    @Override
    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Utilizatorul", id));
    }

    @Override
    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Utilizatorul '" + username + "' nu a fost gasit"));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        User user = findById(id);
        userRepository.delete(user);
        log.info("Utilizator sters: id={}", id);
    }
}
