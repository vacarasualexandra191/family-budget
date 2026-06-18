package com.familybudget.repository;

import com.familybudget.entity.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    Page<Category> findByType(Category.CategoryType type, Pageable pageable);

    boolean existsByNameIgnoreCase(String name);
}
