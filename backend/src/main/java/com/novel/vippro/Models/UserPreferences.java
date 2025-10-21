package com.novel.vippro.Models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "user_preferences", uniqueConstraints = @UniqueConstraint(columnNames = "user_id"))
@Getter
@Setter
@AllArgsConstructor
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
    private List<String> favoriteGenres;

    @ElementCollection
    @CollectionTable(name = "user_favorite_tags")
    @Column(name = "tag")
    private List<String> favoriteTags;

    @Column(name = "preferred_language")
    private String preferredLanguage;

    @Column(name = "preferred_status")
    private String preferredStatus;

    @Column(name = "min_rating")
    private Double minRating;

    @Column(name = "max_chapters")
    private Integer maxChapters;
}