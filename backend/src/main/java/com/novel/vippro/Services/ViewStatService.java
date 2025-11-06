package com.novel.vippro.Services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.novel.vippro.Repository.ChapterRepository;
import com.novel.vippro.Repository.NovelRepository;
import com.novel.vippro.Repository.UserRepository;
import com.novel.vippro.Repository.ViewStatRepository;
import com.novel.vippro.Security.UserDetailsImpl;
import com.novel.vippro.Models.Novel;
import com.novel.vippro.Models.ViewStat;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@Transactional
public class ViewStatService {

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private ViewStatRepository viewStatRepository;
    
    @Autowired
    private NovelRepository novelRepository;

    @Autowired
    private ChapterRepository chapterRepository;

    @Transactional
    public void recordView(UUID novelId, UUID chapterId) {

        UUID userId = UserDetailsImpl.getCurrentUserId();
        LocalDateTime threeMinutesAgo = LocalDateTime.now().minusMinutes(3);

        Long recentViews = 0L;
        if (userId != null) {
            recentViews = viewStatRepository.countRecentViewsByUserAndNovel(novelId, userId, threeMinutesAgo);
        }

        if (recentViews == 0) {
            ViewStat stat = new ViewStat();
            stat.setNovel(novelRepository.getReferenceById(novelId));
            if (chapterId != null) {
                stat.setChapter(chapterRepository.getReferenceById(chapterId));
            }
            if (userId != null) {
                stat.setUser(userRepository.getReferenceById(userId));
            }
            stat.setViewDate(LocalDateTime.now());

            viewStatRepository.save(stat);
            updateNovelViewCounts(novelId);
        }
    }

    @Transactional
    private void updateNovelViewCounts(UUID novelId) {
        Novel novel = novelRepository.findById(novelId).orElseThrow();
        
        // Update total views
        novel.setTotalViews(novel.getTotalViews() + 1);
        
        // Reset monthly/daily if needed
        LocalDateTime now = LocalDateTime.now();
        if (novel.getLastViewReset() == null || 
            !now.toLocalDate().equals(novel.getLastViewReset().toLocalDate())) {
            novel.setDailyViews(1L);
        } else {
            novel.setDailyViews(novel.getDailyViews() + 1);
        }
        
        if (novel.getLastViewReset() == null || 
            now.getMonth() != novel.getLastViewReset().getMonth()) {
            novel.setMonthlyViews(1L);
        } else {
            novel.setMonthlyViews(novel.getMonthlyViews() + 1);
        }
        
        novel.setLastViewReset(now);
        novelRepository.save(novel);
    }

    @Transactional(readOnly = true)
    public Map<String, Long> getNovelViewStats(UUID novelId) {
        Novel novel = novelRepository.findById(novelId).orElseThrow();
        
        Map<String, Long> stats = new HashMap<>();
        stats.put("total", novel.getTotalViews());
        stats.put("monthly", novel.getMonthlyViews());
        stats.put("daily", novel.getDailyViews());
        
        return stats;
    }
}
