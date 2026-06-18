package com.familybudget.repository;

import com.familybudget.entity.Account;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {

    Page<Account> findByOwnerId(Long ownerId, Pageable pageable);

    Page<Account> findByOwnerUsername(String username, Pageable pageable);

    long countByOwnerId(Long ownerId);
}
