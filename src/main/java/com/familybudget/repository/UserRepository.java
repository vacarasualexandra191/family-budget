package com.familybudget.repository;

import com.familybudget.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    Page<User> findByFamilyId(Long familyId, Pageable pageable);

    Page<User> findByFullNameContainingIgnoreCase(String name, Pageable pageable);
}
