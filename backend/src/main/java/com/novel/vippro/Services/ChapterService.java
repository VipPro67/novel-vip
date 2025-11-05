package com.novel.vippro.Services;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.novel.vippro.DTO.Chapter.ChapterDTO;
import com.novel.vippro.DTO.Chapter.ChapterDetailDTO;
import com.novel.vippro.DTO.Chapter.CreateChapterDTO;
import com.novel.vippro.Exception.ResourceNotFoundException;
import com.novel.vippro.Mapper.Mapper;
import com.novel.vippro.Messaging.AsyncTaskPublisher;
import com.novel.vippro.Messaging.payload.ChapterAudioMessage;
import com.novel.vippro.Models.Chapter;
import com.novel.vippro.Models.FileMetadata;
import com.novel.vippro.Models.Novel;
import com.novel.vippro.Payload.Response.PageResponse;
import com.novel.vippro.Repository.ChapterRepository;
import com.novel.vippro.Repository.FileMetadataRepository;
import com.novel.vippro.Repository.NovelRepository;

@Service
public class ChapterService {

    @Autowired
    private ChapterRepository chapterRepository;

    @Autowired
    private NovelRepository novelRepository;

    @Autowired
    @Qualifier("s3FileStorageService")
    private FileStorageService fileStorageService;

    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private Mapper mapper;

    @Autowired
    @Qualifier("openAiEdgeTTS")
    private TextToSpeechService textToSpeechService;

    @Autowired
    private FavoriteService favoriteService;

    @Autowired
    private FileMetadataRepository fileMetadataRepository;

    @Autowired
    private AsyncTaskPublisher asyncTaskPublisher;

    private static final Logger logger = LogManager.getLogger(ChapterService.class);

    @Cacheable(value = "chapters", key = "#id")
    @Transactional(readOnly = true)
    public ChapterDetailDTO getChapterDetailDTO(UUID id) {
        Chapter chapter = chapterRepository.getChapterDetailById(id);
        ChapterDetailDTO dto = mapper.ChaptertoChapterDetailDTO(chapter);
        return dto;
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "chapters", key = "'novel-' + #novelId + '-chapter-' + #chapterNumber")
    public ChapterDetailDTO getChapterByNumberDTO(UUID novelId, Integer chapterNumber) {
        if (!novelRepository.existsById(novelId)) {
            throw new ResourceNotFoundException("Novel", "id", novelId);
        }
        Chapter chapter = chapterRepository.findByNovelIdAndChapterNumber(novelId, chapterNumber);
        if (chapter == null) {
            throw new ResourceNotFoundException("Chapter", "novelId and chapterNumber",
                    novelId + " and " + chapterNumber);
        }
        return mapper.ChaptertoChapterDetailDTO(chapter);
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "chapters", key = "'novel-slug-' + #slug + '-chapter-' + #chapterNumber")
    public ChapterDetailDTO getChapterByNumber2DTO(String slug, Integer chapterNumber) {
        Novel novel = novelRepository.findBySlugWithGraph(slug);
        if (novel == null) {
            throw new ResourceNotFoundException("Novel", "slug", slug);
        }
        Chapter chapter = chapterRepository.findByNovelIdAndChapterNumber(novel.getId(), chapterNumber);
        if (chapter == null) {
            throw new ResourceNotFoundException("Chapter", "novelId and chapterNumber",
                    novel.getId() + " and " + chapterNumber);
        }

        return mapper.ChaptertoChapterDetailDTO(chapter);
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "chapters", key = "'novel-' + #novelId + '-page-' + #pageable.pageNumber")
    public PageResponse<ChapterDTO> getChaptersByNovelDTO(UUID novelId, Pageable pageable) {
        Page<Chapter> chapters = chapterRepository.findByNovelIdOrderByChapterNumberAsc(novelId, pageable);
        return new PageResponse<>(chapters.map(mapper::ChaptertoChapterDTO));
    }

