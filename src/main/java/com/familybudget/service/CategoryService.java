package com.familybudget.service;

import com.familybudget.entity.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CategoryService {

    List<Category> findAll();

    Page<Category> findAllPaged(Pageable pageable);

    Category findById(Long id);

    Category create(Category category);

    Category update(Long id, Category category);

    void delete(Long id);
}
