package com.novel.vippro.Models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;
import com.novel.vippro.Models.base.BaseEntity;

@Entity
@Table(name = "feature_requests")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor

public class FeatureRequest extends BaseEntity {
    @Column(nullable = false)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private FeatureRequestStatus status = FeatureRequestStatus.VOTING;

    @Column(nullable = false)
    private User requester;

    @Column(nullable = false)
    private Integer voteCount = 0;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "feature_request_votes", joinColumns = @JoinColumn(name = "feature_request_id"), inverseJoinColumns = @JoinColumn(name = "user_id"))
    private Set<User> voters = new HashSet<>();
    public enum FeatureRequestStatus {
        VOTING,
        PROCESSING,
        DONE,
        REJECTED
    }
}