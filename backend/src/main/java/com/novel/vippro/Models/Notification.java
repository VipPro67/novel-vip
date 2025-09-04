package com.novel.vippro.Models;

import jakarta.persistence.*;
import lombok.Data;
<<<<<<< Updated upstream
import java.time.Instant;
=======

>>>>>>> Stashed changes
import java.util.UUID;

import com.novel.vippro.Models.base.BaseEntity;

@Entity
@Table(name = "notifications", indexes = {
        @Index(name = "idx_user_id", columnList = "user_id"),
        @Index(name = "idx_title", columnList = "title")
})
@Data
public class Notification extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String message;

    @Column(nullable = false)
    private boolean read = false;

<<<<<<< Updated upstream
    @CreationTimestamp
    private Instant createdAt;

=======
>>>>>>> Stashed changes
    @Column(name = "notification_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private NotificationType type;

    @Column
    private UUID referenceId; 

<<<<<<< Updated upstream
    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
    }
=======
>>>>>>> Stashed changes
}