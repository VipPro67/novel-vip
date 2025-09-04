package com.novel.vippro.Models;

import jakarta.persistence.*;
import lombok.Data;
<<<<<<< Updated upstream
import java.time.Instant;
import java.util.UUID;
=======
import com.novel.vippro.Models.base.BaseEntity;
>>>>>>> Stashed changes

@Entity
@Table(name = "bookmarks", indexes = {
        @Index(name = "idx_user_id", columnList = "user_id"),
        @Index(name = "idx_chapter_id", columnList = "chapter_id"),
        @Index(name = "idx_novel_id", columnList = "novel_id")
})
@Data
public class Bookmark extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chapter_id", nullable = false)
    private Chapter chapter;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "novel_id", nullable = false)
    private Novel novel;

    private String note;

<<<<<<< Updated upstream
    private Integer progress; // Reading progress in percentage

    @Column(nullable = false)
     private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    @PrePersist
    public void onCreate() {
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = Instant.now();
    }
=======
    private Integer progress; 
>>>>>>> Stashed changes
}