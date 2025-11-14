package com.novel.vippro.DTO.ReadingHistory;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.datatype.jsr310.deser.InstantDeserializer;
import lombok.Builder;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;

@Builder
public record ReadingStatsDTO(
    long totalNovelsRead,
    long totalChaptersRead,
    Duration totalReadingTime,
    Map<String, Long> novelsByCategory,
    Map<String, Long> chaptersByNovel,
    Map<String, Duration> readingTimeByNovel,
    Map<String, Long> readingFrequencyByDayOfWeek,
    Map<String, Long> readingFrequencyByHourOfDay,
    @JsonProperty("lastReadAt")
    @JsonDeserialize(using = InstantDeserializer.class)
    Instant lastReadAt,
    long minutesSpentReading,
    double averageReadingTimePerChapter,
    String mostReadGenre,
    String favoriteAuthor,
    String currentlyReading,
    int completedNovels,
    double completionRate
) {}