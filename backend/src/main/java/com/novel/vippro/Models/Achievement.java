package com.novel.vippro.Models;

import com.novel.vippro.Models.base.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "achievements")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Achievement extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String code; // UNIQUE_CODE

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String iconUrl; // Icon image

    @Enumerated(EnumType.STRING)
    private AchievementCategory category; // READING, CONTRIBUTION, SOCIAL, SPECIAL

    // Rules for auto-unlocking (simplified for now, can be complex JSON or distinct fields)
    @Column(nullable = false)
    private String triggerType; // e.g., "READ_CHAPTER_COUNT", "COMMENT_COUNT"
    
    private Long thresholdValue; // e.g., 100 chapters

    private Boolean isHidden; // Hidden achievement
    
    public enum AchievementCategory {
        READING,
        CONTRIBUTION,
        SOCIAL,
        SPECIAL
    }
}
