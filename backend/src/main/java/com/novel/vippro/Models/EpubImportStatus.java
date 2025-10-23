package com.novel.vippro.Models;

public enum EpubImportStatus {
    QUEUED,
    PARSING,
    CHAPTERS_CREATED,
    WAITING_FOR_AUDIO,
    COMPLETED,
    FAILED
}
