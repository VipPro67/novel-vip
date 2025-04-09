package com.novel.vippro.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.novel.vippro.models.Chapter;
import com.novel.vippro.repository.ChapterRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Map;

@Service
public class ChapterService {

    @Autowired
    private ChapterRepository chapterRepository;

    @Autowired
    private CloudinaryService cloudinaryService;

    @Autowired
    private ObjectMapper objectMapper;

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
        // Extract public ID from the JSON URL
        String publicId = extractPublicIdFromUrl(chapter.getJsonUrl());

        // Get the JSON content URL from Cloudinary
        String jsonUrl = cloudinaryService.getJsonContent(publicId);

        // TODO: Implement fetching and parsing JSON content from the URL
        // This would typically involve making an HTTP request to the URL
        // and parsing the response as JSON

        return null; // Placeholder
    }

    private String extractPublicIdFromUrl(String url) {
        // TODO: Implement extracting public ID from Cloudinary URL
        // This would typically involve parsing the URL to extract the public ID
        return ""; // Placeholder
    }
}