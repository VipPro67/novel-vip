package com.novel.vippro.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.novel.vippro.dto.ChapterCreateDTO;
import com.novel.vippro.dto.ChapterDTO;
import com.novel.vippro.dto.ChapterDetailDTO;
import com.novel.vippro.dto.ChapterListDTO;
import com.novel.vippro.exception.ResourceNotFoundException;
import com.novel.vippro.mapper.Mapper;
import com.novel.vippro.models.Chapter;
import com.novel.vippro.payload.response.PageResponse;
import com.novel.vippro.repository.ChapterRepository;
import com.novel.vippro.repository.NovelRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Service
public class ChapterService {

    @Autowired
    private ChapterRepository chapterRepository;

    @Autowired
    private NovelRepository novelRepository;

    @Autowired
    private CloudinaryService cloudinaryService;

    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private Mapper mapper;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private TextToSpeechService textToSpeechService;

    @Cacheable(value = "chapters", key = "#id")
    public ChapterDetailDTO getChapterDetailDTO(UUID id) {
        Chapter chapter = getChapterById(id);
        ChapterDetailDTO dto = mapper.ChaptertoChapterDetailDTO(chapter);
        return dto;
    }

    @Cacheable(value = "chapters", key = "'novel-' + #novelId + '-chapter-' + #chapterNumber")
    public ChapterDetailDTO getChapterByNumberDTO(UUID novelId, Integer chapterNumber) {
        if (!novelRepository.existsById(novelId)) {
            throw new ResourceNotFoundException("Novel", "id", novelId);
        }
        ChapterDetailDTO dto = chapterRepository.findByNovelIdAndChapterNumber(novelId, chapterNumber);
        return dto;
    }

    @Cacheable(value = "chapters", key = "'novel-' + #novelId + '-page-' + #pageable.pageNumber")
    public PageResponse<ChapterListDTO> getChaptersByNovelDTO(UUID novelId, Pageable pageable) {
        Page<Chapter> chapters = chapterRepository.findByNovelIdOrderByChapterNumberAsc(novelId, pageable);
        return new PageResponse<>(chapters.map(mapper::ChaptertoChapterListDTO));
    }

    @CacheEvict(value = { "chapters", "novels" }, allEntries = true)
    @Transactional
    public ChapterListDTO createChapterDTO(ChapterCreateDTO chapterDTO) {
        Chapter chapter = createChapter(chapterDTO);
        return mapper.ChaptertoChapterListDTO(chapter);
    }

    @CacheEvict(value = { "chapters", "novels" }, key = "#id")
    @Transactional
    public ChapterListDTO updateChapterDTO(UUID id, ChapterCreateDTO chapterDTO) {
        Chapter chapter = updateChapter(id, chapterDTO);
        if (chapter == null) {
            throw new ResourceNotFoundException("Chapter", "id", id);
        }
        return mapper.ChaptertoChapterListDTO(chapter);
    }

    @CacheEvict(value = { "chapters", "novels" }, allEntries = true)
    @Transactional
    public void deleteChapter(UUID id) {
        Chapter chapter = getChapterById(id);
        chapterRepository.delete(chapter);
        // Update total chapters and updated at
        novelRepository.findById(chapter.getNovel().getId()).ifPresent(novel -> {
            novel.setTotalChapters(novel.getTotalChapters() - 1);
            novel.setUpdatedAt(LocalDateTime.now());
            novelRepository.save(novel);
        });
    }

    public PageResponse<Chapter> getChaptersByNovel(UUID novelId, Pageable pageable) {
        if (!novelRepository.existsById(novelId)) {
            throw new ResourceNotFoundException("Novel", "id", novelId);
        }
        return new PageResponse<>(chapterRepository.findByNovelIdOrderByChapterNumberAsc(novelId, pageable));
    }

    public Chapter getChapterById(UUID id) {
        return chapterRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Chapter", "id", id));
    }

    public ChapterDetailDTO getChapterByNovelIdAndNumber(UUID novelId, Integer chapterNumber) {
        return chapterRepository.findByNovelIdAndChapterNumber(novelId, chapterNumber);
    }

