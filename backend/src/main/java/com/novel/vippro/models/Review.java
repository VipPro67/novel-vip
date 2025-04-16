package com.novel.vippro.models;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "reviews")
@Data
@NoArgsConstructor
public class Review {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "novel_id", nullable = false)
    private Novel novel;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false)
    private int rating;

    @Column(name = "is_verified_purchase")
    private boolean isVerifiedPurchase = false;

    @Column(name = "helpful_votes")
    private int helpfulVotes = 0;

    @Column(name = "unhelpful_votes")
    private int unhelpfulVotes = 0;

    @Column(name = "is_edited")
    private boolean isEdited = false;

    @Column(name = "created_at")
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    @UpdateTimestamp
    private LocalDateTime updatedAt;
}