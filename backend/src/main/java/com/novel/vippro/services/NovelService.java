package com.novel.vippro.services;

import com.novel.vippro.dto.NovelDTO;
import com.novel.vippro.dto.NovelCreateDTO;
import com.novel.vippro.exception.ResourceNotFoundException;
import com.novel.vippro.mapper.Mapper;
import com.novel.vippro.models.Category;
import com.novel.vippro.models.Novel;
import com.novel.vippro.payload.response.PageResponse;
import com.novel.vippro.repository.CategoryRepository;
import com.novel.vippro.repository.NovelRepository;
import com.novel.vippro.security.jwt.AuthEntryPointJwt;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Service
public class NovelService {

    private static final Logger logger = LoggerFactory.getLogger(AuthEntryPointJwt.class);

    @Autowired
    private NovelRepository novelRepository;

    @Autowired
    private Mapper mapper;

    @Autowired
    private CategoryRepository categoryRepository;

    @Cacheable(value = "novels", key = "#id")
    public NovelDTO getNovelById(UUID id) {
        Novel novel = novelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Novel", "id", id));
        return mapper.NoveltoDTO(novel);
    }

    @Cacheable(value = "novels", key = "'all-' + #pageable.pageNumber + '-' + #pageable.pageSize + '-' + #sortBy")
    public PageResponse<NovelDTO> getAllNovels(Pageable pageable, String sortBy) {
        Page<Novel> novels = novelRepository.findAll(pageable);
        return new PageResponse<>(novels.map(mapper::NoveltoDTO));
    }

    @Cacheable(value = "novels", key = "'category-' + #categoryId + '-' + #pageable.pageNumber + '-' + #pageable.pageSize")
    public PageResponse<NovelDTO> getNovelsByCategory(UUID categoryId, Pageable pageable) {
        Page<Novel> novels = novelRepository.findByCategoriesId(categoryId, pageable);
        return new PageResponse<>(novels.map(mapper::NoveltoDTO));
    }

    @Cacheable(value = "novels", key = "'status-' + #status + '-' + #pageable.pageNumber + '-' + #pageable.pageSize")
    public PageResponse<NovelDTO> getNovelsByStatus(String status, Pageable pageable) {
        Page<Novel> novels = novelRepository.findByStatus(status, pageable);
        return new PageResponse<>(novels.map(mapper::NoveltoDTO));
    }

    @Cacheable(value = "novels", key = "'search-' + #keyword + '-' + #pageable.pageNumber + '-' + #pageable.pageSize")
    public PageResponse<NovelDTO> searchNovels(String keyword, Pageable pageable) {
        Page<Novel> novels = novelRepository.searchByKeyword(keyword, pageable);
        return new PageResponse<>(novels.map(mapper::NoveltoDTO));
    }

    @Cacheable(value = "novels", key = "'top-rated-' + #pageable.pageNumber + '-' + #pageable.pageSize")
    public PageResponse<NovelDTO> getTopRatedNovels(Pageable pageable) {
        Page<Novel> novels = novelRepository.findAllByOrderByRatingDesc(pageable);
        return new PageResponse<>(novels.map(mapper::NoveltoDTO));
    }

    @Cacheable(value = "novels", key = "'most-viewed-' + #pageable.pageNumber + '-' + #pageable.pageSize")
    public PageResponse<NovelDTO> getMostViewedNovels(Pageable pageable) {
        Page<Novel> novels = novelRepository.findAllByOrderByViewsDesc(pageable);
        return new PageResponse<>(novels.map(mapper::NoveltoDTO));
    }

    @Cacheable(value = "novels", key = "'hot-' + #pageable.pageNumber + '-' + #pageable.pageSize")
    public PageResponse<NovelDTO> getHotNovels(Pageable pageable) {
        Page<Novel> novels = novelRepository.findByMinimumRating(4, pageable);
        return new PageResponse<>(novels.map(mapper::NoveltoDTO));
    }

    @CacheEvict(value = "novels", allEntries = true)
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

        return mapper.NoveltoDTO(savedNovel);
    }

    @CacheEvict(value = "novels", key = "#id")
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
        return mapper.NoveltoDTO(updatedNovel);
    }

    @CacheEvict(value = "novels", allEntries = true)
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
        return mapper.NoveltoDTO(updatedNovel);
    }

    @Transactional
    public NovelDTO updateRating(UUID id, int rating) {
        Novel novel = novelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Novel", "id", id));

        novel.setRating(rating);
        Novel updatedNovel = novelRepository.save(novel);
        return mapper.NoveltoDTO(updatedNovel);
    }
}