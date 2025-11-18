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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.CacheManager;
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
import com.novel.vippro.DTO.Notification.CreateNotificationDTO;
import com.novel.vippro.DTO.Notification.NotificationDTO;
import com.novel.vippro.Exception.ResourceNotFoundException;
import com.novel.vippro.Mapper.Mapper;
import com.novel.vippro.Messaging.MessagePublisher;
import com.novel.vippro.Messaging.payload.ChapterAudioMessage;
import com.novel.vippro.Models.Chapter;
import com.novel.vippro.Models.FileMetadata;
import com.novel.vippro.Models.NotificationType;
import com.novel.vippro.Models.Novel;
import com.novel.vippro.Models.SystemJob;
import com.novel.vippro.Models.SystemJobStatus;
import com.novel.vippro.Models.SystemJobType;
import com.novel.vippro.Payload.Response.PageResponse;
import com.novel.vippro.Repository.ChapterRepository;
import com.novel.vippro.Repository.FileMetadataRepository;
import com.novel.vippro.Repository.NovelRepository;
import com.novel.vippro.Repository.SystemJobRepository;

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
    private MessagePublisher messagePublisher;

    @Autowired
    private SystemJobRepository systemJobRepository;

    @Autowired
    private CacheManager cacheManager;

    @Autowired
    private NotificationService notificationService;


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
        chapter.setNovel(novel);
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

        if (!novelRepository.existsById(chapterDTO.novelId())) {
            throw new ResourceNotFoundException("Novel", "id", chapterDTO.novelId());
        }
        if (chapterRepository.findByNovelIdAndChapterNumber(chapterDTO.novelId(),
                chapterDTO.chapterNumber()) != null) {
            throw new RuntimeException(
                    "Chapter number already exists for this novel. Please choose a different number or update the existing chapter.");
        }

        Novel chapterNovel = novelRepository.findById(chapterDTO.novelId())
                .orElseThrow(() -> new ResourceNotFoundException("Novel", "id", chapterDTO.novelId()));

        Chapter chapter = new Chapter();
        chapter.setChapterNumber(chapterDTO.chapterNumber());
        chapter.setTitle(chapterDTO.title());
        chapter.setNovel(chapterNovel);

        Map<String, Object> contentMap = Map.of(
                "novelSlug", chapter.getNovel().getSlug(),
                "novelTitle", chapter.getNovel().getTitle(),
                "chapterNumber", chapter.getChapterNumber(),
                "chapterTitle", chapter.getTitle(),
                "content", chapterDTO.contentHtml());

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

    @CacheEvict(value = {"chapters", "novels"}, allEntries = true)
    @Transactional
    public List<Chapter> createChaptersBatch(List<CreateChapterDTO> chapterDTOs, int batchSize) {
        if (chapterDTOs == null || chapterDTOs.isEmpty()) {
            return Collections.emptyList();
        }

        ExecutorService executor = Executors.newFixedThreadPool(Math.min(8, Runtime.getRuntime().availableProcessors() * 2));
        List<CompletableFuture<Chapter>> futures = new ArrayList<>();

        for (CreateChapterDTO dto : chapterDTOs) {
            futures.add(CompletableFuture.supplyAsync(() -> {
                Novel novel = novelRepository.findById(dto.novelId())
                        .orElseThrow(() -> new ResourceNotFoundException("Novel", "id", dto.novelId()));

                if (chapterRepository.findByNovelIdAndChapterNumber(dto.novelId(), dto.chapterNumber()) != null) {
                    throw new RuntimeException("Chapter number already exists for this novel.");
                }

                Chapter chapter = new Chapter();
                chapter.setChapterNumber(dto.chapterNumber());
                chapter.setTitle(dto.title());
                chapter.setNovel(novel);

                Map<String, Object> contentMap = Map.of(
                        "novelSlug", novel.getSlug(),
                        "novelTitle", novel.getTitle(),
                        "chapterNumber", dto.chapterNumber(),
                        "chapterTitle", dto.title(),
                        "content", dto.contentHtml());

                String jsonContent;
                try {
                    jsonContent = objectMapper.writeValueAsString(contentMap);
                } catch (IOException e) {
                    throw new RuntimeException("Error converting chapter content to JSON", e);
                }

                String publicId = String.format("novels/%s/chapters/chap-%d.json", novel.getSlug(), dto.chapterNumber());
                String jsonUrl;
                try {
                    jsonUrl = fileStorageService.uploadFile(jsonContent.getBytes(), publicId, "application/json");
                } catch (IOException e) {
                    throw new RuntimeException("Error uploading chapter content to storage", e);
                }

                if (jsonUrl == null || jsonUrl.isEmpty()) {
                    throw new RuntimeException("Upload returned empty URL for " + publicId);
                }

                FileMetadata jsonFile = new FileMetadata();
                jsonFile.setPublicId(publicId);
                jsonFile.setContentType("application/json");
                jsonFile.setFileUrl(jsonUrl);
                jsonFile.setCreatedAt(Instant.now());
                chapter.setJsonFile(jsonFile);
                return chapter;
            }, executor));
        }

        List<Chapter> toSave = futures.stream()
                .map(CompletableFuture::join)
                .collect(Collectors.toList());

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

        executor.shutdown();
        return saved;
    }
    @Transactional
    public Chapter updateChapter(UUID id, CreateChapterDTO chapterDTO) {
        Chapter chapter = getChapterById(id);
        chapter.setChapterNumber(chapterDTO.chapterNumber());
        chapter.setTitle(chapterDTO.title());

        if (chapterDTO.novelId() != null) {
            chapter.setNovel(novelRepository.findById(chapterDTO.novelId())
                    .orElseThrow(() -> new RuntimeException("Novel not found with id: " + chapterDTO.novelId())));
        }

        Map<String, Object> contentMap = Map.of(
                "novelSlug", chapter.getNovel().getSlug(),
                "novelTitle", chapter.getNovel().getTitle(),
                "chapterNumber", chapter.getChapterNumber(),
                "chapterTitle", chapter.getTitle(),
                "content", chapterDTO.content());

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
        cacheManager.getCache("chapters").evict(chapter.getId());
        cacheManager.getCache("chapters").evict("'novel-slug-' + " + chapter.getNovel().getSlug() + " + '-chapter-' + " + chapter.getChapterNumber());
        logger.info("Audio generation completed for chapter id: {}", chapter.getId());
        var noti = CreateNotificationDTO.builder()
                .title("Chapter audio ready")
                .message(String.format("Audio for %s - Chapter %d is ready.",
                        chapter.getNovel().getTitle(), chapter.getChapterNumber()))
                .type(NotificationType.CHAPTER_UPDATE)
                .reference(chapter.getNovel().getSlug() + "/chapters/" + chapter.getChapterNumber())
                .build();
        notificationService.createNotification(noti);
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
        SystemJob job = new SystemJob();
        job.setJobType(SystemJobType.CHAPTER_AUDIO);
        job.setStatus(SystemJobStatus.QUEUED);
        job.setUserId(userId);
        Novel novel = chapter.getNovel();
        if (novel != null) {
            job.setNovelId(novel.getId());
            job.setSlug(novel.getSlug());
        }
        job.setChapterId(chapter.getId());
        job.setChapterNumber(chapter.getChapterNumber());
        job.setStatusMessage("Queued chapter audio generation");
        job = systemJobRepository.save(job);
        UUID targetNovelId = novel != null ? novel.getId() : null;
        String targetNovelSlug = novel != null ? novel.getSlug() : null;
        ChapterAudioMessage message = ChapterAudioMessage.builder()
                .chapterId(chapter.getId())
                .chapterNumber(chapter.getChapterNumber())
                .novelId(targetNovelId)
                .novelSlug(targetNovelSlug)
                .jobId(job.getId())
                .userId(userId)
                .build();
        messagePublisher.publishChapterAudio(message);
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
