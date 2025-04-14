package com.novel.vippro.models;

import jakarta.persistence.*;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import java.util.*;
import java.text.Normalizer;
import java.time.LocalDateTime;

@Entity
@Table(name = "novels")
@Data
public class Novel {
        @Id
        @GeneratedValue(strategy = GenerationType.AUTO)
        private UUID id;

        @Column(nullable = false)
        private String title;

        @Column(nullable = false)
        private String titleNomalized;

        @Column(nullable = false, unique = true)
        private String slug;

        @Column(columnDefinition = "TEXT")
        private String description;

        @Column(nullable = false)
        private String author;

        @Column(nullable = false)
        private String coverImage;

        @Column(nullable = false)
        private String status; // ongoing, completed

        @ManyToMany
        @JoinTable(name = "novel_categories", joinColumns = @JoinColumn(name = "novel_id"), inverseJoinColumns = @JoinColumn(name = "category_id"))
        private Set<Category> categories = new HashSet<>();

        @Column(nullable = false)
        private Integer totalChapters;

        @Column(nullable = false)
        private Integer views;

        @Column(nullable = false)
        private Integer rating;

        @OneToMany(mappedBy = "novel", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
        @JsonManagedReference("novel-chapters")
        private List<Chapter> chapters;

        @OneToMany(mappedBy = "novel", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
        @JsonManagedReference("novel-comments")
        private List<Comment> comments;

        @Column(nullable = false)
        private LocalDateTime createdAt = LocalDateTime.now();

        @Column(nullable = false)
        private LocalDateTime updatedAt = LocalDateTime.now();

        public void addCategory(Category category) {
                this.categories.add(category);
                category.getNovels().add(this);
        }

        public void removeCategory(Category category) {
                this.categories.remove(category);
                category.getNovels().remove(this);
        }

        @PrePersist
        public void onCreate() {
                this.titleNomalized = Normalizer.normalize(this.title, Normalizer.Form.NFD)
                                .replaceAll("\\p{M}", "") // Remove accents
                                .toUpperCase();
                this.slug = this.titleNomalized.toLowerCase().replaceAll("[^a-z0-9]+", "-") + "-"
                                + this.id.toString().substring(0, 8);
        }

        @PreUpdate
        public void onUpdate() {
                this.titleNomalized = Normalizer.normalize(this.title, Normalizer.Form.NFD)
                                .replaceAll("\\p{M}", "") // Remove accents
                                .toUpperCase();
                this.slug = this.titleNomalized.toLowerCase().replaceAll("[^a-z0-9]+", "-") + "-"
                                + this.id.toString().substring(0, 8);
                this.updatedAt = LocalDateTime.now();
        }
}