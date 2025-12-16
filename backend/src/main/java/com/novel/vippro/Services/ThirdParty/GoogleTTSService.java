package com.novel.vippro.Services.ThirdParty;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.texttospeech.v1.*;
import com.novel.vippro.Models.FileMetadata;
import com.novel.vippro.Services.FileService;
import com.novel.vippro.Services.TextToSpeechService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Service("gcpTTS")
@ConditionalOnProperty(name = "texttospeech.provider", havingValue = "gcp")
public class GoogleTTSService implements TextToSpeechService {

    private static final Logger logger = LoggerFactory.getLogger(GoogleTTSService.class);

    @Value("${google.credentials.path}")
    private String googleCredentialsPath;

    @Autowired
    private FileService fileService;

    private TextToSpeechClient textToSpeechClient;

    @PostConstruct
    public void init() {
        try {
            logger.info("Initializing Google TTS Client...");
            InputStream credentialsStream = loadCredentialsStream();
            
            if (credentialsStream == null) {
                logger.error("CRITICAL: Google credentials not found. TTS will not work.");
                return; 
            }

            try (credentialsStream) {
                GoogleCredentials credentials = GoogleCredentials.fromStream(credentialsStream);
                TextToSpeechSettings settings = TextToSpeechSettings.newBuilder()
                        .setCredentialsProvider(() -> credentials)
                        .build();
                this.textToSpeechClient = TextToSpeechClient.create(settings);
                logger.info("Google TTS Client initialized successfully.");
            }
        } catch (IOException e) {
            logger.error("Failed to initialize Google TTS Client", e);
            throw new RuntimeException("Failed to initialize Google TTS Client", e);
        }
    }

    @PreDestroy
    public void destroy() {
        if (textToSpeechClient != null) {
            textToSpeechClient.close();
            logger.info("Google TTS Client closed.");
        }
    }

    @Override
    public FileMetadata synthesizeSpeech(String text, String novelSlug, int chapterNumber) throws IOException {
        if (textToSpeechClient == null) {
            throw new IOException("TextToSpeechClient is not initialized. Check server logs for startup errors.");
        }

        try {
            List<String> chunks = chunkText(text);
            logger.info("Synthesizing Chapter {} ({} chunks)", chapterNumber, chunks.size());

            // Build parameters (Reused for all chunks)
            VoiceSelectionParams voice = VoiceSelectionParams.newBuilder()
                    .setLanguageCode("vi-VN")
                    .setSsmlGender(SsmlVoiceGender.NEUTRAL)
                    .build();

            AudioConfig audioConfig = AudioConfig.newBuilder()
                    .setAudioEncoding(AudioEncoding.MP3)
                    .build();

            List<byte[]> audioChunks = new ArrayList<>();
            for (int i = 0; i < chunks.size(); i++) {
                String chunk = chunks.get(i);
                
                SynthesisInput input = SynthesisInput.newBuilder().setText(chunk).build();
                SynthesizeSpeechResponse response = textToSpeechClient.synthesizeSpeech(input, voice, audioConfig);
                
                audioChunks.add(response.getAudioContent().toByteArray());
                logger.debug("Synthesized chunk {}/{}", i + 1, chunks.size());
            }

            byte[] combinedAudio = combineAudioChunks(audioChunks);

            String publicId = String.format("novels/%s/audios/chap-%d-audio", novelSlug, chapterNumber);
            String filename = String.format("chap-%d-audio.mp3", chapterNumber);

            return fileService.uploadFileWithPublicId(combinedAudio, publicId, filename, "audio/mpeg", "mp3");

        } catch (Exception e) {
            logger.error("Error during speech synthesis for {} chap {}", novelSlug, chapterNumber, e);
            throw new IOException("Failed to synthesize speech: " + e.getMessage(), e);
        }
    }
    private List<String> chunkText(String text) {
        List<String> chunks = new ArrayList<>();
        String[] sentences = text.split("(?<=[.!?;])\\s+");

        StringBuilder currentChunk = new StringBuilder();
        int currentByteSize = 0;
        final int MAX_BYTES = 4800; 

        for (String sentence : sentences) {
            int sentenceBytes = sentence.getBytes(StandardCharsets.UTF_8).length;
            if (currentByteSize + sentenceBytes > MAX_BYTES) {
                if (currentChunk.length() > 0) {
                    chunks.add(currentChunk.toString());
                    currentChunk = new StringBuilder();
                    currentByteSize = 0;
                }
            }
            if (sentenceBytes > MAX_BYTES) {
                if (currentChunk.length() > 0) {
                    chunks.add(currentChunk.toString());
                    currentChunk = new StringBuilder();
                    currentByteSize = 0;
                }
                chunks.add(sentence); 
                continue;
            }
            if (currentChunk.length() > 0) {
                currentChunk.append(" ");
                currentByteSize += 1; 
            }
            currentChunk.append(sentence);
            currentByteSize += sentenceBytes;
        }
        if (currentChunk.length() > 0) {
            chunks.add(currentChunk.toString());
        }
        return chunks;
    }

    private byte[] combineAudioChunks(List<byte[]> mp3Chunks) throws IOException {
        if (mp3Chunks.isEmpty()) return new byte[0];
        if (mp3Chunks.size() == 1) return mp3Chunks.get(0);
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            for (byte[] chunk : mp3Chunks) {
                // Strip tags to prevent "blips" between sentences
                byte[] cleanChunk = stripMP3ID3Tags(chunk);
                if (cleanChunk.length > 0) {
                    outputStream.write(cleanChunk);
                }
            }
            return outputStream.toByteArray();
        }
    }

    private byte[] stripMP3ID3Tags(byte[] mp3Data) {
        if (mp3Data == null || mp3Data.length == 0) return new byte[0];

        int offset = 0;
        int length = mp3Data.length;
        if (length > 10 && mp3Data[0] == 'I' && mp3Data[1] == 'D' && mp3Data[2] == '3') {
            int tagSize = ((mp3Data[6] & 0x7F) << 21) | 
                          ((mp3Data[7] & 0x7F) << 14) | 
                          ((mp3Data[8] & 0x7F) << 7)  | 
                           (mp3Data[9] & 0x7F);
            offset = 10 + tagSize;
        }

        if (length > 128 && mp3Data[length - 128] == 'T' && 
                            mp3Data[length - 127] == 'A' && 
                            mp3Data[length - 126] == 'G') {
            length -= 128;
        }
        if (offset >= length) return new byte[0];

        int audioSize = length - offset;
        byte[] cleanAudio = new byte[audioSize];
        System.arraycopy(mp3Data, offset, cleanAudio, 0, audioSize);

        return cleanAudio;
    }

    private InputStream loadCredentialsStream() {
        try {
            ClassPathResource resource = new ClassPathResource(googleCredentialsPath);
            if (resource.exists()) {
                logger.info("Found credentials in classpath: {}", googleCredentialsPath);
                return resource.getInputStream();
            }
        } catch (Exception ignored) {}

        try {
            if (Files.exists(Paths.get(googleCredentialsPath))) {
                logger.info("Found credentials in filesystem: {}", googleCredentialsPath);
                return Files.newInputStream(Paths.get(googleCredentialsPath));
            }
        } catch (Exception ignored) {}

        try {
            String dockerPath = "/app/config/" + googleCredentialsPath;
            if (Files.exists(Paths.get(dockerPath))) {
                logger.info("Found credentials in Docker config: {}", dockerPath);
                return Files.newInputStream(Paths.get(dockerPath));
            }
        } catch (Exception ignored) {}

        return null;
    }
}