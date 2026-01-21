package com.novel.vippro.Services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class GeminiTranslationService {
    
    @Value("${gemini.api.key:}")
    private String apiKey;
    
    @Value("${gemini.api.model:gemini-pro}")
    private String model;
    
    private static final String GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1beta/models/%s:generateContent";
    private static final int MAX_CHUNK_LENGTH = 4000; // Characters per chunk
    private static final int MAX_RETRIES = 3;
    
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    /**
     * Translate HTML content from Chinese to Vietnamese
     */
    public String translateHtmlToVietnamese(String htmlContent) throws Exception {
        if (htmlContent == null || htmlContent.trim().isEmpty()) {
            return htmlContent;
        }
        
        // Parse HTML to preserve structure
        Document doc = Jsoup.parse(htmlContent);
        
        // Extract text content while preserving structure
        List<TextSegment> segments = extractTextSegments(doc);
        
        if (segments.isEmpty()) {
            return htmlContent;
        }
        
        // Translate all segments
        List<String> translatedTexts = translateSegments(segments);
        
        // Reconstruct HTML with translations
        return reconstructHtml(doc, segments, translatedTexts);
    }
    
    /**
     * Translate plain text (batch multiple paragraphs)
     */
    public String translateText(String text) throws Exception {
        if (text == null || text.trim().isEmpty()) {
            return text;
        }
        
        // Split into chunks if too long
        List<String> chunks = splitIntoChunks(text, MAX_CHUNK_LENGTH);
        StringBuilder result = new StringBuilder();
        
        for (String chunk : chunks) {
            String translated = translateChunk(chunk);
            result.append(translated);
            
            // Rate limiting
            if (chunks.size() > 1) {
                Thread.sleep(1000);
            }
        }
        
        return result.toString();
    }
    
    /**
     * Translate a single chunk using Gemini API
     */
    private String translateChunk(String text) throws Exception {
        for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
            try {
                String url = String.format(GEMINI_API_URL, model) + "?key=" + apiKey;
                
                Map<String, Object> requestBody = new HashMap<>();
                
                // Create contents array
                List<Map<String, Object>> contents = new ArrayList<>();
                Map<String, Object> content = new HashMap<>();
                
                List<Map<String, String>> parts = new ArrayList<>();
                Map<String, String> part = new HashMap<>();
                part.put("text", buildTranslationPrompt(text));
                parts.add(part);
                
                content.put("parts", parts);
                contents.add(content);
                
                requestBody.put("contents", contents);
                
                // Add generation config
                Map<String, Object> generationConfig = new HashMap<>();
                generationConfig.put("temperature", 0.3);
                generationConfig.put("maxOutputTokens", 2048);
                requestBody.put("generationConfig", generationConfig);
                
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                
                HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
                
                ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    request,
                    String.class
                );
                
                if (response.getStatusCode() == HttpStatus.OK) {
                    return parseGeminiResponse(response.getBody());
                }
                
            } catch (Exception e) {
                log.warn("Translation attempt {} failed: {}", attempt, e.getMessage());
                if (attempt == MAX_RETRIES) {
                    throw new Exception("Translation failed after " + MAX_RETRIES + " attempts", e);
                }
                Thread.sleep(2000 * attempt); // Exponential backoff
            }
        }
        
        throw new Exception("Translation failed");
    }
    
    private String buildTranslationPrompt(String text) {
        return "Translate the following Chinese text to Vietnamese. " +
               "Keep the translation natural and fluent. " +
               "Preserve any HTML tags if present. " +
               "Only return the translated text without explanations:\n\n" + text;
    }
    
    private String parseGeminiResponse(String responseBody) throws Exception {
        JsonNode root = objectMapper.readTree(responseBody);
        JsonNode candidates = root.get("candidates");
        
        if (candidates != null && candidates.isArray() && candidates.size() > 0) {
            JsonNode firstCandidate = candidates.get(0);
            JsonNode content = firstCandidate.get("content");
            
            if (content != null) {
                JsonNode parts = content.get("parts");
                if (parts != null && parts.isArray() && parts.size() > 0) {
                    JsonNode text = parts.get(0).get("text");
                    if (text != null) {
                        return text.asText().trim();
                    }
                }
            }
        }
        
        throw new Exception("Could not parse Gemini response");
    }
    
    private List<TextSegment> extractTextSegments(Document doc) {
        List<TextSegment> segments = new ArrayList<>();
        Elements paragraphs = doc.select("p, div, span, h1, h2, h3, h4, h5, h6");
        
        int index = 0;
        for (Element element : paragraphs) {
            String text = element.ownText();
            if (!text.trim().isEmpty()) {
                segments.add(new TextSegment(index++, element, text));
            }
        }
        
        return segments;
    }
    
    private List<String> translateSegments(List<TextSegment> segments) throws Exception {
        List<String> translations = new ArrayList<>();
        
        // Batch translate for efficiency
        StringBuilder batch = new StringBuilder();
        List<Integer> batchIndices = new ArrayList<>();
        
        for (int i = 0; i < segments.size(); i++) {
            TextSegment segment = segments.get(i);
            
            if (batch.length() + segment.text.length() > MAX_CHUNK_LENGTH && batch.length() > 0) {
                // Translate current batch
                String batchTranslation = translateText(batch.toString());
                String[] parts = batchTranslation.split("\n");
                
                for (String part : parts) {
                    translations.add(part);
                }
                
                batch = new StringBuilder();
                batchIndices.clear();
            }
            
            batch.append(segment.text).append("\n");
            batchIndices.add(i);
        }
        
        // Translate remaining batch
        if (batch.length() > 0) {
            String batchTranslation = translateText(batch.toString());
            String[] parts = batchTranslation.split("\n");
            for (String part : parts) {
                translations.add(part);
            }
        }
        
        return translations;
    }
    
    private String reconstructHtml(Document doc, List<TextSegment> segments, List<String> translations) {
        for (int i = 0; i < Math.min(segments.size(), translations.size()); i++) {
            TextSegment segment = segments.get(i);
            String translation = translations.get(i);
            segment.element.text(translation);
        }
        
        return doc.body().html();
    }
    
    private List<String> splitIntoChunks(String text, int maxLength) {
        List<String> chunks = new ArrayList<>();
        
        if (text.length() <= maxLength) {
            chunks.add(text);
            return chunks;
        }
        
        String[] paragraphs = text.split("\n");
        StringBuilder currentChunk = new StringBuilder();
        
        for (String paragraph : paragraphs) {
            if (currentChunk.length() + paragraph.length() > maxLength && currentChunk.length() > 0) {
                chunks.add(currentChunk.toString());
                currentChunk = new StringBuilder();
            }
            currentChunk.append(paragraph).append("\n");
        }
        
        if (currentChunk.length() > 0) {
            chunks.add(currentChunk.toString());
        }
        
        return chunks;
    }
    
    private static class TextSegment {
        int index;
        Element element;
        String text;
        
        TextSegment(int index, Element element, String text) {
            this.index = index;
            this.element = element;
            this.text = text;
        }
    }
}
