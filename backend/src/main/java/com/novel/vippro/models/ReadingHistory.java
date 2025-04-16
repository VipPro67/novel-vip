package com.novel.vippro.models;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "reading_history")
public class ReadingHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

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
    private LocalDateTime lastReadAt;

    @Column(name = "created_at")
    @CreationTimestamp
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (this.progress == null) {
            this.progress = 0;
        }
        if (this.readingTime == null) {
            this.readingTime = 0;
        }
    }
}