    @CacheEvict(value = { "chapters", "novels" }, allEntries = true)
    @Transactional
    public ChapterDTO createChapterDTO(CreateChapterDTO chapterDTO) {
        Chapter chapter = createChapter(chapterDTO);
        return mapper.ChaptertoChapterDTO(chapter);
    }

    @CacheEvict(value = { "chapters", "novels" }, key = "#id")
    @Transactional
    public ChapterDTO updateChapterDTO(UUID id, CreateChapterDTO chapterDTO) {
        Chapter chapter = updateChapter(id, chapterDTO);
        if (chapter == null) {
            throw new ResourceNotFoundException("Chapter", "id", id);
        }
        return mapper.ChaptertoChapterDTO(chapter);
    }

    @CacheEvict(value = { "chapters", "novels" }, allEntries = true)
    @Transactional
    public void deleteChapter(UUID id) {
        Chapter chapter = getChapterById(id);
        chapterRepository.delete(chapter);
        // Update total chapters and updated at
        novelRepository.findById(chapter.getNovel().getId()).ifPresent(novel -> {
            novel.setTotalChapters(novel.getTotalChapters() - 1);
            novel.setUpdatedAt(Instant.now());
            novelRepository.save(novel);
        });
    }

    @Transactional(readOnly = true)
    public PageResponse<Chapter> getChaptersByNovel(UUID novelId, Pageable pageable) {
        if (!novelRepository.existsById(novelId)) {
            throw new ResourceNotFoundException("Novel", "id", novelId);
        }
        return new PageResponse<>(chapterRepository.findByNovelIdOrderByChapterNumberAsc(novelId, pageable));
    }

    @Transactional(readOnly = true)
    public Chapter getChapterById(UUID id) {
        return chapterRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Chapter", "id", id));
    }

    @Transactional(readOnly = true)
    public ChapterDetailDTO getChapterByNovelIdAndNumber(UUID novelId, Integer chapterNumber) {
        if (!novelRepository.existsById(novelId)) {
            throw new ResourceNotFoundException("Novel", "id", novelId);
        }
        Chapter chapter = chapterRepository.findByNovelIdAndChapterNumber(novelId, chapterNumber);
        if (chapter == null) {
            throw new ResourceNotFoundException("Chapter", "novelId and chapterNumber",
                    novelId + " and " + chapterNumber);
        }
        logger.info("chapter audio file: {}", chapter.getAudioFile().getId());
        logger.info("chapter json file: {}", chapter.getJsonFile().getId());

        return mapper.ChaptertoChapterDetailDTO(chapter);
    }

    public Map<String, Object> getChapterContent(Chapter chapter) {
        FileMetadata jsonFile = chapter.getJsonFile();

        if (jsonFile == null || jsonFile.getPublicId() == null) {
            throw new ResourceNotFoundException("JSON file not found for chapter " + chapter.getId());
        }

        String presignedUrl = fileStorageService.generateFileUrl(jsonFile.getPublicId(), 3600);
        logger.info("Fetching chapter content from presigned URL: {}", presignedUrl);

        try {
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(presignedUrl))
                .GET()
                .build();

            HttpResponse<String> response = HttpClient.newHttpClient()
                .send(request, HttpResponse.BodyHandlers.ofString());

            String json = response.body();
            return new ObjectMapper().readValue(json, Map.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch chapter content: " + e.getMessage(), e);
        }
    }


