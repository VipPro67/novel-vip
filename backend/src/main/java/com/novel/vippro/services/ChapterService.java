package com.novel.vippro.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.novel.vippro.dto.ChapterCreateDTO;
import com.novel.vippro.dto.ChapterDetailDTO;
import com.novel.vippro.dto.ChapterListDTO;
import com.novel.vippro.exception.ResourceNotFoundException;
import com.novel.vippro.models.Chapter;
import com.novel.vippro.repository.ChapterRepository;
import com.novel.vippro.repository.NovelRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
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
    private RestTemplate restTemplate;

    public Page<ChapterListDTO> getChaptersByNovelDTO(UUID novelId, Pageable pageable) {
        Page<Chapter> chapters = getChaptersByNovel(novelId, pageable);
        return chapters.map(this::convertToChapterListDTO);
    }

    public ChapterListDTO getChapterByNumberDTO(UUID novelId, Integer chapterNumber) {
        Chapter chapter = getChapterByNovelIdAndNumber(novelId, chapterNumber);
        return convertToChapterListDTO(chapter);
    }

    public ChapterDetailDTO getChapterDetailDTO(UUID id) throws IOException {
        Chapter chapter = getChapterById(id);
        Map<String, Object> content = getChapterContent(chapter);

        ChapterDetailDTO dto = convertToChapterDetailDTO(chapter);
        dto.setContent((String) content.get("content"));
        return dto;
    }

    @Transactional
    public ChapterListDTO createChapterDTO(ChapterCreateDTO chapterDTO) throws IOException {
        Chapter chapter = createChapter(chapterDTO);
        return convertToChapterListDTO(chapter);
    }

    @Transactional
    public ChapterListDTO updateChapterDTO(UUID id, ChapterCreateDTO chapterDTO) throws IOException {
        Chapter chapter = updateChapter(id, chapterDTO);
        return convertToChapterListDTO(chapter);
    }

    private ChapterListDTO convertToChapterListDTO(Chapter chapter) {
        ChapterListDTO dto = new ChapterListDTO();
        dto.setId(chapter.getId());
        dto.setChapterNumber(chapter.getChapterNumber());
        dto.setTitle(chapter.getTitle());
        dto.setNovelId(chapter.getNovel().getId());
        dto.setNovelTitle(chapter.getNovel().getTitle());
        dto.setCreatedAt(chapter.getCreatedAt());
        dto.setUpdatedAt(chapter.getUpdatedAt());
        return dto;
    }

    private ChapterDetailDTO convertToChapterDetailDTO(Chapter chapter) {
        ChapterDetailDTO dto = new ChapterDetailDTO();
        dto.setId(chapter.getId());
        dto.setChapterNumber(chapter.getChapterNumber());
        dto.setTitle(chapter.getTitle());
        dto.setNovelId(chapter.getNovel().getId());
        dto.setNovelTitle(chapter.getNovel().getTitle());
        dto.setCreatedAt(chapter.getCreatedAt());
        dto.setUpdatedAt(chapter.getUpdatedAt());
        return dto;
    }

    public Page<Chapter> getChaptersByNovel(UUID novelId, Pageable pageable) {
        if (!novelRepository.existsById(novelId)) {
            throw new ResourceNotFoundException("Novel", "id", novelId);
        }
        return chapterRepository.findByNovelIdOrderByChapterNumberAsc(novelId, pageable);
    }

    public Chapter getChapterById(UUID id) {
        return chapterRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Chapter", "id", id));
    }

    public Chapter getChapterByNovelIdAndNumber(UUID novelId, Integer chapterNumber) {
        return chapterRepository.findByNovelIdAndChapterNumber(novelId, chapterNumber);
    }

    @Transactional
    public Chapter saveChapter(Chapter chapter, Map<String, Object> content) throws IOException {
        // Convert content to JSON string
        String jsonContent = objectMapper.writeValueAsString(content);

        // Generate a unique public ID for the chapter
        String publicId = String.format("chapters/%s/%d",
                chapter.getNovel().getSlug(),
                chapter.getChapterNumber());

        // Upload JSON content to Cloudinary
        String jsonUrl = cloudinaryService.uploadJson(jsonContent, publicId);

        // Set the JSON URL in the chapter
        chapter.setJsonUrl(jsonUrl);

        // Save the chapter to the database
        return chapterRepository.save(chapter);
    }

    public Map<String, Object> getChapterContent(Chapter chapter) throws IOException {
        // Get the JSON content URL from Cloudinary
        String jsonUrl = chapter.getJsonUrl();

        if (jsonUrl == null || jsonUrl.isEmpty()) {
            throw new IOException("Chapter content URL is not available");
        }

        // Fetch the JSON content from the URL
        String jsonContent = restTemplate.getForObject(jsonUrl, String.class);

        if (jsonContent == null || jsonContent.isEmpty()) {
            throw new IOException("Failed to fetch chapter content");
        }

        // Parse the JSON content
        return objectMapper.readValue(jsonContent, Map.class);
    }

    private String extractPublicIdFromUrl(String url) {
        // Extract the public ID from the Cloudinary URL
        // Example URL:
        // https://res.cloudinary.com/drpudphzv/raw/upload/v1744167203/novel/chapters/bohuupgcdzv4edx9m47y.json
        // Public ID: novel/chapters/bohuupgcdzv4edx9m47y

        if (url == null || url.isEmpty()) {
            return "";
        }

        try {
            // Split the URL by "/"
            String[] parts = url.split("/");

            // Find the index of "upload" or "raw"
            int uploadIndex = -1;
            for (int i = 0; i < parts.length; i++) {
                if (parts[i].equals("upload") || parts[i].equals("raw")) {
                    uploadIndex = i;
                    break;
                }
            }

            if (uploadIndex == -1 || uploadIndex + 1 >= parts.length) {
                return "";
            }

            // The public ID is everything after the version number
            StringBuilder publicId = new StringBuilder();
            for (int i = uploadIndex + 2; i < parts.length; i++) {
                publicId.append(parts[i]);
                if (i < parts.length - 1) {
                    publicId.append("/");
                }
            }

            // Remove the file extension
            String result = publicId.toString();
            int dotIndex = result.lastIndexOf(".");
            if (dotIndex > 0) {
                result = result.substring(0, dotIndex);
            }

            return result;
        } catch (Exception e) {
            return "";
        }
    }

    @Transactional
    public Chapter createChapter(ChapterCreateDTO chapterDTO) throws IOException {
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
        String jsonContent = objectMapper.writeValueAsString(contentMap);

        // Generate a unique public ID for the chapter
        String publicId = String.format("chapters/%s/%d",
                chapter.getNovel().getSlug(),
                chapter.getChapterNumber());

        // Upload JSON content to Cloudinary
        String jsonUrl = cloudinaryService.uploadJson(jsonContent, publicId);

        // Set the JSON URL in the chapter
        chapter.setJsonUrl(jsonUrl);

        // Save the chapter to the database
        return chapterRepository.save(chapter);
    }

    @Transactional
    public Chapter updateChapter(UUID id, ChapterCreateDTO chapterDTO) throws IOException {
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
        String jsonContent = objectMapper.writeValueAsString(contentMap);

        // Generate a unique public ID for the chapter
        String publicId = String.format("chapters/%s/%d",
                chapter.getNovel().getSlug(),
                chapter.getChapterNumber());

        // Upload JSON content to Cloudinary
        String jsonUrl = cloudinaryService.uploadJson(jsonContent, publicId);

        // Set the JSON URL in the chapter
        chapter.setJsonUrl(jsonUrl);

        // Save the chapter to the database
        return chapterRepository.save(chapter);
    }

    @Transactional
    public void deleteChapter(UUID id) {
        Chapter chapter = getChapterById(id);
        chapterRepository.delete(chapter);
    }
}