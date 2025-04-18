package com.novel.vippro.services;

import com.novel.vippro.models.Category;
import com.novel.vippro.payload.response.PageResponse;
import com.novel.vippro.repository.CategoryRepository;
import com.novel.vippro.dto.CategoryDTO;
import com.novel.vippro.dto.NovelDTO;
import com.novel.vippro.exception.ResourceNotFoundException;
import com.novel.vippro.mapper.Mapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Service
public class CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private NovelService novelService;

    @Autowired
    private Mapper mapper;

    @Cacheable(value = "categories", key = "'page-' + #pageable.pageNumber + '-' + #pageable.pageSize")
    public PageResponse<CategoryDTO> getAllCategories(Pageable pageable) {
        Page<Category> page = categoryRepository.findAll(pageable);
        // Convert to DTO if necessary
        return new PageResponse<>(page.map(mapper::CategorytoDTO));
    }

    @Cacheable(value = "categories", key = "#id")
    public Category getCategoryById(UUID id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", id));
    }

    @Transactional
    public Category getCategoryByName(String name) {
        return categoryRepository.findByNameIgnoreCase(name)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "name", name));
    }

    @CacheEvict(value = "categories", allEntries = true)
    @Transactional
    public Category createCategory(Category category) {
        // Generate slug from name if not provided
        if (category.getSlug() == null || category.getSlug().isEmpty()) {
            category.setSlug(category.getName().toLowerCase()
                    .replaceAll("\\s+", "-")
                    .replaceAll("[^a-z0-9-]", ""));
        }
        return categoryRepository.save(category);
    }

    @CacheEvict(value = "categories", key = "#id")
    @Transactional
    public Category updateCategory(UUID id, Category categoryDetails) {
        Category category = getCategoryById(id);

        category.setName(categoryDetails.getName());
        category.setDescription(categoryDetails.getDescription());

        // Update slug if name has changed
        if (!category.getName().equalsIgnoreCase(categoryDetails.getName())) {
            category.setSlug(category.getName().toLowerCase()
                    .replaceAll("\\s+", "-")
                    .replaceAll("[^a-z0-9-]", ""));
        }

        return categoryRepository.save(category);
    }

    @CacheEvict(value = "categories", allEntries = true)
    @Transactional
    public void deleteCategory(UUID id) {
        Category category = getCategoryById(id);
        categoryRepository.delete(category);
    }

    @Transactional
    public Set<Category> getOrCreateCategories(Set<String> categoryNames) {
        Set<Category> result = new HashSet<>();

        // First, find all existing categories
        for (String name : categoryNames) {
            try {
                Category category = getCategoryByName(name);
                result.add(category);
            } catch (ResourceNotFoundException e) {
                // Category doesn't exist, create it
                Category newCategory = new Category();
                newCategory.setName(name);
                // Generate slug from name
                newCategory.setSlug(name.toLowerCase()
                        .replaceAll("\\s+", "-")
                        .replaceAll("[^a-z0-9-]", ""));
                result.add(categoryRepository.save(newCategory));
            }
        }

        return result;
    }

    @Cacheable(value = { "categories",
            "novels" }, key = "'category-novels-' + #categoryName + '-' + #pageable.pageNumber")
    @Transactional
    public PageResponse<NovelDTO> getNovelsByCategory(String categoryName, Pageable pageable) {
        Category category = getCategoryByName(categoryName);
        return novelService.getNovelsByCategory(category.getId(), pageable);
    }
}