package com.novel.vippro.Models;

import com.novel.vippro.Models.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "epub_import_jobs")
@Getter
@Setter
public class SystemJob extends BaseEntity {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SystemJobType jobType = SystemJobType.EPUB_IMPORT;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SystemJobStatus status = SystemJobStatus.QUEUED;

    @Enumerated(EnumType.STRING)
    private EpubImportType importType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "file_metadata_id")
    private FileMetadata importFile;

    @Column(nullable = false)
    private UUID userId;

    private UUID novelId;

    private String slug;

    private String requestedStatus;

    private UUID chapterId;

    private Integer chapterNumber;

    @Column(length = 2048)
    private String statusMessage;

    private int totalChapters;

    private int chaptersProcessed;

    private int audioCompleted;

    private Instant completedAt;

    private String originalFileName;
}
