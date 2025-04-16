package com.novel.vippro.services;

import com.google.cloud.texttospeech.v1.*;
import com.google.protobuf.ByteString;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class TextToSpeechService {

    @Autowired
    private CloudinaryService cloudinaryService;

    public String synthesizeSpeech(String text, String novelSlug, int chapterNumber) throws IOException {
        // Initialize the Text-to-Speech client
        try (TextToSpeechClient textToSpeechClient = TextToSpeechClient.create()) {
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
            String publicId = String.format("chapters/%s/%d-audio", novelSlug, chapterNumber);

            // Upload the audio content to Cloudinary
            return cloudinaryService.uploadFile(audioContents.toByteArray(), publicId, "mp3");
        }
    }
}