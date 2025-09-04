package com.novel.vippro.Models;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
<<<<<<< Updated upstream
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;
=======
import com.novel.vippro.Models.base.BaseEntity;
>>>>>>> Stashed changes

@Entity
@Table(name = "reviews", indexes = {
        @Index(name = "idx_novel_id", columnList = "novel_id"),
        @Index(name = "idx_user_id", columnList = "user_id")
})
@Data
@NoArgsConstructor
public class Review extends BaseEntity {

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

<<<<<<< Updated upstream
    @CreationTimestamp
     private Instant createdAt;

    @UpdateTimestamp
    private Instant updatedAt;
=======
>>>>>>> Stashed changes
}