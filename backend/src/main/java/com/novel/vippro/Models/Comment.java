package com.novel.vippro.Models;

import jakarta.persistence.*;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonBackReference;
<<<<<<< Updated upstream
import java.time.Instant;
=======
>>>>>>> Stashed changes
import java.util.List;
import com.novel.vippro.Models.base.BaseEntity;

@Entity
@Table(name = "comments", indexes = {
        @Index(name = "idx_user_id", columnList = "user_id"),
        @Index(name = "idx_novel_id", columnList = "novel_id"),
        @Index(name = "idx_chapter_id", columnList = "chapter_id")
})
@Data
public class Comment extends BaseEntity {
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "novel_id")
    @JsonBackReference("novel-comments")
    private Novel novel;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chapter_id")
    private Chapter chapter;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Comment parent;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL)
    private List<Comment> replies;

<<<<<<< Updated upstream
    @CreationTimestamp
    private Instant createdAt;

    @UpdateTimestamp
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
>>>>>>> Stashed changes
}