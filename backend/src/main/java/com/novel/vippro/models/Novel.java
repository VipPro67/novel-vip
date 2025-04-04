package com.novel.vippro.models;

import jakarta.persistence.*;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import java.util.List;

@Entity
@Table(name = "novels")
@Data
public class Novel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private String author;

    @Column(nullable = false)
    private String coverImage;

    @Column(nullable = false)
    private String status; // ongoing, completed

    @ElementCollection
    @CollectionTable(name = "novel_categories", joinColumns = @JoinColumn(name = "novel_id"))
    @Column(name = "category")
    private List<String> categories;

    @Column(nullable = false)
    private Integer totalChapters;

    @Column(nullable = false)
    private Integer views;

    @Column(nullable = false)
    private Integer rating;

    @OneToMany(mappedBy = "novel", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference
    private List<Chapter> chapters;
}