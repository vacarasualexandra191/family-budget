package com.familybudget.service;

import com.familybudget.dto.RegistrationForm;
import com.familybudget.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserService {

    User register(RegistrationForm form);

    Page<User> findAll(Pageable pageable);

    User findById(Long id);

    User findByUsername(String username);

    void delete(Long id);
}
