package com.novel.vippro.Services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.novel.vippro.DTO.Chapter.CreateChapterDTO;
import com.novel.vippro.DTO.Chapter.ChapterDTO;
import com.novel.vippro.DTO.Chapter.ChapterDetailDTO;
import com.novel.vippro.Exception.ResourceNotFoundException;
import com.novel.vippro.Mapper.Mapper;
import com.novel.vippro.Models.Chapter;
import com.novel.vippro.Models.FileMetadata;
import com.novel.vippro.Models.Novel;
import com.novel.vippro.Payload.Response.PageResponse;
import com.novel.vippro.Repository.ChapterRepository;
import com.novel.vippro.Repository.FileMetadataRepository;
import com.novel.vippro.Repository.NovelRepository;

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
import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

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
    private RestTemplate restTemplate;

    @Autowired
    @Qualifier("openAiEdgeTTS")
    private TextToSpeechService textToSpeechService;

    @Autowired
    private FavoriteService favoriteService;

    @Autowired
    private FileMetadataRepository fileMetadataRepository;

    private static final Logger logger = LogManager.getLogger(ChapterService.class);

    @Cacheable(value = "chapters", key = "#id")
    public ChapterDetailDTO getChapterDetailDTO(UUID id) {
        Chapter chapter = getChapterById(id);
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
        // Get the JSON content URL from Cloudinary
        String jsonUrl = chapter.getJsonFile().getFileUrl();
        // jsonurl only save publicId not full url

        if (jsonUrl == null || jsonUrl.isEmpty()) {
            throw new ResourceNotFoundException("Chapter content", "jsonUrl", jsonUrl);
        }

        // Fetch the JSON content from the URL
        String jsonContent = restTemplate.getForObject(jsonUrl, String.class);

        if (jsonContent == null || jsonContent.isEmpty()) {
            throw new ResourceNotFoundException("Chapter content", "jsonContent", jsonContent);
        }

        // Parse the JSON content
        Map<String, Object> contentMap;
        try {
            contentMap = objectMapper.readValue(jsonContent, Map.class);

        } catch (IOException e) {
            throw new RuntimeException("Error parsing chapter content: " + e.getMessage(), e);
        }
        if (contentMap == null || contentMap.isEmpty()) {
            throw new ResourceNotFoundException("Chapter content", "contentMap", contentMap);
        }
        return contentMap;
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
        fileMetadataRepository.save(jsonFile);
        chapter.setJsonFile(jsonFile);

        novelRepository.findById(chapter.getNovel().getId()).ifPresent(novel -> {
            novel.setTotalChapters(novel.getTotalChapters() + 1);
            novel.setUpdatedAt(Instant.now());
            novelRepository.save(novel);
        });

        chapterRepository.save(chapter);
        favoriteService.notifyFavorites(chapter.getNovel().getId());
        return chapter;
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
                "content", chapterDTO.getContentHtml());

        String jsonContent;
        try {
            jsonContent = objectMapper.writeValueAsString(contentMap);
        } catch (IOException e) {
            throw new RuntimeException("Error converting chapter content to JSON: " + e.getMessage(), e);
        }
        if (chapterRepository.findByNovelIdAndChapterNumber(chapter.getNovel().getId(),
                chapter.getChapterNumber()) != null) {
            throw new RuntimeException(
                    "Chapter number already exists for this novel. Please choose a different number or update the existing chapter.");
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
        fileMetadataRepository.save(jsonFile);
        chapter.setJsonFile(jsonFile);

        novelRepository.findById(chapter.getNovel().getId()).ifPresent(novel -> {
            novel.setTotalChapters(novel.getTotalChapters() + 1);
            novel.setUpdatedAt(Instant.now());
            novelRepository.save(novel);
        });
        return chapterRepository.save(chapter);
    }

    @Transactional
    public ChapterDetailDTO createChapterAudio(UUID id) {
        Chapter chapter = getChapterById(id);
        if (chapter == null) {
            throw new ResourceNotFoundException("Chapter", "id", id);
        }

        if (chapter.getAudioFile() != null) {
            ChapterDetailDTO dto = mapper.ChaptertoChapterDetailDTO(chapter);
            return dto;
        }

        Map<String, Object> content = getChapterContent(chapter);
        String textToConvert = chapter.getTitle() + "\n" + content.get("content");

        textToConvert = textToConvert.replaceAll("<[^>]*>", "");

        String audioUrl;
        try {
            audioUrl = textToSpeechService.synthesizeSpeech(
                    textToConvert,
                    chapter.getNovel().getSlug(),
                    chapter.getChapterNumber());
        } catch (IOException | IllegalStateException e) {
            throw new RuntimeException("Error generating audio for chapter: " + e.getMessage(), e);
        }

        FileMetadata audioFile = new FileMetadata();
        audioFile.setPublicId("novels/"+chapter.getNovel().getSlug() + "/audios/" + "chap-" + chapter.getChapterNumber() + "-audio.mp3");
        audioFile.setContentType("audio/mpeg");
        audioFile.setFileUrl(audioUrl);
        audioFile.setFileName("chap-" + chapter.getChapterNumber() + "-audio.mp3");
        audioFile.setType("audio");
        audioFile.setSize(0);
        audioFile.setUpdatedAt(Instant.now());
        fileMetadataRepository.save(audioFile);
        chapter.setAudioFile(audioFile);
        chapterRepository.save(chapter);
        ChapterDetailDTO dto = mapper.ChaptertoChapterDetailDTO(chapter);
        return dto;
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
}
