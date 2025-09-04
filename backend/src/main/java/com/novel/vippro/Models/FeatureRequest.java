package com.novel.vippro.Models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
<<<<<<< Updated upstream
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
=======
>>>>>>> Stashed changes
import java.util.HashSet;
import java.util.Set;
import com.novel.vippro.Models.base.BaseEntity;

@Entity
@Table(name = "feature_requests", indexes = {
        @Index(name = "idx_user_id", columnList = "user_id"),
        @Index(name = "idx_status", columnList = "status")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FeatureRequest extends BaseEntity {
    @Column(nullable = false)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private FeatureRequestStatus status = FeatureRequestStatus.VOTING;

    @Column(nullable = false)
    private Integer voteCount = 0;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "feature_request_votes", joinColumns = @JoinColumn(name = "feature_request_id"), inverseJoinColumns = @JoinColumn(name = "user_id"))
    private Set<User> voters = new HashSet<>();

<<<<<<< Updated upstream
    @CreationTimestamp
     private Instant createdAt;

    @UpdateTimestamp
    private Instant updatedAt;

=======
>>>>>>> Stashed changes
    public enum FeatureRequestStatus {
        VOTING,
        PROCESSING,
        DONE,
        REJECTED
    }

<<<<<<< Updated upstream
    // set timestamp for createdAt and updatedAt
    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        updatedAt = Instant.now();
    }
=======
>>>>>>> Stashed changes
}