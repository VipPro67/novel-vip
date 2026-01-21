package com.novel.vippro.Models;

import com.novel.vippro.Models.base.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "novel_sources",
    uniqueConstraints = @UniqueConstraint(columnNames = {"novel_id", "source_url"}),
    indexes = {
        @Index(name = "idx_novel_sources_novel_id", columnList = "novel_id"),
        @Index(name = "idx_novel_sources_source_url", columnList = "source_url"),
        @Index(name = "idx_novel_sources_enabled", columnList = "enabled"),
        @Index(name = "idx_novel_sources_next_sync", columnList = "nextSyncTime")
    })
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class NovelSource extends BaseEntity {
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "novel_id", nullable = false)
    private Novel novel;
    
    @Column(nullable = false, length = 2048)
    private String sourceUrl;
    
    @Column(length = 512)
    private String sourceId;
    
    @Column(length = 100, nullable = false)
    private String sourcePlatform = "69shuba";
    
    @Column(nullable = false)
    private Boolean enabled = true;
    
    @Column(name = "last_synced_chapter")
    private Integer lastSyncedChapter;
    
    @Column(name = "last_sync_time")
    private Instant lastSyncTime;
    
    @Column(name = "sync_status")
    @Enumerated(EnumType.STRING)
    private SyncStatus syncStatus = SyncStatus.IDLE;
    
    @Column(name = "next_sync_time")
    private Instant nextSyncTime;
    
    @Column(name = "sync_interval_minutes")
    private Integer syncIntervalMinutes = 60;
    
    @Column(name = "error_message", length = 2048)
    private String errorMessage;
    
    @Column(name = "consecutive_failures")
    private Integer consecutiveFailures = 0;
}
