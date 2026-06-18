package com.familybudget.repository;

import com.familybudget.entity.Budget;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BudgetRepository extends JpaRepository<Budget, Long> {

    Page<Budget> findByFamilyId(Long familyId, Pageable pageable);

    Page<Budget> findByFamilyIdAndYearAndMonth(Long familyId, Integer year, Integer month, Pageable pageable);

    Optional<Budget> findByFamilyIdAndCategoryIdAndYearAndMonth(
            Long familyId, Long categoryId, Integer year, Integer month);
}
