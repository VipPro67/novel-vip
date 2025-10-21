package com.novel.vippro.Services;

import java.io.IOException;

import com.novel.vippro.Models.FileMetadata;

/**
 * Abstraction for generating audio from plain text.
 */
public interface TextToSpeechService {

    FileMetadata synthesizeSpeech(String text, String novelSlug, int chapterNumber) throws IOException;
}
