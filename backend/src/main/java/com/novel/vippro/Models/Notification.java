package com.novel.vippro.Models;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "notifications", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "user_id", "title" })
}, indexes = {
        @Index(name = "idx_user_id", columnList = "user_id"),
        @Index(name = "idx_title", columnList = "title")
})
@Data
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
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

    @CreationTimestamp
     private LocalDateTime createdAt;

    @Column(name = "notification_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private NotificationType type;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}