package com.novel.vippro.Models;

import com.novel.vippro.Models.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "videos")
@Getter
@Setter
@NoArgsConstructor
public class Video extends BaseEntity {

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, unique = true)
    private String videoUrl;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private VideoPlatform platform;

    @Column(nullable = false)
    private String embedUrl;

    @Column
    private String externalId;

    public enum VideoPlatform {
        YOUTUBE,
        FACEBOOK
    }
}
