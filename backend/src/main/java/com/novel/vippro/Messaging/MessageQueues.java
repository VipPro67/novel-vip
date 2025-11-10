package com.novel.vippro.Messaging;

/**
 * Centralizes queue names so RabbitMQ and ActiveMQ stay aligned.
 */
public final class MessageQueues {

    public static final String NOTIFICATION = "notifications";
    public static final String COMMENT = "comments";
    public static final String EPUB_UPLOAD = "epub.upload.queue";
    public static final String CHAPTER_AUDIO = "chapter.audio.queue";
    public static final String EMAIL_VERIFICATION = "email.verification.queue";

    private MessageQueues() {
    }
}
