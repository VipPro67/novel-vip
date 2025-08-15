package com.novel.vippro.Events;

import java.util.UUID;

import com.novel.vippro.DTO.ReadingHistory.ReadingHistoryDTO;

public class ReadingProgressEvent {
    private final UUID userId;
    private final ReadingHistoryDTO progress;

    public ReadingProgressEvent(UUID userId, ReadingHistoryDTO progress) {
        this.userId = userId;
        this.progress = progress;
    }

    public UUID getUserId() {
        return userId;
    }

    public ReadingHistoryDTO getProgress() {
        return progress;
    }
}
