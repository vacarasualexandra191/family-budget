package com.familybudget.repository;

import com.familybudget.entity.Family;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FamilyRepository extends JpaRepository<Family, Long> {

    Page<Family> findByNameContainingIgnoreCase(String name, Pageable pageable);
}
