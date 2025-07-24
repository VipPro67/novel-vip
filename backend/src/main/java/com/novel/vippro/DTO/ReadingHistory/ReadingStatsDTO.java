package com.novel.vippro.DTO.ReadingHistory;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;

@Data
@Getter
@Setter
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
    @JsonProperty("lastReadAt")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime lastReadAt;
    private long minutesSpentReading;
    private double averageReadingTimePerChapter;
    private String mostReadGenre;
    private String favoriteAuthor;
    private String currentlyReading;
    private int completedNovels;
    private double completionRate;
}