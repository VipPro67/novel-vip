package com.novel.vippro.dto;

import lombok.Data;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;

@Data
public class ReadingStatsDTO {
    private long totalNovelsRead;
    private long totalChaptersRead;
    private Duration totalReadingTime;
    private Map<String, Long> novelsByCategory;
    private Map<String, Long> chaptersByNovel;
    private Map<String, Duration> readingTimeByNovel;
    private Map<String, Long> readingFrequencyByDayOfWeek;
    private Map<String, Long> readingFrequencyByHourOfDay;

    // Additional fields needed by the service
    private LocalDateTime lastReadAt;
    private long minutesSpentReading;
    private double averageReadingTimePerChapter;
    private String mostReadGenre;
    private String favoriteAuthor;
    private String currentlyReading;
    private int completedNovels;
    private double completionRate;
}