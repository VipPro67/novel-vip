package com.novel.vippro.Services;

import java.io.IOException;

/**
 * Abstraction for generating audio from plain text.
 */
public interface TextToSpeechService {

    String synthesizeSpeech(String text, String novelSlug, int chapterNumber) throws IOException;
}
