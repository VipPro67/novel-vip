package com.novel.vippro.Services;

import com.novel.vippro.DTO.Gamification.GamificationProfileDTO;
import com.novel.vippro.Models.Achievement;
import com.novel.vippro.Models.CultivationRank;
import com.novel.vippro.Models.User;
import com.novel.vippro.Models.UserAchievement;
import com.novel.vippro.Repository.AchievementRepository;
import com.novel.vippro.Repository.CultivationRankRepository;
import com.novel.vippro.Repository.UserAchievementRepository;
import com.novel.vippro.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GamificationService {

    private final UserRepository userRepository;
    private final CultivationRankRepository rankRepository;
    private final AchievementRepository achievementRepository;
    private final UserAchievementRepository userAchievementRepository;

    @Transactional
    public void addPoints(UUID userId, int points) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setCultivationPoints(user.getCultivationPoints() + points);
        
        // Check for Rank Up
        updateRank(user);
        
        userRepository.save(user);
    }

    private void updateRank(User user) {
        Optional<CultivationRank> appropriateRank = rankRepository
                .findTopByRequiredPointsLessThanEqualOrderByRankLevelDesc(user.getCultivationPoints());

        appropriateRank.ifPresent(rank -> {
            if (user.getCurrentRank() == null || rank.getRankLevel() > user.getCurrentRank().getRankLevel()) {
                user.setCurrentRank(rank);
                // Trigger Rank Up Notification/Event here if needed
            }
        });
    }

    @Transactional(readOnly = true)
    public GamificationProfileDTO getUserGamificationProfile(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        GamificationProfileDTO dto = new GamificationProfileDTO();
        dto.setPoints(user.getCultivationPoints());
        
        if (user.getCurrentRank() != null) {
            dto.setRankName(user.getCurrentRank().getName());
            dto.setRankLevel(user.getCurrentRank().getRankLevel());
            dto.setRankStyle(user.getCurrentRank().getStyleEffect()); // Simplified mapping
            
            // Calculate next rank
            // For now, simple logic: find next rank by level + 1
            // Real implementation might want a direct query
        } else {
             dto.setRankName("Phàm Nhân");
             dto.setRankLevel(0);
        }

        List<UserAchievement> userAchievements = userAchievementRepository.findByUser(user);
        
        dto.setAchievements(userAchievements.stream().map(this::toAchievementDTO).collect(Collectors.toList()));
        dto.setEquippedAchievements(userAchievements.stream()
                .filter(ua -> Boolean.TRUE.equals(ua.getIsEquipped()))
                .map(this::toAchievementDTO)
                .collect(Collectors.toList()));

        return dto;
    }
    
    @Transactional
    public void unlockAchievement(UUID userId, String achievementCode) {
         User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
         
         if (userAchievementRepository.findByUserAndAchievementCode(user, achievementCode).isPresent()) {
             return; // Already unlocked
         }
         
         Achievement achievement = achievementRepository.findByCode(achievementCode)
                 .orElseThrow(() -> new RuntimeException("Achievement not found"));
                 
         UserAchievement ua = new UserAchievement();
         ua.setUser(user);
         ua.setAchievement(achievement);
         ua.setObtainedAt(Instant.now());
         ua.setIsEquipped(false);
         
         userAchievementRepository.save(ua);
    }
    
    private GamificationProfileDTO.AchievementDTO toAchievementDTO(UserAchievement ua) {
        GamificationProfileDTO.AchievementDTO dto = new GamificationProfileDTO.AchievementDTO();
        dto.setCode(ua.getAchievement().getCode());
        dto.setName(ua.getAchievement().getName());
        dto.setDescription(ua.getAchievement().getDescription());
        dto.setIconUrl(ua.getAchievement().getIconUrl());
        dto.setIsEquipped(ua.getIsEquipped());
        return dto;
    }
}
