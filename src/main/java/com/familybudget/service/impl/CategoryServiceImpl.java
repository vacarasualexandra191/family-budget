package com.familybudget.service.impl;

import com.familybudget.entity.Category;
import com.familybudget.exception.DuplicateResourceException;
import com.familybudget.exception.ResourceNotFoundException;
import com.familybudget.repository.CategoryRepository;
import com.familybudget.service.CategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    @Override
    public List<Category> findAll() {
        return categoryRepository.findAll();
    }

    @Override
    public Page<Category> findAllPaged(Pageable pageable) {
        return categoryRepository.findAll(pageable);
    }

    @Override
    public Category findById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Categoria", id));
    }

    @Override
    @Transactional
    public Category create(Category category) {
        if (categoryRepository.existsByNameIgnoreCase(category.getName())) {
            throw new DuplicateResourceException("Categoria '" + category.getName() + "' exista deja");
        }
        Category saved = categoryRepository.save(category);
        log.info("Categorie creata: id={}, nume={}", saved.getId(), saved.getName());
        return saved;
    }

    @Override
    @Transactional
    public Category update(Long id, Category category) {
        Category existing = findById(id);
        existing.setName(category.getName());
        existing.setType(category.getType());
        existing.setIcon(category.getIcon());
        Category updated = categoryRepository.save(existing);
        log.info("Categorie actualizata: id={}", updated.getId());
        return updated;
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Category category = findById(id);
        categoryRepository.delete(category);
        log.info("Categorie stearsa: id={}", id);
    }
}
