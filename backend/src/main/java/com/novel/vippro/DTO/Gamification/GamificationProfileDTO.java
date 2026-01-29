package com.novel.vippro.DTO.Gamification;

import lombok.Data;
import java.util.List;

@Data
public class GamificationProfileDTO {
    private Long points;
    private String rankName;
    private Integer rankLevel;
    private String rankStyle; // e.g. "color: gold; effect: glow"
    private Long nextRankPoints;
    
    private List<AchievementDTO> achievements;
    private List<AchievementDTO> equippedAchievements;

    @Data
    public static class AchievementDTO {
        private String code;
        private String name;
        private String description;
        private String iconUrl;
        private Boolean isEquipped;
    }
}
