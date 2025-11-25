package com.novel.vippro.Services;

import com.novel.vippro.DTO.ReadingHistory.ReadingHistoryDTO;
import com.novel.vippro.DTO.ReadingHistory.ReadingStatsDTO;
import com.novel.vippro.Exception.ResourceNotFoundException;
import com.novel.vippro.Mapper.Mapper;
import com.novel.vippro.Models.Novel;
import com.novel.vippro.Models.ReadingHistory;
import com.novel.vippro.Models.User;
import com.novel.vippro.Payload.Response.PageResponse;
import com.novel.vippro.Repository.ChapterRepository;
import com.novel.vippro.Repository.NovelRepository;
import com.novel.vippro.Repository.ReadingHistoryRepository;
import com.novel.vippro.Repository.UserRepository;
import com.novel.vippro.Security.UserDetailsImpl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.novel.vippro.Events.ReadingProgressEvent;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class ReadingHistoryService {

    @Autowired
    private ReadingHistoryRepository readingHistoryRepository;

    @Autowired
    private NovelRepository novelRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private Mapper mapper;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Transactional
    public ReadingHistoryDTO updateReadingProgress(UUID novelId, Integer lastReadChapterIndex) {
        UUID userId = UserDetailsImpl.getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        Novel novel = novelRepository.findById(novelId)
                .orElseThrow(() -> new ResourceNotFoundException("Novel", "id", novelId));

        ReadingHistory history = readingHistoryRepository.findByUserIdAndNovelId(userId, novelId)
        .orElse(new ReadingHistory());

        history.setUser(user);
        history.setNovel(novel);
        history.setLastReadChapterIndex(lastReadChapterIndex);
        history.setUpdatedAt(Instant.now());
        ReadingHistory savedHistory = readingHistoryRepository.save(history);
        ReadingHistoryDTO dto = mapper.ReadingHistorytoDTO(savedHistory);
        eventPublisher.publishEvent(new ReadingProgressEvent(userId, dto));
        return dto;
    }

    @Transactional(readOnly = true)
    public PageResponse<ReadingHistoryDTO> getUserReadingHistory(Pageable pageable) {
        UUID userId = UserDetailsImpl.getCurrentUserId();
        Page<ReadingHistory> historyPage = readingHistoryRepository
                .findByUserIdOrderByLastReadAtDesc(userId, pageable);
        return new PageResponse<>(
                historyPage.map(mapper::ReadingHistorytoDTO));
    }

    @Transactional(readOnly = true)
    public PageResponse<ReadingHistoryDTO> getNovelReadingHistory(UUID novelId, Pageable pageable) {
        if (!novelRepository.existsById(novelId)) {
            throw new ResourceNotFoundException("Novel", "id", novelId);
        }
        Page<ReadingHistory> historyPage = readingHistoryRepository
                .findByNovelIdOrderByLastReadAtDesc(novelId, pageable);
        return new PageResponse<>(
                historyPage.map(mapper::ReadingHistorytoDTO));
    }

    @Transactional(readOnly = true)
    public ReadingHistoryDTO getLastReadChapter(UUID novelId) {
        UUID userId = UserDetailsImpl.getCurrentUserId();
        ReadingHistory history = readingHistoryRepository
                .findFirstByUserIdAndNovelIdOrderByLastReadAtDesc(userId, novelId)
                .orElseThrow(() -> new ResourceNotFoundException("Reading history", "novelId", novelId));
        return mapper.ReadingHistorytoDTO(history);
    }

    @Transactional
    public void deleteReadingHistory(UUID id) {
        if (!readingHistoryRepository.existsById(id)) {
            throw new ResourceNotFoundException("Reading history", "id", id);
        }
        readingHistoryRepository.deleteById(id);
    }

    @Transactional
    public void clearReadingHistory() {
        UUID userId = UserDetailsImpl.getCurrentUserId();
        readingHistoryRepository.deleteByUserId(userId);
    }

    public ReadingStatsDTO getReadingStats() {
        UUID userId = UserDetailsImpl.getCurrentUserId();
        long totalNovelsRead = readingHistoryRepository.countTotalNovelsRead(userId);

        List<ReadingHistory> history = readingHistoryRepository
                .findByUserIdOrderByLastReadAtDesc(userId, Pageable.unpaged())
                .getContent();

        long totalMinutes = 0;
        Instant lastReadTime = null;
        String currentlyReading = null;

        for (ReadingHistory entry : history) {
            if (lastReadTime == null) {
                lastReadTime = entry.getUpdatedAt();
            }
            // Assume average reading time of 5 minutes per chapter if not specified
            totalMinutes += 5;
        }

        double avgPerChapter = history.isEmpty() ? 0 : (double) totalMinutes / history.size();

        // Get most read genre
        List<Object[]> genres = readingHistoryRepository.findMostReadGenre(userId);
        String mostReadGenre = !genres.isEmpty() ? (String) genres.get(0)[0] : null;

        // Get favorite author
        List<Object[]> authors = readingHistoryRepository.findFavoriteAuthor(userId);
        String favoriteAuthor = !authors.isEmpty() ? (String) authors.get(0)[0] : null;

        var currentEntry = readingHistoryRepository.findFirstByUserIdAndNovelIdOrderByLastReadAtDesc(userId, null);
        if (currentEntry.isPresent()) {
            currentlyReading = currentEntry.get().getNovel().getTitle();
        }

        return ReadingStatsDTO.builder()
                .totalNovelsRead(totalNovelsRead)
                .lastReadAt(lastReadTime)
                .minutesSpentReading(totalMinutes)
                .averageReadingTimePerChapter(avgPerChapter)
                .mostReadGenre(mostReadGenre)
                .favoriteAuthor(favoriteAuthor)
                .currentlyReading(currentlyReading)
                .build();
    }

    public ReadingStatsDTO getUserReadingStats() {
        UUID userId = UserDetailsImpl.getCurrentUserId();
        long totalNovels = readingHistoryRepository.countTotalNovelsRead(userId);

        List<Object[]> genres = readingHistoryRepository.findMostReadGenre(userId);
        String mostReadGenre = !genres.isEmpty() ? (String) genres.get(0)[0] : null;

        List<Object[]> authors = readingHistoryRepository.findFavoriteAuthor(userId);
        String favoriteAuthor = !authors.isEmpty() ? (String) authors.get(0)[0] : null;

        String currentlyReading = null;
        Instant lastReadAt = null;

        var latestHistory = readingHistoryRepository.findByUserIdOrderByLastReadAtDesc(userId, Pageable.ofSize(1))
                .getContent()
                .stream()
                .findFirst();
        if (latestHistory.isPresent()) {
            currentlyReading = latestHistory.get().getNovel().getTitle();
            lastReadAt = latestHistory.get().getUpdatedAt();
        }

        return ReadingStatsDTO.builder()
                .totalNovelsRead(totalNovels)
                .mostReadGenre(mostReadGenre)
                .favoriteAuthor(favoriteAuthor)
                .currentlyReading(currentlyReading)
                .lastReadAt(lastReadAt)
                .build();
    }

    @Transactional(readOnly = true)
    public PageResponse<ReadingHistoryDTO> getUserNovelReadingHistory(Pageable pageable) {
        UUID userId = UserDetailsImpl.getCurrentUserId();
        Page<ReadingHistory> historyPage = readingHistoryRepository
                .findByUserIdOrderByLastReadAtDesc(userId, pageable);
        return new PageResponse<>(
                historyPage.map(mapper::ReadingHistorytoDTO));
    }
}