    public Map<String, Object> getChapterContent(Chapter chapter) {
        // Get the JSON content URL from Cloudinary
        String jsonUrl = chapter.getJsonUrl();
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
    public Chapter createChapter(ChapterCreateDTO chapterDTO) {

        // Check if the novel exists
        if (!novelRepository.existsById(chapterDTO.getNovelId())) {
            throw new ResourceNotFoundException("Novel", "id", chapterDTO.getNovelId());
        }
        // Check if the chapter number is unique for the novel
        if (chapterRepository.findByNovelIdAndChapterNumber(chapterDTO.getNovelId(),
                chapterDTO.getChapterNumber()) != null) {
            throw new RuntimeException(
                    "Chapter number already exists for this novel. Please choose a different number or update the existing chapter.");
        }
        Chapter chapter = new Chapter();
        chapter.setChapterNumber(chapterDTO.getChapterNumber());
        chapter.setTitle(chapterDTO.getTitle());

        // Set the novel
        chapter.setNovel(novelRepository.findById(chapterDTO.getNovelId())
                .orElseThrow(() -> new RuntimeException("Novel not found with id: " + chapterDTO.getNovelId())));

        // Create content map
        Map<String, Object> contentMap = Map.of(
                "novelSlug", chapter.getNovel().getSlug(),
                "novelTitle", chapter.getNovel().getTitle(),
                "chapterNumber", chapter.getChapterNumber(),
                "chapterTitle", chapter.getTitle(),
                "content", chapterDTO.getContent());

        // Convert content to JSON string
        String jsonContent;
        try {
            jsonContent = objectMapper.writeValueAsString(contentMap);
        } catch (IOException e) {
            throw new RuntimeException("Error converting chapter content to JSON: " + e.getMessage(), e);
        }

        // Generate a unique public ID for the chapter
        String publicId = String.format("novels/%s/chapters/%d.json",
                chapter.getNovel().getSlug(),
                chapter.getChapterNumber());

        // Upload JSON content to Cloudinary
        String jsonUrl;
        try {
            jsonUrl = cloudinaryService.uploadFile(jsonContent.getBytes(), publicId, "application/json");
        } catch (IOException e) {
            throw new RuntimeException("Error uploading chapter content to Cloudinary: " + e.getMessage(), e);
        }
        if (jsonUrl == null || jsonUrl.isEmpty()) {
            throw new RuntimeException("Error uploading chapter content to Cloudinary: URL is empty");
        }

        // Set the JSON URL in the chapter only use publicId not save full url
        // chapter.setJsonUrl(jsonUrl);
        chapter.setJsonUrl(publicId);

        // Update total chapters and updated at
        novelRepository.findById(chapter.getNovel().getId()).ifPresent(novel -> {
            novel.setTotalChapters(novel.getTotalChapters() + 1);
            novel.setUpdatedAt(LocalDateTime.now());
            novelRepository.save(novel);
        });

        // Save the chapter to the database
        return chapterRepository.save(chapter);
    }

    @Transactional
    public Chapter updateChapter(UUID id, ChapterCreateDTO chapterDTO) {
        Chapter chapter = getChapterById(id);
        chapter.setChapterNumber(chapterDTO.getChapterNumber());
        chapter.setTitle(chapterDTO.getTitle());

        if (chapterDTO.getNovelId() != null) {
            chapter.setNovel(novelRepository.findById(chapterDTO.getNovelId())
                    .orElseThrow(() -> new RuntimeException("Novel not found with id: " + chapterDTO.getNovelId())));
        }

        // Create content map
        Map<String, Object> contentMap = Map.of(
                "novelSlug", chapter.getNovel().getSlug(),
                "novelTitle", chapter.getNovel().getTitle(),
                "chapterNumber", chapter.getChapterNumber(),
                "chapterTitle", chapter.getTitle(),
                "content", chapterDTO.getContent());

        // Convert content to JSON string
        String jsonContent;
        try {
            jsonContent = objectMapper.writeValueAsString(contentMap);
        } catch (IOException e) {
            throw new RuntimeException("Error converting chapter content to JSON: " + e.getMessage(), e);
        }
        // Check if the chapter number is unique for the novel
        if (chapterRepository.findByNovelIdAndChapterNumber(chapter.getNovel().getId(),
                chapter.getChapterNumber()) != null) {
            throw new RuntimeException(
                    "Chapter number already exists for this novel. Please choose a different number or update the existing chapter.");
        }

        // Generate a unique public ID for the chapter
        String publicId = String.format("novels/%s/chapters/%d" + ".json",
                chapter.getNovel().getSlug(),
                chapter.getChapterNumber());

        // Upload JSON content to Cloudinary
        String jsonUrl;
        try {
            jsonUrl = cloudinaryService.uploadFile(jsonContent.getBytes(), publicId, "application/json");
        } catch (IOException e) {
            throw new RuntimeException("Error uploading chapter content to Cloudinary: " + e.getMessage(), e);
        }
        if (jsonUrl == null || jsonUrl.isEmpty()) {
            throw new RuntimeException("Error uploading chapter content to Cloudinary: URL is empty");
        }

        // Set the JSON URL in the chapter
        chapter.setJsonUrl(jsonUrl);

        // Update total chapters and updated at
        novelRepository.findById(chapter.getNovel().getId()).ifPresent(novel -> {
            novel.setTotalChapters(novel.getTotalChapters() + 1);
            novel.setUpdatedAt(LocalDateTime.now());
            novelRepository.save(novel);
        });
        // Save the chapter to the database
        return chapterRepository.save(chapter);
    }

    @Transactional
    public ChapterDetailDTO createChapterAudio(UUID id) {
        Chapter chapter = getChapterById(id);
        // check if chapter is not null
        if (chapter == null) {
            throw new ResourceNotFoundException("Chapter", "id", id);
        }

        // check if chapter has audio url
        if (chapter.getAudioUrl() != null) {
            ChapterDetailDTO dto = mapper.ChaptertoChapterDetailDTO(chapter);
            return dto;
        }

        // Get chapter content
        Map<String, Object> content = getChapterContent(chapter);
        String textToConvert = chapter.getTitle() + "\n" + content.get("content");

        // remove all html tags
        textToConvert = textToConvert.replaceAll("<[^>]*>", "");

        // Generate audio using TextToSpeechService
        String audioUrl;
        try {
            audioUrl = textToSpeechService.synthesizeSpeech(
                    textToConvert,
                    chapter.getNovel().getSlug(),
                    chapter.getChapterNumber());
        } catch (IOException e) {
            throw new RuntimeException("Error generating audio for chapter: " + e.getMessage(), e);
        }

        // Update chapter with audio URL by using publicId
        chapter.setAudioUrl(audioUrl);
        chapterRepository.save(chapter);

        ChapterDetailDTO dto = mapper.ChaptertoChapterDetailDTO(chapter);
        return dto;
    }
}