package com.novel.vippro.Services;

import com.novel.vippro.DTO.Category.CategoryDTO;
import com.novel.vippro.DTO.Novel.NovelDTO;
import com.novel.vippro.Exception.ResourceNotFoundException;
import com.novel.vippro.Mapper.Mapper;
import com.novel.vippro.Models.Category;
import com.novel.vippro.Payload.Response.PageResponse;
import com.novel.vippro.Repository.CategoryRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
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

    @CacheEvict(value = "categories", allEntries = true)
    @Transactional
    public CategoryDTO createCategory(CategoryDTO categoryDTO) {
        Category category = mapper.DTOtoCategory(categoryDTO);
        // Ensure the category name is unique
        if (categoryRepository.findByNameIgnoreCase(category.getName()).isPresent()) {
            throw new ResourceNotFoundException("Category", "name", category.getName());
        }
        categoryRepository.save(category);
        return mapper.CategorytoDTO(category);
    }

    @CacheEvict(value = "categories", key = "#id")
    @Transactional
    public CategoryDTO updateCategory(UUID id, CategoryDTO categoryDetails) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", id));
        category.setName(categoryDetails.getName());
        category.setDescription(categoryDetails.getDescription());
        categoryRepository.save(category);
        return mapper.CategorytoDTO(category);
    }

    @CacheEvict(value = "categories", allEntries = true)
    @Transactional
    public void deleteCategory(UUID id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", id));
        categoryRepository.delete(category);
    }

    @Cacheable(value = { "categories",
            "novels" }, key = "'category-novels-' + #categoryName + '-' + #pageable.pageNumber")
    @Transactional
    public PageResponse<NovelDTO> getNovelsByCategory(String categoryName, Pageable pageable) {
        Category category = categoryRepository.findByNameIgnoreCase(categoryName)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "name", categoryName));
        return novelService.getNovelsByCategory(category.getId(), pageable);
    }

    @Cacheable(value = "categories", key = "#id")
    @Transactional(readOnly = true)
    public CategoryDTO getCategoryById(UUID id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", id));
        return mapper.CategorytoDTO(category);
    }

    @Cacheable(value = "categories", key = "#name")
    @Transactional(readOnly = true)
    public CategoryDTO getCategoryByName(String name) {
        Category category = categoryRepository.findByNameIgnoreCase(name)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "name", name));
        return mapper.CategorytoDTO(category);
    }

    @Cacheable(value = "categories")
    @Transactional(readOnly = true)
    public Set<CategoryDTO> getAllCategories() {
        Set<CategoryDTO> categoryDTOs = new HashSet<>();
        for (Category category : categoryRepository.findAll()) {
            categoryDTOs.add(mapper.CategorytoDTO(category));
        }
        return categoryDTOs;
    }
}