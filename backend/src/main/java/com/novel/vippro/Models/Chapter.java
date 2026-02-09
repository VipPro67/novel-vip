package com.novel.vippro.Models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import java.util.List;
import com.novel.vippro.Models.base.BaseEntity;

@Entity
@Table(name = "chapters", uniqueConstraints = @UniqueConstraint(columnNames = { "chapterNumber",
        "novel_id" }))
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor

@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class Chapter extends BaseEntity {

    @Column(nullable = false)
    private Integer chapterNumber;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private Integer views = 0;

    @Column(nullable = false)
    private Integer price = 0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "novel_id", nullable = false)
    @JsonBackReference("novel-chapters")
    private Novel novel;

    @OneToMany(mappedBy = "chapter", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference("chapter-comments")
    private List<Comment> comments;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "json_file_id", referencedColumnName = "id")
    private FileMetadata jsonFile;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "audio_file_id", referencedColumnName = "id")
    private FileMetadata audioFile;

    @OneToMany(mappedBy = "chapter", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference("chapter-corrections")
    private List<CorrectionRequest> corrections;
}