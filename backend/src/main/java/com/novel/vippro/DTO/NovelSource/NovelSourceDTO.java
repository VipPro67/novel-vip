package com.novel.vippro.DTO.NovelSource;

import com.novel.vippro.Models.SyncStatus;
import java.time.Instant;
import java.util.UUID;

public record NovelSourceDTO(
    UUID id,
    UUID novelId,
    String novelTitle,
    String sourceUrl,
    String sourceId,
    String sourcePlatform,
    Boolean enabled,
    Integer lastSyncedChapter,
    Instant lastSyncTime,
    SyncStatus syncStatus,
    Instant nextSyncTime,
    Integer syncIntervalMinutes,
    String errorMessage,
    Integer consecutiveFailures,
    Instant createdAt,
    Instant updatedAt
) {}
