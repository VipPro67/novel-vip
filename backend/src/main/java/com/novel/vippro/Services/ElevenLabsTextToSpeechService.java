package com.novel.vippro.Services;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class ElevenLabsTextToSpeechService implements TextToSpeechService {

    private static final Logger logger = LoggerFactory.getLogger(ElevenLabsTextToSpeechService.class);

    private static final String DEFAULT_MODEL = "eleven_monolingual_v1";
    private static final String AUDIO_CONTENT_TYPE = "audio/mpeg";

    private final FileStorageService fileStorageService;
    private final HttpClient httpClient;

    @Value("${elevenlabs.api.key:}")
    private String apiKey;

    @Value("${elevenlabs.voice.id:}")
    private String voiceId;

    @Value("${elevenlabs.model.id:}")
    private String modelId;

    @Value("${elevenlabs.base.url:https://api.elevenlabs.io}")
    private String baseUrl;

    public ElevenLabsTextToSpeechService(FileStorageService fileStorageService) {
        this.fileStorageService = fileStorageService;
        this.httpClient = HttpClient.newHttpClient();
    }

    @Override
    public String synthesizeSpeech(String text, String novelSlug, int chapterNumber) throws IOException {
        validateConfiguration();

        String requestBody = buildRequestBody(text);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(buildSynthesisUrl()))
                .header("Accept", AUDIO_CONTENT_TYPE)
                .header("xi-api-key", apiKey)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody, StandardCharsets.UTF_8))
                .build();

        HttpResponse<byte[]> response;
        try {
            logger.debug("Sending TTS request to ElevenLabs: URL={}, Body={}", request.uri(), requestBody);
            response = httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());
            logger.debug("Received TTS response: Status={}, BodyLength={}", response.statusCode(),
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
        return fileStorageService.uploadFile(response.body(), publicId, AUDIO_CONTENT_TYPE);
    }

    private void validateConfiguration() {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("ElevenLabs API key is not configured.");
        }
        if (voiceId == null || voiceId.isBlank()) {
            throw new IllegalStateException("ElevenLabs voice id is not configured.");
        }
    }

    private String buildSynthesisUrl() {
        String base = (baseUrl == null ? "https://api.elevenlabs.io" : baseUrl).trim();
        base = base.replaceAll("/+$", "");        // drop trailing slashes
        if (base.endsWith("/v1")) base = base.substring(0, base.length() - 3); // drop /v1
        return base + "/v1/text-to-speech/" + voiceId;
    }


    private String buildRequestBody(String text) {
        String sanitizedText = text == null ? "" : text.trim();
        String effectiveModelId = (modelId == null || modelId.isBlank()) ? DEFAULT_MODEL : modelId;
        return "{" +
                "\"text\":" + escapeJson(sanitizedText) +
                ",\"model_id\":\"" + effectiveModelId + "\"" +
                ",\"voice_settings\":{\"stability\":0.5,\"similarity_boost\":0.75}" +
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
