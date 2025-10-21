package com.novel.vippro.Services;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import com.novel.vippro.Models.FileMetadata;

@Service("openAiEdgeTTS")
@ConditionalOnProperty(name = "texttospeech.provider", havingValue = "openai-edge", matchIfMissing = true)
public class OpenAiEdgeTextToSpeechService implements TextToSpeechService {

    private static final Logger logger = LoggerFactory.getLogger(OpenAiEdgeTextToSpeechService.class);

    private static final String AUDIO_CONTENT_TYPE = "audio/mpeg";
    private static final String DEFAULT_RESPONSE_FORMAT = "mp3";
    private static final double DEFAULT_SPEED = 1.0;

    @Autowired
    private FileService fileService;

    private final HttpClient httpClient;

    @Value("${openai.edge.tts.api-key:}")
    private String apiKey;

    @Value("${openai.edge.tts.base-url:http://localhost:5050}")
    private String baseUrl;

    @Value("${openai.edge.tts.response-format:mp3}")
    private String responseFormat;

    @Value("${openai.edge.tts.speed:1.0}")
    private double speed;

    public OpenAiEdgeTextToSpeechService() {
        this.httpClient = HttpClient.newHttpClient();
    }

    @Override
    public FileMetadata synthesizeSpeech(String text, String novelSlug, int chapterNumber) throws IOException {
        validateConfiguration();

        logger.info("Synthesizing speech for novel: {}, chapter: {}", novelSlug, chapterNumber);
        String requestBody = buildRequestBody(text);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(buildSynthesisUrl()))
                .header("Authorization", "Bearer " + apiKey)
                .header("Accept", AUDIO_CONTENT_TYPE)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody, StandardCharsets.UTF_8))
                .build();

        HttpResponse<byte[]> response;
        try {
            response = httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());
            logger.info("Received response from TTS service. Status: {}, Body length: {}", response.statusCode(),
                    response.body() != null ? response.body().length : 0);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Text-to-speech request interrupted", e);
        }

        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            logger.error("Failed to generate speech. Status: {}, Body length: {}", response.statusCode(),
                    response.body() != null ? response.body().length : 0);
            throw new IOException("Text-to-speech provider returned status " + response.statusCode());
        }

        String publicId = String.format("novels/%s/audios/chap-%d-audio", novelSlug, chapterNumber);
        return fileService.uploadFileWithPublicId(response.body(), publicId,"chap-"+ chapterNumber+"-audio", AUDIO_CONTENT_TYPE,"mp3");
    }

    private void validateConfiguration() {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("OpenAI Edge TTS API key is not configured.");
        }
    }

    private String buildSynthesisUrl() {
        String normalizedBaseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        return normalizedBaseUrl + "/v1/audio/speech";
    }

    private String buildRequestBody(String text) {
        String sanitizedText = text == null ? "" : text.trim();
        String format = (responseFormat == null || responseFormat.isBlank()) ? DEFAULT_RESPONSE_FORMAT : responseFormat;
        double effectiveSpeed = speed <= 0 ? DEFAULT_SPEED : speed;

        return "{" +
                "\"input\":" + escapeJson(sanitizedText) +
                ",\"response_format\":" + escapeJson(format) +
                ",\"speed\":" + effectiveSpeed +
                "}";
    }

    private String escapeJson(String value) {
        StringBuilder builder = new StringBuilder("\"");
        for (char c : value.toCharArray()) {
            switch (c) {
                case '\\':
                    builder.append("\\\\");
                    break;
                case '\"':
                    builder.append("\\\"");
                    break;
                case '\n':
                    builder.append("\\n");
                    break;
                case '\r':
                    builder.append("\\r");
                    break;
                case '\t':
                    builder.append("\\t");
                    break;
                default:
                    if (c < 0x20) {
                        builder.append(String.format("\\u%04x", (int) c));
                    } else {
                        builder.append(c);
                    }
                    break;
            }
        }
        builder.append('"');
        return builder.toString();
    }
}
