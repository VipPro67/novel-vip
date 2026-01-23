package com.novel.vippro.Services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
@Slf4j
public class GeminiTranslationService {

    @Value("${gemini.api.key:}")
    private String apiKey;

    @Value("${gemini.api.model:gemini-1.5-flash}")
    private String model;

    private static final String GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1beta/models/%s:generateContent";
    
    private static final int MAX_BATCH_CHARS = 3500; 
    private static final String BATCH_DELIMITER = " ||| "; 
    private static final String SPLIT_REGEX = "\\s*\\|\\|\\|\\s*"; 
    private static final int MAX_RETRIES = 3;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public GeminiTranslationService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
        this.objectMapper = new ObjectMapper();
    }

    public String translateHtmlToVietnamese(String htmlContent) {
        if (htmlContent == null || htmlContent.trim().isEmpty()) {
            return htmlContent;
        }

        try {
            Document doc = Jsoup.parse(htmlContent);

            List<TextSegment> segments = extractTextSegments(doc);
            if (segments.isEmpty()) return htmlContent;

            List<String> translatedTexts = new ArrayList<>(Collections.nCopies(segments.size(), ""));
            List<TextSegment> currentBatch = new ArrayList<>();
            int currentBatchLength = 0;

            for (int i = 0; i < segments.size(); i++) {
                TextSegment segment = segments.get(i);
                
                currentBatch.add(segment);
                currentBatchLength += segment.text.length();

                if (currentBatchLength >= MAX_BATCH_CHARS || i == segments.size() - 1) {
                    processBatch(currentBatch, translatedTexts);
                    
                    currentBatch.clear();
                    currentBatchLength = 0;

                    if (i < segments.size() - 1) Thread.sleep(1000); 
                }
            }
            return reconstructHtml(doc, segments, translatedTexts);

        } catch (Exception e) {
            log.error("Error during HTML translation", e);
            return htmlContent; 
        }
    }

    private void processBatch(List<TextSegment> batch, List<String> finalTranslations) {
        if (batch.isEmpty()) return;

        StringBuilder combinedText = new StringBuilder();
        for (int i = 0; i < batch.size(); i++) {
            combinedText.append(batch.get(i).text);
            if (i < batch.size() - 1) {
                combinedText.append(BATCH_DELIMITER);
            }
        }

        try {
            String translatedResult = callGeminiApiWithRetry(combinedText.toString());

            String[] splitResults = translatedResult.split(SPLIT_REGEX);

            for (int i = 0; i < batch.size(); i++) {
                TextSegment original = batch.get(i);
                if (i < splitResults.length) {
                    finalTranslations.set(original.index, splitResults[i].trim());
                } else {
                    log.warn("Translation count mismatch. Index {} using original text.", original.index);
                    finalTranslations.set(original.index, original.text);
                }
            }
        } catch (Exception e) {
            log.error("Batch translation failed: {}", e.getMessage());
            for (TextSegment seg : batch) {
                finalTranslations.set(seg.index, seg.text);
            }
        }
    }

    private String callGeminiApiWithRetry(String text) throws Exception {
        int attempt = 0;
        long waitTime = 2000; 

        while (attempt < MAX_RETRIES) {
            try {
                attempt++;
                return sendApiRequest(text);

            } catch (HttpClientErrorException e) {
                if (e.getStatusCode() == HttpStatus.TOO_MANY_REQUESTS) { // 429
                    log.warn("Rate limit hit (Attempt {}/{}). Waiting {}ms...", attempt, MAX_RETRIES, waitTime);
                    Thread.sleep(waitTime);
                    waitTime *= 2; 
                } else {
                    throw e; 
                }
            } catch (Exception e) {
                log.error("API Error: {}", e.getMessage());
                if (attempt == MAX_RETRIES) throw e;
                Thread.sleep(waitTime);
            }
        }
        throw new Exception("Failed to translate after " + MAX_RETRIES + " retries");
    }

    private String sendApiRequest(String text) throws Exception {
        String url = String.format(GEMINI_API_URL, model) + "?key=" + apiKey;

        Map<String, Object> requestBody = new HashMap<>();
        
        List<Map<String, Object>> contents = new ArrayList<>();
        Map<String, Object> content = new HashMap<>();
        List<Map<String, String>> parts = new ArrayList<>();
        Map<String, String> part = new HashMap<>();
        part.put("text", buildTranslationPrompt(text));
        parts.add(part);
        content.put("parts", parts);
        contents.add(content);
        requestBody.put("contents", contents);

        Map<String, Object> config = new HashMap<>();
        config.put("temperature", 0.3);
        requestBody.put("generationConfig", config);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);

        if (response.getStatusCode() == HttpStatus.OK) {
            return parseGeminiResponse(response.getBody());
        }
        throw new Exception("Non-OK Response: " + response.getStatusCode());
    }

    private String buildTranslationPrompt(String text) {
        return "Vai trò: Bạn là dịch giả tiểu thuyết Trung Quốc (Tiên Hiệp, Huyền Ảo) chuyên nghiệp.\n"
             + "Nhiệm vụ: Dịch văn bản sau sang tiếng Việt. Sử dụng từ Hán Việt chuẩn (Trúc Cơ, Nguyên Anh, Đạo hữu...).\n"
             + "QUAN TRỌNG VỀ ĐỊNH DẠNG (BẮT BUỘC):\n"
             + "1. Văn bản chứa nhiều đoạn, ngăn cách bởi ký tự: ' ||| '\n"
             + "2. Bạn phải dịch từng đoạn nhưng GIỮ NGUYÊN ký tự ' ||| ' ở đúng vị trí để ngăn cách các câu.\n"
             + "3. KHÔNG được gộp các đoạn lại. Nếu input có 5 dấu ' ||| ', output cũng phải có 5 dấu.\n"
             + "4. Chỉ trả về kết quả dịch, không thêm lời dẫn.\n\n"
             + "Văn bản:\n" + text;
    }

    private String parseGeminiResponse(String responseBody) throws Exception {
        JsonNode root = objectMapper.readTree(responseBody);
        JsonNode candidates = root.path("candidates");
        if (candidates.isArray() && candidates.size() > 0) {
            JsonNode textNode = candidates.get(0).path("content").path("parts").get(0).path("text");
            if (!textNode.isMissingNode()) {
                return textNode.asText().trim();
            }
        }
        throw new Exception("Invalid JSON response from Gemini");
    }

    private List<TextSegment> extractTextSegments(Document doc) {
        List<TextSegment> segments = new ArrayList<>();
        Elements elements = doc.select("p, h1, h2, h3, h4, h5, h6, li");

        int index = 0;
        for (Element element : elements) {
            String text = element.text(); 
            if (text != null && text.length() > 1 && !isNumberOrSymbol(text)) {
                segments.add(new TextSegment(index++, element, text));
            }
        }
        return segments;
    }

    private boolean isNumberOrSymbol(String text) {
        return text.matches("^[0-9\\s\\p{Punct}]+$");
    }

    private String reconstructHtml(Document doc, List<TextSegment> segments, List<String> translations) {
        for (int i = 0; i < segments.size(); i++) {
            TextSegment segment = segments.get(i);
            String translation = translations.get(i);
            
            if (translation != null && !translation.isEmpty()) {
                segment.element.text(translation);
            }
        }
        return doc.body().html();
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