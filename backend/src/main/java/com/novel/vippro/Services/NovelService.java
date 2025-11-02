package com.novel.vippro.Services;

import com.novel.vippro.DTO.Chapter.CreateChapterDTO;
import com.novel.vippro.DTO.Novel.NovelCreateDTO;
import com.novel.vippro.DTO.Novel.NovelDTO;
import com.novel.vippro.DTO.Novel.NovelSearchDTO;
import com.novel.vippro.Exception.ResourceNotFoundException;
import com.novel.vippro.Mapper.Mapper;
import com.novel.vippro.Models.Category;
import com.novel.vippro.Models.FileMetadata;
import com.novel.vippro.Utils.EpubParseResult;
import com.novel.vippro.Models.Genre;
import com.novel.vippro.Models.Novel;
import com.novel.vippro.Models.Tag;
import com.novel.vippro.Models.User;
import com.novel.vippro.Payload.Response.PageResponse;
import com.novel.vippro.Repository.CategoryRepository;
import com.novel.vippro.Repository.GenreRepository;
import com.novel.vippro.Repository.NovelRepository;
import com.novel.vippro.Repository.TagRepository;
import com.novel.vippro.Repository.UserRepository;
import com.novel.vippro.Security.UserDetailsImpl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.checkerframework.checker.units.qual.A;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
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

    private static final Logger logger = LoggerFactory.getLogger(NovelService.class);

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

    @Autowired
    private SearchService searchService;

    @Autowired
    private ChapterService chapterService;

    @Autowired
    private UserRepository userRepository;


    @Transactional(readOnly = true)
    public void reindexAllNovels() {
        List<Novel> novels = novelRepository.findAll();
        if (novels.isEmpty()) {
            logger.info("No novels found to reindex.");
            return;
        }
        logger.info("Reindexing {} novels...", novels.size());
            searchService.indexNovels(novels);
        logger.info("Reindexing completed. {} novels reindexed.", novels.size());
    }
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
    public PageResponse<NovelDTO> getNovelsByCategory(UUID category, Pageable pageable) {  
        Page<Novel> novels = novelRepository.findByCategoriesId(category, pageable);
        return new PageResponse<>(novels.map(mapper::NoveltoDTO));
    }
    
    @Cacheable(value = "novels", key = "'genre-' + #genre + '-' + #pageable.pageNumber + '-' + #pageable.pageSize")
    @Transactional(readOnly = true)
    public PageResponse<NovelDTO> getNovelsByGenre(UUID genre, Pageable pageable) {
        Page<Novel> novels = novelRepository.findByGenresId(genre, pageable);
        return new PageResponse<>(novels.map(mapper::NoveltoDTO));
    }

    @Cacheable(value = "novels", key = "'tag-' + #tag + '-' + #pageable.pageNumber + '-' + #pageable.pageSize")
    @Transactional(readOnly = true)
    public PageResponse<NovelDTO> getNovelsByTag(UUID tag, Pageable pageable) {
        Page<Novel> novels = novelRepository.findByTagsId(tag, pageable);
        return new PageResponse<>(novels.map(mapper::NoveltoDTO));
    }

    @Cacheable(value = "novels", key = "'status-' + #status + '-' + #pageable.pageNumber + '-' + #pageable.pageSize")
    @Transactional(readOnly = true)
    public PageResponse<NovelDTO> getNovelsByStatus(String status, Pageable pageable) {
        Page<Novel> novels = novelRepository.findByStatus(status, pageable);
        return new PageResponse<>(novels.map(mapper::NoveltoDTO));
    }

    @Cacheable(value = "novels", key = "'search-' + (#searchDTO != null ? #searchDTO.cacheKey() : '') + '-' + #pageable.pageNumber + '-' + #pageable.pageSize + '-' + #pageable.sort.toString()")
    @Transactional(readOnly = true)
    public PageResponse<NovelDTO> searchNovels(NovelSearchDTO searchDTO, Pageable pageable) {
        NovelSearchDTO filters = searchDTO == null ? new NovelSearchDTO() : searchDTO.cleanedCopy();

        if (!filters.hasFilters()) {
            logger.info("Search requested without filters. Returning empty page.");
            Page<Novel> emptyResult = Page.empty(pageable);
            return new PageResponse<>(emptyResult.map(mapper::NoveltoDTO));
        }

        logger.info("Searching novels with filters: {}", filters);
        Page<Novel> novels = searchService.search(filters, pageable);

        if (novels.isEmpty()) {
            logger.info("Elasticsearch search returned no results. Falling back to database query.");
            novels = novelRepository.searchByCriteria(
                    filters.getKeyword(),
                    filters.getTitle(),
                    filters.getAuthor(),
                    filters.getCategory(),
                    filters.getGenre(),
                    filters.getTag(),
                    pageable);

            if (novels.isEmpty() && filters.getKeyword() != null) {
                logger.info("No novels found using criteria. Falling back to keyword search for: {}", filters.getKeyword());
                novels = novelRepository.searchByKeyword(filters.getKeyword(), pageable);
                searchService.indexNovels(novels.getContent());
                logger.info("Reindexing completed. {} novels reindexed.", novels.getTotalElements());
            }
        }

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

    @Transactional
    @CacheEvict(value = "novels", allEntries = true)
    public NovelDTO createNovel(NovelCreateDTO novelDTO) {
        Novel novel = new Novel();
        novel.setTitle(novelDTO.getTitle());
        novel.setSlug(novelDTO.getSlug());
        novel.setDescription(novelDTO.getDescription());
        novel.setAuthor(novelDTO.getAuthor());
        novel.setTitleNormalized(novelDTO.getTitle().toLowerCase());
        novel.setStatus(novelDTO.getStatus());
        UUID ownerId = UserDetailsImpl.getCurrentUserId();
        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", ownerId));
        novel.setOwner(owner);
        novel.setRating(0);
        novel.setTotalChapters(0);
        novel.setComments(null);

        // Handle categories - only use existing categories
        if (novelDTO.getCategories() != null && !novelDTO.getCategories().isEmpty()) {
            List<Category> categories = new ArrayList<>();

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
        List<Novel> novelsToIndex = List.of(savedNovel);
        searchService.indexNovels(novelsToIndex);

        return mapper.NoveltoDTO(savedNovel);
    }

    public NovelDTO createNovelFromEpub(EpubParseResult epubResult, String slug, String status) {
        Novel saved = saveNovelInitial(epubResult, slug, status);

        if (epubResult.getCoverImage() != null) {
            try {
                FileMetadata cover = fileService.uploadFile(epubResult.getCoverImage(), epubResult.getCoverImageName(), "image/jpeg", "cover");
                saved.setCoverImage(cover);
                novelRepository.save(saved);
                saved = novelRepository.findById(saved.getId()).orElse(saved);
            } catch (Exception e) {
                logger.error("Error uploading cover image from EPUB: {}", e.getMessage());
            }
        }

        if (epubResult.getChapters() != null) {
            int idx = 1;
            for (var c : epubResult.getChapters()) {
                try {
                    CreateChapterDTO dto = new CreateChapterDTO();
                    dto.setChapterNumber(idx);
                    dto.setNovelId(saved.getId());
                    dto.setTitle(c.getTitle() != null && !c.getTitle().isBlank() ? c.getTitle() : "Chapter " + idx);
                    dto.setContentHtml(c.getContentHtml() == null ? "" : c.getContentHtml());
                    dto.setFormat(CreateChapterDTO.ContentFormat.HTML);
                    chapterService.createChapter(dto);
                } catch (Exception e) {
                    logger.error("Failed to create chapter {} for novel {}: {}", idx, saved.getId(), e.getMessage());
                }
                idx++;
            }
        }

        saved = novelRepository.findById(saved.getId()).orElseThrow();
        searchService.indexNovels(List.of(saved));
        return mapper.NoveltoDTO(saved);
    }

    @Transactional
    protected Novel saveNovelInitial(EpubParseResult epubResult, String slug, String status) {
        Novel novel = new Novel();
        novel.setTitle(epubResult.getTitle() == null || epubResult.getTitle().isBlank() ? epubResult.getAuthor() + " - Import" : epubResult.getTitle());
        novel.setSlug(slug);
        novel.setDescription("Imported from EPUB: " + (epubResult.getTitle() != null ? epubResult.getTitle() : ""));
        novel.setAuthor(epubResult.getAuthor() == null || epubResult.getAuthor().isBlank() ? "Unknown" : epubResult.getAuthor());
        novel.setTitleNormalized(novel.getTitle().toLowerCase());
        novel.setStatus(status == null ? "ongoing" : status);
        novel.setRating(0);
        novel.setTotalChapters(0);
        novel.setComments(null);
        return novelRepository.save(novel);
    }

    @Transactional
    @Caching( evict ={
        @CacheEvict(value = "novels", key = "#novelId"),
        @CacheEvict(value = "novels", key = "'slug-' + #novel.slug")
    })
    public NovelDTO updateNovelCover(UUID novelId, MultipartFile coverImage) {
        try {
            Novel novel = novelRepository.findById(novelId)
                    .orElseThrow(() -> new ResourceNotFoundException("Novel", "id", novelId));
            FileMetadata cover = fileService.uploadFile(coverImage, "cover");
            
            novel.setCoverImage(cover);
            Novel updatedNovel = novelRepository.save(novel);
            return mapper.NoveltoDTO(updatedNovel);
        } catch (Exception e) {
            logger.error("Error uploading cover image: {}", e.getMessage());
            throw new RuntimeException("Failed to upload cover image", e);
        }
    }

    @Caching( evict ={
        @CacheEvict(value = "novels", key = "#id"),
        @CacheEvict(value = "novels", key = "'slug-' + #novelDTO.slug")
    })
    @Transactional
    public NovelDTO updateNovel(UUID id, NovelCreateDTO novelDTO) {
        Novel novel = novelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Novel", "id", id));

        // Update novel properties
        novel.setTitle(novelDTO.getTitle());
        //novel.setSlug(novelDTO.getSlug()); dont allow slug change :)
        novel.setDescription(novelDTO.getDescription());
        novel.setAuthor(novelDTO.getAuthor());
        novel.setTitleNormalized(novelDTO.getTitle().toLowerCase());
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
        List<Novel> novelsToIndex = List.of(updatedNovel);
        searchService.indexNovels(novelsToIndex);
        return mapper.NoveltoDTO(updatedNovel);
    }

    @Caching( evict ={
        @CacheEvict(value = "novels", key = "#id"),
        @CacheEvict(value = "novels", key = "'slug-' + #novel.slug")
    })
    @Transactional
    public void deleteNovel(UUID id) {
        Novel novel = novelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Novel", "id", id));
        novelRepository.delete(novel);
        searchService.deleteNovel(id);
    }

    @Transactional
    public NovelDTO updateRating(UUID id, int rating) {
        Novel novel = novelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Novel", "id", id));

        novel.setRating(rating);
        Novel updatedNovel = novelRepository.save(novel);
        return mapper.NoveltoDTO(updatedNovel);
    }

    @Cacheable(value = "novels", key = "'slug-' + #slug")
    @Transactional(readOnly = true)
    public NovelDTO getNovelBySlug(String slug) {
        Novel novel = novelRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Novel", "slug", slug));
        return mapper.NoveltoDTO(novel);
    }

    @Caching( evict ={
        @CacheEvict(value = "novels", key = "#novelId"),
        @CacheEvict(value = "novels", key = "'slug-' + #novel.slug")
    })
    @Transactional
    public NovelDTO addChaptersFromEpub(UUID novelId, EpubParseResult epubResult) {
        Novel novel = novelRepository.findById(novelId)
                .orElseThrow(() -> new ResourceNotFoundException("Novel", "id", novelId));

        // Find the highest chapter number
        int lastChapterNumber = chapterService.getLastChapterNumber(novelId);
        int startingIndex = lastChapterNumber + 1;

        // Add new chapters starting from the next number
        if (epubResult.getChapters() != null) {
            int idx = startingIndex;
            List<CreateChapterDTO> dtos = new java.util.ArrayList<>();
            for (var c : epubResult.getChapters()) {
                CreateChapterDTO dto = new CreateChapterDTO();
                dto.setChapterNumber(idx);
                dto.setNovelId(novelId);
                dto.setTitle(c.getTitle() != null && !c.getTitle().isBlank() ? c.getTitle() : "Chapter " + idx);
                dto.setContentHtml(c.getContentHtml() == null ? "" : c.getContentHtml());
                dto.setFormat(CreateChapterDTO.ContentFormat.HTML);
                dtos.add(dto);
                idx++;
            }

            try {
                chapterService.createChaptersBatch(dtos, 50);
                logger.info("Created {} chapters for novel {} via batch", dtos.size(), novelId);
            } catch (Exception e) {
                logger.error("Failed to create chapters batch for novel {}: {}", novelId, e.getMessage());
                // fallback: try to create individually to salvage successful ones
                int retryIdx = startingIndex;
                for (CreateChapterDTO dto : dtos) {
                    try {
                        chapterService.createChapter(dto);
                        logger.info("Created chapter {} for novel {} (fallback)", retryIdx, novelId);
                    } catch (Exception ex) {
                        logger.error("Failed to create chapter {} for novel {} in fallback: {}", retryIdx, novelId, ex.getMessage());
                    }
                    retryIdx++;
                }
            }
        }

        // Reload and index the updated novel
        novel = novelRepository.findById(novelId).orElseThrow();
        try{
            searchService.indexNovels(List.of(novel));
        }
        catch (Exception e) {
            logger.error("Error indexing novel: {}", e.getMessage());
        }

        // Notification for novel owner 
        return mapper.NoveltoDTO(novel);
    }
    
}
