package com.novel.vippro.Models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.novel.vippro.Models.base.BaseEntity;

@Entity
@Table(name = "correction_requests")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CorrectionRequest extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonBackReference("user-corrections")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "novel_id", nullable = false)
    @JsonBackReference("novel-corrections")
    private Novel novel;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chapter_id", nullable = false)
    @JsonBackReference("chapter-corrections")
    private Chapter chapter;

    @Column(nullable = false)
    private String s3Key;

    @Column(nullable = true)
    private Integer paragraphIndex;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String originalText;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String suggestedText;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CorrectionStatus status = CorrectionStatus.PENDING;

    @Column(columnDefinition = "TEXT")
    private String rejectionReason;

    public enum CorrectionStatus {
        PENDING,
        APPROVED,
        REJECTED
    }
}
