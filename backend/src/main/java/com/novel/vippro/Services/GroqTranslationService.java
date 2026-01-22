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
public class GroqTranslationService {

    @Value("${groq.api.key:}")
    private String apiKey;

    @Value("${groq.api.model:llama-3.3-70b-versatile}")
    private String model;

    private static final String GROQ_API_URL = "https://api.groq.com/openai/v1/chat/completions";
    private static final int MAX_CHUNK_LENGTH = 6000; // Characters per chunk
    private static final int MAX_RETRIES = 3;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public GroqTranslationService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
        this.objectMapper = new ObjectMapper();
    }

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
     * Translate plain text
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
                Thread.sleep(100);
            }
        }

        return result.toString();
    }

    /**
     * Translate a single chunk using Groq API
     */
    private String translateChunk(String text) throws Exception {
        for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
            try {
                Map<String, Object> requestBody = new HashMap<>();
                requestBody.put("model", model);
                
                List<Map<String, String>> messages = new ArrayList<>();
                
                // System message
                Map<String, String> systemMsg = new HashMap<>();
                systemMsg.put("role", "system");
                systemMsg.put("content", buildSystemPrompt());
                messages.add(systemMsg);
                
                // User message
                Map<String, String> userMsg = new HashMap<>();
                userMsg.put("role", "user");
                userMsg.put("content", text);
                messages.add(userMsg);
                
                requestBody.put("messages", messages);
                requestBody.put("temperature", 0.3);
                requestBody.put("max_tokens", 8000);

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                headers.set("Authorization", "Bearer " + apiKey);

                HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

                ResponseEntity<String> response = restTemplate.exchange(
                        GROQ_API_URL,
                        HttpMethod.POST,
                        request,
                        String.class);

                if (response.getStatusCode() == HttpStatus.OK) {
                    return parseGroqResponse(response.getBody());
                }

            } catch (org.springframework.web.client.HttpClientErrorException e) {
                // Handle rate limit (429) specially
                if (e.getStatusCode().value() == 429) {
                    log.warn("Rate limit hit on attempt {}. Response: {}", attempt, e.getResponseBodyAsString());
                    
                    // Extract retry-after from headers or use exponential backoff
                    int retryAfter = extractRetryAfter(e);
                    if (attempt < MAX_RETRIES) {
                        log.info("Waiting {} seconds before retry", retryAfter);
                        Thread.sleep(retryAfter * 1000L);
                        continue;
                    }
                }
                
                log.warn("Translation attempt {} failed: {} - {}", attempt, e.getStatusCode(), e.getMessage());
                if (attempt == MAX_RETRIES) {
                    throw new Exception("Translation failed after " + MAX_RETRIES + " attempts: " + e.getMessage(), e);
                }
                Thread.sleep(3000 * attempt);
                
            } catch (Exception e) {
                log.warn("Translation attempt {} failed: {}", attempt, e.getMessage());
                if (attempt == MAX_RETRIES) {
                    throw new Exception("Translation failed after " + MAX_RETRIES + " attempts", e);
                }
                Thread.sleep(3000 * attempt);
            }
        }

        throw new Exception("Translation failed");
    }
    
    private int extractRetryAfter(org.springframework.web.client.HttpClientErrorException e) {
        try {
            // Try to get retry-after from headers
            String retryAfterHeader = e.getResponseHeaders().getFirst("retry-after");
            if (retryAfterHeader != null) {
                return Integer.parseInt(retryAfterHeader);
            }
            
            // Try to parse from response body (Groq/OpenAI format)
            String responseBody = e.getResponseBodyAsString();
            if (responseBody.contains("retry_after")) {
                // Parse JSON for retry_after field
                int start = responseBody.indexOf("\"retry_after\":") + 14;
                int end = responseBody.indexOf(",", start);
                if (end == -1) end = responseBody.indexOf("}", start);
                if (start > 13 && end > start) {
                    String retryStr = responseBody.substring(start, end).trim();
                    return (int) Double.parseDouble(retryStr);
                }
            }
        } catch (Exception ex) {
            log.debug("Failed to extract retry-after: {}", ex.getMessage());
        }
        
        // Default exponential backoff: 5s, 15s, 45s
        return 5 * (int) Math.pow(3, Math.min(2, Math.max(0, MAX_RETRIES - 1)));
    }

    private String buildSystemPrompt() {
        return "Bạn là một dịch giả chuyên nghiệp chuyên dịch tiểu thuyết mạng Trung Quốc (Tiên Hiệp, Huyền Ảo, Kiếm Hiệp) sang tiếng Việt.\n\n"
                + "Các quy tắc bắt buộc:\n"
                + "1. Thuật ngữ: Sử dụng từ Hán-Việt chuẩn cho tên riêng, địa danh, chiêu thức và các cảnh giới tu luyện (ví dụ: dùng 'Trúc Cơ' thay vì 'Xây nền', 'Kim Đan' thay vì 'Viên vàng', 'Nguyên Anh', 'Đạo hữu').\n"
                + "2. Xưng hô: Phải dựa vào ngữ cảnh để chọn đại từ nhân xưng phù hợp với thứ bậc và quan hệ (ví dụ: Ta/Ngươi, Huynh/Đệ, Tỷ/Muội, Tiền bối/Vãn bối, Tại hạ/Các hạ). TUYỆT ĐỐI KHÔNG dùng 'Tôi/Bạn' trừ khi truyện thuộc bối cảnh đô thị hiện đại.\n"
                + "3. Văn phong: Dịch mượt mà, văn vẻ, câu từ trau chuốt phù hợp với thể loại truyện. Tránh dịch word-for-word (dịch sát nghĩa đen) gây thô cứng.\n"
                + "4. Định dạng: GIỮ NGUYÊN chính xác tất cả các thẻ HTML (như <br>, <p>, <span>). Không được dịch, xóa hay thay đổi vị trí các thẻ này.\n"
                + "5. Đầu ra: CHỈ trả về nội dung văn bản đã dịch. Không bao gồm lời dẫn, ghi chú, giải thích hay văn bản gốc.\n\n"
                + "Hãy dịch văn bản tiếng Trung sau sang tiếng Việt:";
    }

    private String parseGroqResponse(String responseBody) throws Exception {
        JsonNode root = objectMapper.readTree(responseBody);
        JsonNode choices = root.get("choices");

        if (choices != null && choices.isArray() && choices.size() > 0) {
            JsonNode firstChoice = choices.get(0);
            JsonNode message = firstChoice.get("message");

            if (message != null) {
                JsonNode content = message.get("content");
                if (content != null) {
                    return content.asText().trim();
                }
            }
        }

        throw new Exception("Could not parse Groq response");
    }

    private List<TextSegment> extractTextSegments(Document doc) {
        List<TextSegment> segments = new ArrayList<>();
        Elements paragraphs = doc.select("p");

        int index = 0;
        for (Element element : paragraphs) {
            String text = element.text();
            if (!text.trim().isEmpty()) {
                segments.add(new TextSegment(index++, element, text));
            }
        }

        return segments;
    }

    private List<String> translateSegments(List<TextSegment> segments) throws Exception {
        List<String> translations = new ArrayList<>();

        // Translate each paragraph individually to ensure nothing gets lost
        for (TextSegment segment : segments) {
            try {
                String translated = translateText(segment.text);
                translations.add(translated);
                
                // Rate limiting between paragraphs
                if (segments.size() > 5) {
                    Thread.sleep(100);
                }
            } catch (Exception e) {
                log.error("Failed to translate segment {}: {}. Using original text.", segment.index, e.getMessage());
                // Fallback: keep original if translation fails
                translations.add(segment.text);
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
