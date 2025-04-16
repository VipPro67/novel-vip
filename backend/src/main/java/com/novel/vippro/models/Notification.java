package com.novel.vippro.models;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "notifications")
@Data
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String message;

    @Column(nullable = false)
    private boolean read = false;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "notification_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private NotificationType type;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}