    @Transactional
    public Chapter createChapter(CreateChapterDTO chapterDTO) {

        if (!novelRepository.existsById(chapterDTO.getNovelId())) {
            throw new ResourceNotFoundException("Novel", "id", chapterDTO.getNovelId());
        }
        if (chapterRepository.findByNovelIdAndChapterNumber(chapterDTO.getNovelId(),
                chapterDTO.getChapterNumber()) != null) {
            throw new RuntimeException(
                    "Chapter number already exists for this novel. Please choose a different number or update the existing chapter.");
        }

        Novel chapterNovel = novelRepository.findById(chapterDTO.getNovelId())
                .orElseThrow(() -> new ResourceNotFoundException("Novel", "id", chapterDTO.getNovelId()));

        Chapter chapter = new Chapter();
        chapter.setChapterNumber(chapterDTO.getChapterNumber());
        chapter.setTitle(chapterDTO.getTitle());
        chapter.setNovel(chapterNovel);

        Map<String, Object> contentMap = Map.of(
                "novelSlug", chapter.getNovel().getSlug(),
                "novelTitle", chapter.getNovel().getTitle(),
                "chapterNumber", chapter.getChapterNumber(),
                "chapterTitle", chapter.getTitle(),
                "content", chapterDTO.getContentHtml());

        String jsonContent;
        try {
            jsonContent = objectMapper.writeValueAsString(contentMap);
        } catch (IOException e) {
            throw new RuntimeException("Error converting chapter content to JSON: " + e.getMessage(), e);
        }

        String publicId = String.format("novels/%s/chapters/chap-%d.json",
                chapter.getNovel().getSlug(),
                chapter.getChapterNumber());

        String jsonUrl;
        try {
            jsonUrl = fileStorageService.uploadFile(jsonContent.getBytes(), publicId, "application/json");
        } catch (IOException e) {
            throw new RuntimeException("Error uploading chapter content to Cloudinary: " + e.getMessage(), e);
        }
        if (jsonUrl == null || jsonUrl.isEmpty()) {
            throw new RuntimeException("Error uploading chapter content to Cloudinary: URL is empty");
        }

        FileMetadata jsonFile = new FileMetadata();
        jsonFile.setPublicId(publicId);
        jsonFile.setContentType("application/json");
        jsonFile.setFileUrl(jsonUrl);
        jsonFile.setCreatedAt(Instant.now());
        chapter.setJsonFile(jsonFile);

        novelRepository.findById(chapter.getNovel().getId()).ifPresent(novel -> {
            novel.setTotalChapters(novel.getTotalChapters() + 1);
            novel.setUpdatedAt(Instant.now());
            novelRepository.save(novel);
        });

        chapterRepository.save(chapter);
        favoriteService.notifyFavorites(chapter.getNovel().getId());
        logger.info("Created chapter: {}", chapterNovel.getSlug() + " " + chapter.getChapterNumber());
        return chapter;
    }

