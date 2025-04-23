package com.novel.vippro.models;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.*;
import lombok.Data;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "categories")
@Data
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false, unique = true)
    private String slug;

    @Column(columnDefinition = "TEXT")
    private String description;

    @ManyToMany(mappedBy = "categories")
    @JsonIgnore
    private Set<Novel> novels = new HashSet<>();

    public void addNovel(Novel novel) {
        if (this.novels == null) {
            this.novels = new HashSet<>();
        }
        this.novels.add(novel);
    }

    public void removeNovel(Novel novel) {
        if (this.novels != null) {
            this.novels.remove(novel);
        }
    }

    public void setNovels(Set<Novel> novels) {
        if (this.novels != null) {
            this.novels.clear();
        }
        if (novels != null) {
            novels.forEach(this::addNovel);
        }
    }

    @PrePersist
    public void onCreate() {
        if (this.slug == null || this.slug.isEmpty()) {
            this.slug = this.name.toLowerCase()
                    .replaceAll("\\s+", "-")
                    .replaceAll("[^a-z0-9-]", "");
        }
    }
}
