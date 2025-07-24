package com.novel.vippro.Services;

import com.novel.vippro.DTO.Novel.NovelDTO;
import com.novel.vippro.DTO.ReadingHistory.ReadingHistoryDTO;
import com.novel.vippro.DTO.ReadingHistory.ReadingStatsDTO;
import com.novel.vippro.Exception.ResourceNotFoundException;
import com.novel.vippro.Mapper.Mapper;
import com.novel.vippro.Models.Chapter;
import com.novel.vippro.Models.Novel;
import com.novel.vippro.Models.ReadingHistory;
import com.novel.vippro.Models.User;
import com.novel.vippro.Payload.Response.PageResponse;
import com.novel.vippro.Repository.ChapterRepository;
import com.novel.vippro.Repository.NovelRepository;
import com.novel.vippro.Repository.ReadingHistoryRepository;
import com.novel.vippro.Repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ReadingHistoryService {

    @Autowired
    private ReadingHistoryRepository readingHistoryRepository;

    @Autowired
    private NovelRepository novelRepository;

    @Autowired
    private ChapterRepository chapterRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private Mapper mapper;

    @Transactional
    public ReadingHistoryDTO updateReadingProgress(UUID novelId, UUID chapterId, Integer progress,
            Integer readingTime) {
        UUID userId = userService.getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        Novel novel = novelRepository.findById(novelId)
                .orElseThrow(() -> new ResourceNotFoundException("Novel", "id", novelId));

        Chapter chapter = chapterRepository.findById(chapterId)
                .orElseThrow(() -> new ResourceNotFoundException("Chapter", "id", chapterId));

        ReadingHistory history = readingHistoryRepository
                .findByUserIdAndNovelIdAndChapterId(userId, novelId, chapterId)
                .orElse(new ReadingHistory());

        history.setUser(user);
        history.setNovel(novel);
        history.setChapter(chapter);
        history.setProgress(progress);

        // Add new reading time to existing
        if (history.getReadingTime() == null) {
            history.setReadingTime(readingTime);
        } else {
            history.setReadingTime(history.getReadingTime() + readingTime);
        }

        history.setLastReadAt(LocalDateTime.now());

        ReadingHistory savedHistory = readingHistoryRepository.save(history);
        return mapper.ReadingHistorytoDTO(savedHistory);
    }

    public PageResponse<ReadingHistoryDTO> getUserReadingHistory(Pageable pageable) {
        UUID userId = userService.getCurrentUserId();
        Page<ReadingHistory> historyPage = readingHistoryRepository
                .findByUserIdOrderByLastReadAtDesc(userId, pageable);
        return new PageResponse<>(
                historyPage.map(mapper::ReadingHistorytoDTO));
    }

    public PageResponse<ReadingHistoryDTO> getNovelReadingHistory(UUID novelId, Pageable pageable) {
        if (!novelRepository.existsById(novelId)) {
            throw new ResourceNotFoundException("Novel", "id", novelId);
        }
        Page<ReadingHistory> historyPage = readingHistoryRepository
                .findByNovelIdOrderByLastReadAtDesc(novelId, pageable);
        return new PageResponse<>(
                historyPage.map(mapper::ReadingHistorytoDTO));
    }

    @Transactional
    public ReadingHistoryDTO addReadingHistory(UUID chapterId) {
        UUID userId = userService.getCurrentUserId();

        Chapter chapter = chapterRepository.findById(chapterId)
                .orElseThrow(() -> new ResourceNotFoundException("Chapter", "id", chapterId));

        Novel novel = chapter.getNovel();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        ReadingHistory history = new ReadingHistory();
        history.setUser(user);
        history.setNovel(novel);
        history.setChapter(chapter);
        history.setProgress(0); // Initial progress

        history.setReadingTime(0); // Initial reading time
        history.setLastReadAt(LocalDateTime.now());
        ReadingHistory savedHistory = readingHistoryRepository.save(history);
        return mapper.ReadingHistorytoDTO(savedHistory);
    }

    public ReadingHistoryDTO getLastReadChapter(UUID novelId) {
        UUID userId = userService.getCurrentUserId();
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
        UUID userId = userService.getCurrentUserId();
        readingHistoryRepository.deleteByUserId(userId);
    }

    public List<NovelDTO> getRecentlyRead(int limit) {
        UUID userId = userService.getCurrentUserId();
        return readingHistoryRepository.findRecentlyReadNovels(userId, PageRequest.of(0, limit))
                .stream()
                .map(history -> mapper.NoveltoDTO(history.getNovel()))
                .collect(Collectors.toList());
    }

    public ReadingStatsDTO getReadingStats() {
        UUID userId = userService.getCurrentUserId();
        ReadingStatsDTO stats = new ReadingStatsDTO();

        // Get total chapters and novels read
        stats.setTotalChaptersRead(readingHistoryRepository.countTotalChaptersRead(userId));
        stats.setTotalNovelsRead(readingHistoryRepository.countTotalNovelsRead(userId));

        // Calculate reading time and average
        List<ReadingHistory> history = readingHistoryRepository
                .findByUserIdOrderByLastReadAtDesc(userId, Pageable.unpaged())
                .getContent();

        long totalMinutes = 0;
        LocalDateTime lastReadTime = null;

        for (ReadingHistory entry : history) {
            if (lastReadTime == null) {
                lastReadTime = entry.getLastReadAt();
                stats.setLastReadAt(lastReadTime);
            }
            // Assume average reading time of 5 minutes per chapter if not specified
            totalMinutes += 5;
        }

        stats.setMinutesSpentReading(totalMinutes);
        stats.setAverageReadingTimePerChapter(history.isEmpty() ? 0
                : (double) totalMinutes / history.size());

        // Get most read genre
        List<Object[]> genres = readingHistoryRepository.findMostReadGenre(userId);
        if (!genres.isEmpty()) {
            stats.setMostReadGenre((String) genres.get(0)[0]);
        }

        // Get favorite author
        List<Object[]> authors = readingHistoryRepository.findFavoriteAuthor(userId);
        if (!authors.isEmpty()) {
            stats.setFavoriteAuthor((String) authors.get(0)[0]);
        }

        // Get currently reading novel
        readingHistoryRepository.findFirstByUserIdAndNovelIdOrderByLastReadAtDesc(userId, null)
                .ifPresent(current -> stats.setCurrentlyReading(current.getNovel().getTitle()));

        // Calculate completion statistics
        long completedCount = history.stream()
                .filter(h -> h.getProgress() != null && h.getProgress() >= 100)
                .map(h -> h.getNovel().getId())
                .distinct()
                .count();

        stats.setCompletedNovels((int) completedCount);
        stats.setCompletionRate(stats.getTotalNovelsRead() == 0 ? 0
                : (double) completedCount / stats.getTotalNovelsRead() * 100);

        return stats;
    }

    public ReadingStatsDTO getUserReadingStats() {
        UUID userId = userService.getCurrentUserId();
        ReadingStatsDTO stats = new ReadingStatsDTO();

        // Calculate basic stats
        stats.setTotalChaptersRead((int) readingHistoryRepository.countTotalChaptersRead(userId));
        stats.setTotalNovelsRead((int) readingHistoryRepository.countTotalNovelsRead(userId));

        // Get most read genre
        List<Object[]> genres = readingHistoryRepository.findMostReadGenre(userId);
        if (!genres.isEmpty()) {
            stats.setMostReadGenre((String) genres.get(0)[0]);
        }

        // Get favorite author
        List<Object[]> authors = readingHistoryRepository.findFavoriteAuthor(userId);
        if (!authors.isEmpty()) {
            stats.setFavoriteAuthor((String) authors.get(0)[0]);
        }

        // Get currently reading novel
        readingHistoryRepository.findByUserIdOrderByLastReadAtDesc(userId, Pageable.ofSize(1))
                .getContent()
                .stream()
                .findFirst()
                .ifPresent(history -> {
                    stats.setCurrentlyReading(history.getNovel().getTitle());
                    stats.setLastReadAt(history.getLastReadAt());
                });

        return stats;
    }

    @Transactional
    public ReadingHistoryDTO updateReadingProgress(UUID chapterId, Integer progress) {
        UUID userId = userService.getCurrentUserId();

        Chapter chapter = chapterRepository.findById(chapterId)
                .orElseThrow(() -> new ResourceNotFoundException("Chapter", "id", chapterId));

        Novel novel = chapter.getNovel();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        ReadingHistory history = new ReadingHistory();
        history.setUser(user);
        history.setNovel(novel);
        history.setChapter(chapter);
        history.setProgress(progress);

        return mapper.ReadingHistorytoDTO(readingHistoryRepository.save(history));
    }
}