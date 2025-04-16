package com.novel.vippro.models;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "user_preferences")
@Data
@NoArgsConstructor
public class UserPreferences {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ElementCollection
    @CollectionTable(name = "user_favorite_genres")
    @Column(name = "genre")
    private Set<String> favoriteGenres;

    @ElementCollection
    @CollectionTable(name = "user_favorite_tags")
    @Column(name = "tag")
    private Set<String> favoriteTags;

    @Column(name = "preferred_language")
    private String preferredLanguage;

    @Column(name = "preferred_status")
    private String preferredStatus;

    @Column(name = "min_rating")
    private Double minRating;

    @Column(name = "max_chapters")
    private Integer maxChapters;
}