package com.novel.vippro.Services.ThirdParty;

import com.google.cloud.texttospeech.v1.*;
import com.novel.vippro.Models.FileMetadata;
import com.novel.vippro.Services.FileService;
import com.novel.vippro.Services.TextToSpeechService;
import com.google.auth.oauth2.GoogleCredentials;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Service("gcpTTS")
@ConditionalOnProperty(name = "texttospeech.provider", havingValue = "gcp")
public class GoogleTTSService implements TextToSpeechService  {

    @Value("${google.credentials.path}")
    private String googleCredentialsPath;

    @Autowired
    private FileService fileService;

    private static final Logger logger = LoggerFactory.getLogger(GoogleTTSService.class);

    public FileMetadata synthesizeSpeech(String text, String novelSlug, int chapterNumber) throws IOException {
        try {
            // Split text into chunks that don't exceed the API limit
            List<String> chunks = chunkText(text);
            logger.info("Chapter text split into {} chunks for synthesis", chunks.size());

            // Create TextToSpeechClient
            InputStream credentialsStream = loadCredentialsStream();
            if (credentialsStream == null) {
                logger.error("Google credentials file not found: " + googleCredentialsPath);
                throw new IOException("Google credentials file not found: " + googleCredentialsPath);
            }
            GoogleCredentials credentials = GoogleCredentials.fromStream(credentialsStream);
            TextToSpeechSettings settings = TextToSpeechSettings.newBuilder()
                    .setCredentialsProvider(() -> credentials)
                    .build();
            TextToSpeechClient textToSpeechClient = TextToSpeechClient.create(settings);

            // Build the voice parameters
            VoiceSelectionParams voice = VoiceSelectionParams.newBuilder()
                    .setLanguageCode("vi-VN")
                    .setSsmlGender(SsmlVoiceGender.NEUTRAL)
                    .build();

            // Select the audio file type
            AudioConfig audioConfig = AudioConfig.newBuilder()
                    .setAudioEncoding(AudioEncoding.MP3)
                    .build();

            // Synthesize each chunk and collect audio data
            List<byte[]> audioChunks = new ArrayList<>();
            for (int i = 0; i < chunks.size(); i++) {
                String chunk = chunks.get(i);
                logger.debug("Synthesizing chunk {}/{}: {} bytes", i + 1, chunks.size(), chunk.getBytes().length);

                // Set the text input to be synthesized
                SynthesisInput input = SynthesisInput.newBuilder()
                        .setText(chunk)
                        .build();

                // Perform the text-to-speech request
                SynthesizeSpeechResponse response = textToSpeechClient.synthesizeSpeech(input, voice, audioConfig);
                audioChunks.add(response.getAudioContent().toByteArray());
            }

            textToSpeechClient.close();

            // Combine all audio chunks into a single MP3 file
            byte[] combinedAudio = combineAudioChunks(audioChunks);

            // Generate a unique public ID for the audio file
            String publicId = String.format("novels/%s/audios/chap-%d-audio", novelSlug, chapterNumber);

            // Upload the audio content to Cloudinary
            return fileService.uploadFile(combinedAudio, publicId, "audio/mpeg", "mp3");

        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to synthesize speech: " + e.getMessage());
        }
    }

    private List<String> chunkText(String text) {
        List<String> chunks = new ArrayList<>();
        String[] sentences = text.split("(?<=[.!?;])\\s+");
        
        StringBuilder currentChunk = new StringBuilder();
        int currentByteCount = 0;
        final int MAX_BYTES = 5000;

        for (String sentence : sentences) {
            int sentenceBytes = sentence.getBytes().length;
            
            // If adding this sentence would exceed limit and we have content, save current chunk
            if (currentByteCount + sentenceBytes > MAX_BYTES && currentChunk.length() > 0) {
                chunks.add(currentChunk.toString());
                currentChunk = new StringBuilder();
                currentByteCount = 0;
            }

            // If a single sentence exceeds limit, force it into its own chunk
            if (sentenceBytes > MAX_BYTES) {
                if (currentChunk.length() > 0) {
                    chunks.add(currentChunk.toString());
                    currentChunk = new StringBuilder();
                    currentByteCount = 0;
                }
                chunks.add(sentence);
                continue;
            }

            // Add sentence to current chunk
            if (currentChunk.length() > 0) {
                currentChunk.append(" ");
                currentByteCount += 1;
            }
            currentChunk.append(sentence);
            currentByteCount += sentenceBytes;
        }

        // Add remaining content
        if (currentChunk.length() > 0) {
            chunks.add(currentChunk.toString());
        }

        return chunks;
    }

