package com.novel.vippro.models;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "reports", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "reporter_id", "novel_id", "chapter_id", "comment_id" })
}, indexes = {
        @Index(name = "idx_reporter_id", columnList = "reporter_id"),
        @Index(name = "idx_novel_id", columnList = "novel_id"),
        @Index(name = "idx_chapter_id", columnList = "chapter_id"),
        @Index(name = "idx_comment_id", columnList = "comment_id")
})

@Data
public class Report {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reporter_id", nullable = false)
    private User reporter;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "novel_id")
    private Novel novel;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chapter_id")
    private Chapter chapter;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comment_id")
    private Comment comment;

    @Column(nullable = false)
    private String reason;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ReportStatus status = ReportStatus.PENDING;

    private String adminResponse;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private LocalDateTime resolvedAt;

    @PrePersist
    public void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public enum ReportStatus {
        PENDING,
        RESOLVED,
        REJECTED
    }
}