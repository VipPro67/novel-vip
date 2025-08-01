package com.novel.vippro.Services;

import com.novel.vippro.DTO.Novel.NovelCreateDTO;
import com.novel.vippro.DTO.Novel.NovelDTO;
import com.novel.vippro.Exception.ResourceNotFoundException;
import com.novel.vippro.Mapper.Mapper;
import com.novel.vippro.Models.Category;
import com.novel.vippro.Models.FileMetadata;
import com.novel.vippro.Models.Genre;
import com.novel.vippro.Models.Novel;
import com.novel.vippro.Models.Tag;
import com.novel.vippro.Payload.Response.PageResponse;
import com.novel.vippro.Repository.CategoryRepository;
import com.novel.vippro.Repository.GenreRepository;
import com.novel.vippro.Repository.NovelRepository;
import com.novel.vippro.Repository.TagRepository;
import com.novel.vippro.Security.JWT.AuthEntryPointJwt;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class NovelService {

    private static final Logger logger = LoggerFactory.getLogger(AuthEntryPointJwt.class);

    @Autowired
    private NovelRepository novelRepository;

    @Autowired
    private TagRepository tagRepository;

    @Autowired
    private GenreRepository genreRepository;

    @Autowired
    private FileService fileService;

    @Autowired
    private Mapper mapper;

    @Autowired
    private CategoryRepository categoryRepository;

    @Cacheable(value = "novels", key = "#id")
    @Transactional(readOnly = true)
    public NovelDTO getNovelById(UUID id) {
        Novel novel = novelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Novel", "id", id));
        return mapper.NoveltoDTO(novel);
    }

    @Cacheable(value = "novels", key = "'all-' + #pageable.pageNumber + '-' + #pageable.pageSize + '-' + #pageable.sort.toString()")
    @Transactional(readOnly = true)
    public PageResponse<NovelDTO> getAllNovels(Pageable pageable) {
        Page<UUID> idPage = novelRepository.findAllIds(pageable);
        if (idPage.isEmpty()) {
            return new PageResponse<>(Page.empty());
        }

        List<UUID> orderedIds = idPage.getContent();
        List<Novel> novels = novelRepository.findAllByIdInWithGraph(orderedIds);

        // preserve original order
        Map<UUID, Novel> byId = novels.stream()
                .collect(Collectors.toMap(Novel::getId, Function.identity(), (a, b) -> a, LinkedHashMap::new));
        List<Novel> orderedNovels = orderedIds.stream()
                .map(byId::get)
                .filter(Objects::nonNull)
                .toList();

        Page<Novel> pageWithEntities = new PageImpl<>(orderedNovels, pageable, idPage.getTotalElements());
        return new PageResponse<>(pageWithEntities.map(mapper::NoveltoDTO));
    }

    @Cacheable(value = "novels", key = "'category-' + #categoryId + '-' + #pageable.pageNumber + '-' + #pageable.pageSize")
    @Transactional(readOnly = true)
    public PageResponse<NovelDTO> getNovelsByCategory(UUID categoryId, Pageable pageable) {
        Page<Novel> novels = novelRepository.findByCategoriesId(categoryId, pageable);
        return new PageResponse<>(novels.map(mapper::NoveltoDTO));
    }

    @Cacheable(value = "novels", key = "'status-' + #status + '-' + #pageable.pageNumber + '-' + #pageable.pageSize")
    @Transactional(readOnly = true)
    public PageResponse<NovelDTO> getNovelsByStatus(String status, Pageable pageable) {
        Page<Novel> novels = novelRepository.findByStatus(status, pageable);
        return new PageResponse<>(novels.map(mapper::NoveltoDTO));
    }

    @Cacheable(value = "novels", key = "'search-' + #keyword + '-' + #pageable.pageNumber + '-' + #pageable.pageSize + '-' + #pageable.sort.toString()")
    @Transactional(readOnly = true)
    public PageResponse<NovelDTO> searchNovels(String keyword, Pageable pageable) {
        Page<Novel> novels = novelRepository.searchByKeyword(keyword, pageable);
        return new PageResponse<>(novels.map(mapper::NoveltoDTO));
    }

    @Cacheable(value = "novels", key = "'top-rated-' + #pageable.pageNumber + '-' + #pageable.pageSize")
    @Transactional(readOnly = true)
    public PageResponse<NovelDTO> getTopRatedNovels(Pageable pageable) {
        Page<Novel> novels = novelRepository.findAllByOrderByRatingDesc(pageable);
        return new PageResponse<>(novels.map(mapper::NoveltoDTO));
    }

    @Cacheable(value = "novels", key = "'latest-updates-' + #pageable.pageNumber + '-' + #pageable.pageSize")
    @Transactional(readOnly = true)
    public PageResponse<NovelDTO> getLatestUpdatedNovels(Pageable pageable) {
        Page<Novel> novels = novelRepository.findAllByOrderByUpdatedAtDesc(pageable);
        return new PageResponse<>(novels.map(mapper::NoveltoDTO));
    }

    @Cacheable(value = "novels", key = "'most-viewed-' + #pageable.pageNumber + '-' + #pageable.pageSize")
    @Transactional(readOnly = true)
    public PageResponse<NovelDTO> getMostViewedNovels(Pageable pageable) {
        Page<Novel> novels = novelRepository.findAllByOrderByViewsDesc(pageable);
        return new PageResponse<>(novels.map(mapper::NoveltoDTO));
    }

    @Cacheable(value = "novels", key = "'hot-' + #pageable.pageNumber + '-' + #pageable.pageSize")
    @Transactional(readOnly = true)
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

        // Set cover image if provided
        if (novelDTO.getCoverImage() != null) {
            try {
                FileMetadata coverImage = fileService.uploadFile(novelDTO.getCoverImage(), "cover");
                novel.setCoverImage(coverImage);
            } catch (Exception e) {
                logger.error("Error uploading cover image: {}", e.getMessage());
                throw new RuntimeException("Failed to upload cover image", e);
            }
        }
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

        // Handle tags - only use existing tags
        if (novelDTO.getTags() != null && !novelDTO.getTags().isEmpty()) {
            Set<Tag> tags = new HashSet<>();

            for (String tagName : novelDTO.getTags()) {
                try {
                    Tag tag = tagRepository.findByNameIgnoreCase(tagName)
                            .orElseThrow(() -> new ResourceNotFoundException("Tag", "name", tagName));
                    tags.add(tag);
                } catch (ResourceNotFoundException e) {
                    logger.warn("Tag not found: {}", tagName);
                    // Skip this tag and continue with others
                }
            }

            // Add found tags to the novel
            novel.getTags().addAll(tags);
        }

        // Handle genres - only use existing genres
        if (novelDTO.getGenres() != null && !novelDTO.getGenres().isEmpty()) {
            Set<Genre> genres = new HashSet<>();

            for (String genreName : novelDTO.getGenres()) {
                try {
                    Genre genre = genreRepository.findByNameIgnoreCase(genreName)
                            .orElseThrow(() -> new ResourceNotFoundException("Genre", "name", genreName));
                    genres.add(genre);
                } catch (ResourceNotFoundException e) {
                    logger.warn("Genre not found: {}", genreName);
                    // Skip this genre and continue with others
                }
            }

            // Add found genres to the novel
            novel.getGenres().addAll(genres);
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
        novel.setTitleNormalized(novelDTO.getTitle().toLowerCase());

        // Update cover image if provided
        if (novelDTO.getCoverImage() != null) {
            try {
                FileMetadata coverImage = fileService.uploadFile(novelDTO.getCoverImage(), "cover");
                novel.setCoverImage(coverImage);
            } catch (Exception e) {
                logger.error("Error uploading cover image: {}", e.getMessage());
                throw new RuntimeException("Failed to upload cover image", e);
            }
        }
        // Update status
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

        // Handle tags - only use existing tags
        if (novelDTO.getTags() != null && !novelDTO.getTags().isEmpty()) {
            Set<Tag> tags = new HashSet<>();

            for (String tagName : novelDTO.getTags()) {
                try {
                    Tag tag = tagRepository.findByNameIgnoreCase(tagName)
                            .orElseThrow(() -> new ResourceNotFoundException("Tag", "name", tagName));
                    tags.add(tag);
                } catch (ResourceNotFoundException e) {
                    logger.warn("Tag not found: {}", tagName);
                    // Skip this tag and continue with others
                }
            }

            // Add found tags to the novel
            novel.getTags().addAll(tags);
        }

        // Handle genres - only use existing genres
        if (novelDTO.getGenres() != null && !novelDTO.getGenres().isEmpty()) {
            Set<Genre> genres = new HashSet<>();

            for (String genreName : novelDTO.getGenres()) {
                try {
                    Genre genre = genreRepository.findByNameIgnoreCase(genreName)
                            .orElseThrow(() -> new ResourceNotFoundException("Genre", "name", genreName));
                    genres.add(genre);
                } catch (ResourceNotFoundException e) {
                    logger.warn("Genre not found: {}", genreName);
                    // Skip this genre and continue with others
                }
            }

            // Add found genres to the novel
            novel.getGenres().addAll(genres);
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