    @CacheEvict(value = { "chapters", "novels" }, allEntries = true)
    @Transactional
    public List<Chapter> createChaptersBatch(List<CreateChapterDTO> chapterDTOs, int batchSize) {
        if (chapterDTOs == null || chapterDTOs.isEmpty()) {
            return Collections.emptyList();
        }

        List<Chapter> toSave = new ArrayList<>(chapterDTOs.size());

        for (CreateChapterDTO chapterDTO : chapterDTOs) {
            if (!novelRepository.existsById(chapterDTO.getNovelId())) {
                throw new ResourceNotFoundException("Novel", "id", chapterDTO.getNovelId());
            }
            if (chapterRepository.findByNovelIdAndChapterNumber(chapterDTO.getNovelId(),
                    chapterDTO.getChapterNumber()) != null) {
                throw new RuntimeException(
                        "Chapter number already exists for this novel. Please choose a different number or update the existing chapter.");
            }

            Novel chapterNovel = novelRepository.findById(chapterDTO.getNovelId())
                    .orElseThrow(() -> new ResourceNotFoundException("Novel", "id", chapterDTO.getNovelId()));

            Chapter chapter = new Chapter();
            chapter.setChapterNumber(chapterDTO.getChapterNumber());
            chapter.setTitle(chapterDTO.getTitle());
            chapter.setNovel(chapterNovel);

            Map<String, Object> contentMap = Map.of(
                    "novelSlug", chapter.getNovel().getSlug(),
                    "novelTitle", chapter.getNovel().getTitle(),
                    "chapterNumber", chapter.getChapterNumber(),
                    "chapterTitle", chapter.getTitle(),
                    "content", chapterDTO.getContentHtml());

            String jsonContent;
            try {
                jsonContent = objectMapper.writeValueAsString(contentMap);
            } catch (IOException e) {
                throw new RuntimeException("Error converting chapter content to JSON: " + e.getMessage(), e);
            }

            String publicId = String.format("novels/%s/chapters/chap-%d.json",
                    chapter.getNovel().getSlug(),
                    chapter.getChapterNumber());

            String jsonUrl;
            try {
                jsonUrl = fileStorageService.uploadFile(jsonContent.getBytes(), publicId, "application/json");
            } catch (IOException e) {
                throw new RuntimeException("Error uploading chapter content to storage: " + e.getMessage(), e);
            }
            if (jsonUrl == null || jsonUrl.isEmpty()) {
                throw new RuntimeException("Error uploading chapter content to storage: URL is empty");
            }

            FileMetadata jsonFile = new FileMetadata();
            jsonFile.setPublicId(publicId);
            jsonFile.setContentType("application/json");
            jsonFile.setFileUrl(jsonUrl);
            jsonFile.setCreatedAt(Instant.now());
            chapter.setJsonFile(jsonFile);

            toSave.add(chapter);
        }

        List<Chapter> saved = new ArrayList<>(toSave.size());

        for (int i = 0; i < toSave.size(); i += batchSize) {
            int end = Math.min(i + batchSize, toSave.size());
            List<Chapter> chunk = toSave.subList(i, end);
            List<Chapter> chunkSaved = chapterRepository.saveAll(chunk);
            chapterRepository.flush();
            saved.addAll(chunkSaved);
            Set<UUID> novelIds = chunkSaved.stream().map(c -> c.getNovel().getId()).collect(Collectors.toSet());
            for (UUID nid : novelIds) {
                favoriteService.notifyFavorites(nid);
                novelRepository.findById(nid).ifPresent(novel -> {
                    long countForNovel = chunkSaved.stream().filter(c -> c.getNovel().getId().equals(nid)).count();
                    novel.setTotalChapters(novel.getTotalChapters() + (int) countForNovel);
                    novel.setUpdatedAt(Instant.now());
                    novelRepository.save(novel);
                });
            }
        }
        return saved;
    }
        
    @Transactional
    public Chapter updateChapter(UUID id, CreateChapterDTO chapterDTO) {
        Chapter chapter = getChapterById(id);
        chapter.setChapterNumber(chapterDTO.getChapterNumber());
        chapter.setTitle(chapterDTO.getTitle());

        if (chapterDTO.getNovelId() != null) {
            chapter.setNovel(novelRepository.findById(chapterDTO.getNovelId())
                    .orElseThrow(() -> new RuntimeException("Novel not found with id: " + chapterDTO.getNovelId())));
        }

        Map<String, Object> contentMap = Map.of(
                "novelSlug", chapter.getNovel().getSlug(),
                "novelTitle", chapter.getNovel().getTitle(),
                "chapterNumber", chapter.getChapterNumber(),
                "chapterTitle", chapter.getTitle(),
                "content", chapterDTO.getContent());

        String jsonContent;
        try {
            jsonContent = objectMapper.writeValueAsString(contentMap);
        } catch (IOException e) {
            throw new RuntimeException("Error converting chapter content to JSON: " + e.getMessage(), e);
        }

        String publicId = String.format("novels/%s/chapters/chap-%d" + ".json",
                chapter.getNovel().getSlug(),
                chapter.getChapterNumber());

        String jsonUrl;
        try {
            jsonUrl = fileStorageService.uploadFile(jsonContent.getBytes(), publicId, "application/json");
        } catch (IOException e) {
            throw new RuntimeException("Error uploading chapter content to Cloudinary: " + e.getMessage(), e);
        }
        if (jsonUrl == null || jsonUrl.isEmpty()) {
            throw new RuntimeException("Error uploading chapter content to Cloudinary: URL is empty");
        }

        FileMetadata jsonFile = chapter.getJsonFile();
        if (jsonFile == null) {
            jsonFile = new FileMetadata();
        }
        jsonFile.setPublicId(publicId);
        jsonFile.setFileName(chapter.getTitle());
        jsonFile.setType("json");
        jsonFile.setSize(jsonContent.length());
        jsonFile.setContentType("application/json");
        jsonFile.setFileUrl(jsonUrl);
        jsonFile.setUpdatedAt(Instant.now());
        chapter.setJsonFile(jsonFile);

        novelRepository.findById(chapter.getNovel().getId()).ifPresent(novel -> {
            novel.setTotalChapters(novel.getTotalChapters() + 1);
            novel.setUpdatedAt(Instant.now());
            novelRepository.save(novel);
        });
        return chapterRepository.save(chapter);
    }

