package com.novel.vippro.Models;

import com.novel.vippro.Models.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "cultivation_ranks")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CultivationRank extends BaseEntity {

    @Column(nullable = false, unique = true)
    private Integer rankLevel; // 0 to 10

    @Column(nullable = false)
    private String name; // e.g., "Luyện Khí", "Trúc Cơ"

    @Column(nullable = false)
    private Long requiredPoints; // Points needed to reach this rank

    @Column(length = 50)
    private String styleColor; // CSS color code or class name

    @Column(length = 50)
    private String styleEffect; // e.g., "glow", "fire", "pulse"

    @Column(columnDefinition = "TEXT")
    private String description;
    
    // Icon URL or path if needed
    private String iconUrl;
}