    private byte[] combineAudioChunks(List<byte[]> audioChunks) throws IOException {
        if (audioChunks.isEmpty()) {
            throw new IOException("No audio chunks to combine");
        }
        
        if (audioChunks.size() == 1) {
            return audioChunks.get(0);
        }

        // For MP3 files, we need to use a proper MP3 concatenation approach
        // Using Jakarta Commons Compress or similar would be ideal
        // For now, using a simple concatenation with MP3 frame detection
        return concatenateMP3Files(audioChunks);
    }

    private byte[] concatenateMP3Files(List<byte[]> mp3Chunks) throws IOException {
        // Simple concatenation approach for MP3 files
        // This works reasonably well for Google TTS MP3 output which typically has consistent frames
        // For production, consider using libraries like jaudiotagger or mp3agic for proper MP3 merging
        
        List<byte[]> frameData = new ArrayList<>();
        int totalSize = 0;

        for (byte[] chunk : mp3Chunks) {
            // Skip ID3 tags and concatenate MP3 frames
            byte[] data = stripMP3ID3Tags(chunk);
            if (data.length > 0) {
                frameData.add(data);
                totalSize += data.length;
            }
        }

        if (frameData.isEmpty()) {
            throw new IOException("No valid MP3 data found in audio chunks");
        }

        // Combine all frame data
        byte[] result = new byte[totalSize];
        int offset = 0;
        for (byte[] data : frameData) {
            System.arraycopy(data, 0, result, offset, data.length);
            offset += data.length;
        }

        logger.info("Combined {} audio chunks into {} bytes", mp3Chunks.size(), result.length);
        return result;
    }

    private byte[] stripMP3ID3Tags(byte[] mp3Data) {
        // Remove ID3v2 tags from the beginning of MP3 file
        int offset = 0;

        // Check for ID3v2 tag (starts with "ID3")
        if (mp3Data.length > 10 && mp3Data[0] == 'I' && mp3Data[1] == 'D' && mp3Data[2] == '3') {
            // ID3v2 size is in bytes 6-9 (synchsafe integer)
            int size = ((mp3Data[6] & 0x7F) << 21) | ((mp3Data[7] & 0x7F) << 14) 
                    | ((mp3Data[8] & 0x7F) << 7) | (mp3Data[9] & 0x7F);
            offset = 10 + size;
        }

        // Also skip ID3v1 tag if present (last 128 bytes starting with "TAG")
        int length = mp3Data.length;
        if (length > 128 && mp3Data[length - 128] == 'T' && mp3Data[length - 127] == 'A' && mp3Data[length - 126] == 'G') {
            length -= 128;
        }

        // Return the MP3 frame data without tags
        if (offset >= length) {
            return new byte[0];
        }

        byte[] result = new byte[length - offset];
        System.arraycopy(mp3Data, offset, result, 0, result.length);
        return result;
    }

    private InputStream loadCredentialsStream() throws IOException {
        // Try to load from classpath first (for packaged JAR)
        try {
            ClassPathResource resource = new ClassPathResource(googleCredentialsPath);
            if (resource.exists()) {
                logger.info("Loading Google credentials from classpath: " + googleCredentialsPath);
                return resource.getInputStream();
            }
        } catch (Exception e) {
            logger.warn("Could not load credentials from classpath: " + e.getMessage());
        }

        // Try to load from file system (for Docker container or development)
        try {
            if (Files.exists(Paths.get(googleCredentialsPath))) {
                logger.info("Loading Google credentials from file system: " + googleCredentialsPath);
                return Files.newInputStream(Paths.get(googleCredentialsPath));
            }
        } catch (Exception e) {
            logger.warn("Could not load credentials from file system at " + googleCredentialsPath + ": " + e.getMessage());
        }

        // Try to load from /app/config directory (common Docker convention)
        try {
            String dockerPath = "/app/config/" + googleCredentialsPath;
            if (Files.exists(Paths.get(dockerPath))) {
                logger.info("Loading Google credentials from Docker config: " + dockerPath);
                return Files.newInputStream(Paths.get(dockerPath));
            }
        } catch (Exception e) {
            logger.warn("Could not load credentials from Docker config: " + e.getMessage());
        }

        logger.error("Failed to load Google credentials from any location. Checked: classpath, filesystem, and /app/config/");
        return null;
    }
}