    @Transactional
    public Chapter ensureChapterAudioGenerated(UUID id) {
        logger.info("Ensuring audio for chapter id: {}", id);
        Chapter chapter = getChapterById(id);
        if (chapter == null) {
            throw new ResourceNotFoundException("Chapter", "id", id);
        }

        if (chapter.getAudioFile() != null) {
            return chapter;
        }

        Map<String, Object> content = getChapterContent(chapter);
        String textToConvert = chapter.getTitle() + "\n" + content.get("content");
        textToConvert = textToConvert.replaceAll("</?(?!h2\\b)[^>]*>", "");

        FileMetadata audioFile;
        try {
            logger.info("Starting speech synthesis for chapter id: {}", chapter.getId());
            audioFile = textToSpeechService.synthesizeSpeech(
                    textToConvert,
                    chapter.getNovel().getSlug(),
                    chapter.getChapterNumber());
        } catch (IOException | IllegalStateException e) {
            throw new RuntimeException("Error generating audio for chapter: " + e.getMessage(), e);
        }
        fileMetadataRepository.save(audioFile);
        chapter.setAudioFile(audioFile);
        chapterRepository.save(chapter);
        return chapter;
    }

    @Transactional
    public ChapterDetailDTO createChapterAudio(UUID id) {
        Chapter chapter = ensureChapterAudioGenerated(id);
        return mapper.ChaptertoChapterDetailDTO(chapter);
    }

    @Transactional(readOnly = true)
    public boolean enqueueChapterAudio(UUID chapterId, UUID userId) {
        Chapter chapter = getChapterById(chapterId);
        if (chapter.getAudioFile() != null) {
            return false;
        }
        ChapterAudioMessage message = ChapterAudioMessage.builder()
                .chapterId(chapter.getId())
                .chapterNumber(chapter.getChapterNumber())
                .novelId(chapter.getNovel().getId())
                .novelSlug(chapter.getNovel().getSlug())
                .jobId(null)
                .userId(userId)
                .build();
        asyncTaskPublisher.publishChapterAudio(message);
        return true;
    }

    public FileMetadata getChapterAudioMetadata(UUID id) {
        Chapter chapter = getChapterById(id);
        if (chapter == null) {
            throw new ResourceNotFoundException("Chapter", "id", id);
        }
        FileMetadata audioFile = chapter.getAudioFile();
        if (audioFile == null) {
            throw new ResourceNotFoundException("Chapter audio file", "chapterId", id);
        }
        return audioFile;
    }

    public FileMetadata getChapterJsonMetadata(UUID id) {
        Chapter chapter = getChapterById(id);
        if (chapter == null) {
            throw new ResourceNotFoundException("Chapter", "id", id);
        }
        FileMetadata jsonFile = chapter.getJsonFile();
        if (jsonFile == null) {
            throw new ResourceNotFoundException("Chapter json file", "chapterId", id);
        }
        return jsonFile;
    }

    @Transactional
    public Chapter saveChapterEntity(Chapter chapter) {
        return chapterRepository.save(chapter);
    }

    public int getLastChapterNumber(UUID novelId) {
        Chapter last = chapterRepository.findTopByNovelIdOrderByChapterNumberDesc(novelId);
        return last == null ? 0 : last.getChapterNumber();
    }
}
