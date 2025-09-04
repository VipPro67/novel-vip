package com.novel.vippro.Models;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

import org.hibernate.annotations.UpdateTimestamp;

<<<<<<< Updated upstream
import java.time.Instant;
import java.util.UUID;
=======
import com.novel.vippro.Models.base.BaseEntity;
>>>>>>> Stashed changes

@Data
@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "reading_history", indexes = {
        @Index(name = "idx_user_id", columnList = "user_id"),
        @Index(name = "idx_novel_id", columnList = "novel_id"),
        @Index(name = "idx_chapter_id", columnList = "chapter_id")
})
public class ReadingHistory extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "novel_id", nullable = false)
    private Novel novel;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chapter_id", nullable = false)
    private Chapter chapter;

    @Column(name = "progress")
    private Integer progress; // Progress percentage in the chapter (0-100)

    @Column(name = "reading_time")
    private Integer readingTime; // Time spent reading in minutes

    @Column(name = "last_read_at")
    @UpdateTimestamp
    private Instant lastReadAt;

<<<<<<< Updated upstream
    @CreationTimestamp
     private Instant createdAt;

    @UpdateTimestamp
    private Instant updatedAt;

    @PrePersist
    protected void onCreate() {
        if (this.progress == null) {
            this.progress = 0;
        }
        if (this.readingTime == null) {
            this.readingTime = 0;
        }
    }
=======
>>>>>>> Stashed changes
}