package com.novel.vippro.Services;

import com.google.cloud.texttospeech.v1.*;
import com.google.protobuf.ByteString;
import com.novel.vippro.Models.FileMetadata;
import com.google.auth.oauth2.GoogleCredentials;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;

@Service("gcpTTS")
@ConditionalOnProperty(name = "texttospeech.provider", havingValue = "gcp")
public class GoogleTTSService implements TextToSpeechService  {

    @Value("${google.credentials.path}")
    private String googleCredentialsPath;

    @Autowired
    private FileService fileService;

    private static final Logger logger = LoggerFactory.getLogger(GoogleTTSService.class);

    public FileMetadata synthesizeSpeech(String text, String novelSlug, int chapterNumber) throws IOException {
        // Initialize the Text-to-Speech client with default credentials
        try {

            // Load Google credentials from the service account key file
            InputStream credentialsStream = getClass().getResourceAsStream("/" + googleCredentialsPath);
            if (credentialsStream == null) {
                logger.error("Google credentials file not found: " + googleCredentialsPath);
                // log current location
                logger.error("Current location: " + getClass().getProtectionDomain().getCodeSource().getLocation());
                throw new IOException("Google credentials file not found: " + googleCredentialsPath);
            }
            GoogleCredentials credentials = GoogleCredentials.fromStream(credentialsStream);
            TextToSpeechSettings settings = TextToSpeechSettings.newBuilder()
                    .setCredentialsProvider(() -> credentials)
                    .build();
            TextToSpeechClient textToSpeechClient = TextToSpeechClient.create(settings);
            // Set the text input to be synthesized
            SynthesisInput input = SynthesisInput.newBuilder()
                    .setText(text)
                    .build();

            // Build the voice parameters
            VoiceSelectionParams voice = VoiceSelectionParams.newBuilder()
                    .setLanguageCode("vi-VN")
                    .setSsmlGender(SsmlVoiceGender.NEUTRAL)
                    .build();

            // Select the audio file type
            AudioConfig audioConfig = AudioConfig.newBuilder()
                    .setAudioEncoding(AudioEncoding.MP3)
                    .build();

            // Perform the text-to-speech request
            SynthesizeSpeechResponse response = textToSpeechClient.synthesizeSpeech(input, voice, audioConfig);
            ByteString audioContents = response.getAudioContent();

            // Generate a unique public ID for the audio file
            String publicId = String.format("novels/%s/audios/chap-%d-audio", novelSlug, chapterNumber);

            // Upload the audio content to Cloudinary
            return fileService.uploadFile(audioContents.toByteArray(), publicId, "audio/mpeg","mp3");

        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to synthesize speech: " + e.getMessage());
        }
    }
}
