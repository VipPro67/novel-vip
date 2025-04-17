package com.novel.vippro.services;

import com.novel.vippro.dto.NovelDTO;
import com.novel.vippro.dto.NovelCreateDTO;
import com.novel.vippro.exception.ResourceNotFoundException;
import com.novel.vippro.mapper.NovelMapper;
import com.novel.vippro.models.Category;
import com.novel.vippro.models.Novel;
import com.novel.vippro.repository.CategoryRepository;
import com.novel.vippro.repository.NovelRepository;
import com.novel.vippro.security.jwt.AuthEntryPointJwt;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.Normalizer;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Service
public class NovelService {

    private static final Logger logger = LoggerFactory.getLogger(AuthEntryPointJwt.class);

    @Autowired
    private NovelRepository novelRepository;

    @Autowired
    private NovelMapper novelMapper;

    @Autowired
    private CategoryRepository categoryRepository;

    public Page<NovelDTO> getAllNovels(Pageable pageable) {
        Page<Novel> novels = novelRepository.findAll(pageable);
        return novels.map(novelMapper::toDTO);
    }

    public Page<NovelDTO> getNovelsByCategory(Category category, Pageable pageable) {
        if (category == null) {
            throw new ResourceNotFoundException("Category", "id", null);
        }
        logger.info("Fetching novels for category: {}", category.getName());
        String categoryName = Normalizer.normalize(category.getName(), Normalizer.Form.NFD)
                .replaceAll("[^\\p{ASCII}]", "")
                .toUpperCase();
        Page<Novel> novels = novelRepository.findByCategoriesContaining(categoryName, pageable);
        return novels.map(novelMapper::toDTO);
    }

    public Page<NovelDTO> getNovelsByCategory(String categoryName, Pageable pageable) {
        Page<Novel> novels = novelRepository.findByCategoriesContaining(categoryName, pageable);
        return novels.map(novelMapper::toDTO);
    }

    public Page<NovelDTO> getNovelsByStatus(String status, Pageable pageable) {
        Page<Novel> novels = novelRepository.findByStatus(status, pageable);
        return novels.map(novelMapper::toDTO);
    }

    public Page<NovelDTO> searchNovels(String keyword, Pageable pageable) {
        keyword = Normalizer.normalize(keyword, Normalizer.Form.NFD)
                .replaceAll("[^\\p{ASCII}]", "")
                .toUpperCase();
        logger.info("Searching for novels with keyword: {}", keyword);
        Page<Novel> novels = novelRepository.searchByKeyword(keyword, pageable);
        return novels.map(novelMapper::toDTO);
    }

    public NovelDTO getNovelById(UUID id) {
        Novel novel = novelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Novel", "id", id));
        return novelMapper.toDTO(novel);
    }

    public Page<NovelDTO> getHotNovels(Pageable pageable) {
        Page<Novel> novels = novelRepository.findAllByOrderByViewsDesc(pageable);
        return novels.map(novelMapper::toDTO);
    }

    public Page<NovelDTO> getTopRatedNovels(Pageable pageable) {
        Page<Novel> novels = novelRepository.findAllByOrderByRatingDesc(pageable);
        return novels.map(novelMapper::toDTO);
    }

    @Transactional
    public NovelDTO createNovel(NovelCreateDTO novelDTO) {
        Novel novel = new Novel();
        novel.setTitle(novelDTO.getTitle());
        novel.setSlug(novelDTO.getSlug());
        novel.setDescription(novelDTO.getDescription());
        novel.setAuthor(novelDTO.getAuthor());
        novel.setCoverImage(novelDTO.getCoverImage());
        novel.setStatus(novelDTO.getStatus());

        // Set default values
        novel.setViews(0);
        novel.setRating(0);
        novel.setTotalChapters(0);
        novel.setComments(null);

        // Initialize categories as an empty set
        novel.setCategories(new HashSet<>());

        // Handle categories - only use existing categories
        if (novelDTO.getCategories() != null && !novelDTO.getCategories().isEmpty()) {
            Set<Category> categories = new HashSet<>();

            for (String categoryName : novelDTO.getCategories()) {
                try {
                    Category category = categoryRepository.findByNameIgnoreCase(categoryName)
                            .orElseThrow(() -> new ResourceNotFoundException("Category", "name", categoryName));
                    categories.add(category);
                } catch (ResourceNotFoundException e) {
                    logger.warn("Category not found: {}", categoryName);
                    // Skip this category and continue with others
                }
            }

            // Add found categories to the novel
            novel.getCategories().addAll(categories);
        }

        // Save the novel
        logger.info("Saving novel: {}", novel);
        logger.info("Novel categories: {}", novel.getCategories());
        Novel savedNovel = novelRepository.save(novel);

        return novelMapper.toDTO(savedNovel);
    }

    @Transactional
    public NovelDTO updateNovel(UUID id, NovelCreateDTO novelDTO) {
        Novel novel = novelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Novel", "id", id));

        // Update novel properties
        novel.setTitle(novelDTO.getTitle());
        novel.setSlug(novelDTO.getSlug());
        novel.setDescription(novelDTO.getDescription());
        novel.setAuthor(novelDTO.getAuthor());
        novel.setCoverImage(novelDTO.getCoverImage());
        novel.setStatus(novelDTO.getStatus());

        // Handle categories
        if (novelDTO.getCategories() != null) {
            // Clear existing categories
            novel.getCategories().clear();

            // Add new categories if provided
            if (!novelDTO.getCategories().isEmpty()) {
                Set<String> categoryNames = new HashSet<>(novelDTO.getCategories());
                for (String categoryName : categoryNames) {
                    try {
                        Category category = categoryRepository.findByNameIgnoreCase(categoryName)
                                .orElseThrow(() -> new ResourceNotFoundException("Category", "name", categoryName));
                        novel.getCategories().add(category);
                    } catch (ResourceNotFoundException e) {
                        logger.warn("Category not found: {}", categoryName);
                        // Skip this category and continue with others
                    }
                }
            }
        }

        Novel updatedNovel = novelRepository.save(novel);
        return novelMapper.toDTO(updatedNovel);
    }

    @Transactional
    public void deleteNovel(UUID id) {
        Novel novel = novelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Novel", "id", id));
        novelRepository.delete(novel);
    }

    @Transactional
    public NovelDTO incrementViews(UUID id) {
        Novel novel = novelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Novel", "id", id));

        novel.setViews(novel.getViews() + 1);
        Novel updatedNovel = novelRepository.save(novel);
        return novelMapper.toDTO(updatedNovel);
    }

    @Transactional
    public NovelDTO updateRating(UUID id, int rating) {
        Novel novel = novelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Novel", "id", id));

        novel.setRating(rating);
        Novel updatedNovel = novelRepository.save(novel);
        return novelMapper.toDTO(updatedNovel);
    }
}