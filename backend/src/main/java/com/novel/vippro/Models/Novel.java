package com.novel.vippro.Models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

import java.time.LocalDateTime;
import java.util.*;
import com.novel.vippro.Models.base.BaseEntity;

@Entity
@Table(name = "novels")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor

@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class Novel extends BaseEntity {

	@Column(nullable = false)
	private String title;

	@Column(nullable = false)
	private String titleNormalized;

	@Column(nullable = false, unique = true)
	private String slug;

	@Column(columnDefinition = "TEXT")
	private String description;

	@Column(nullable = false)
	private String author;

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	@JoinColumn(name = "cover_image_id", referencedColumnName = "id")
	private FileMetadata coverImage;

	@Column(nullable = false)
	private String status; // ongoing, completed

	@ManyToMany(fetch = FetchType.LAZY)
	@JoinTable(name = "novel_categories", joinColumns = @JoinColumn(name = "novel_id"), inverseJoinColumns = @JoinColumn(name = "category_id"))
	private Set<Category> categories = new HashSet<>();

	@ManyToMany(fetch = FetchType.LAZY)
	@JoinTable(name = "novel_tags", joinColumns = @JoinColumn(name = "novel_id"), inverseJoinColumns = @JoinColumn(name = "tag_id"))
	private Set<Tag> tags = new HashSet<>();

	@ManyToMany(fetch = FetchType.LAZY)
	@JoinTable(name = "novel_genres", joinColumns = @JoinColumn(name = "novel_id"), inverseJoinColumns = @JoinColumn(name = "genre_id"))
	private Set<Genre> genres = new HashSet<>();

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "owner_id", referencedColumnName = "id")
	private User owner;

	@Column(nullable = false)
	private boolean isPublic = false;

	@Column(nullable = false)
	private Integer totalChapters;

	@Column(nullable = false)
	private Integer rating;

	@OneToMany(mappedBy = "novel", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	@JsonManagedReference("novel-chapters")
	private List<Chapter> chapters = new ArrayList<>();

	@OneToMany(mappedBy = "novel", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	@JsonManagedReference("novel-comments")
	private List<Comment> comments = new ArrayList<>();

	@OneToMany(mappedBy = "novel", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	@JsonManagedReference("novel-corrections")
	private List<CorrectionRequest> corrections = new ArrayList<>();

	@OneToMany(mappedBy = "novel", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	private List<Rating> ratings = new ArrayList<>();

	@OneToMany(mappedBy = "novel", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	private List<Review> reviews = new ArrayList<>();

	@OneToMany(mappedBy = "novel", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	private List<Favorite> favorites = new ArrayList<>();

	@OneToMany(mappedBy = "novel", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	private List<Bookmark> bookmarks = new ArrayList<>();

	@OneToMany(mappedBy = "novel", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	private List<ReadingHistory> readingHistories = new ArrayList<>();

	@OneToMany(mappedBy = "novel", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	private List<ViewStat> viewStats = new ArrayList<>();

	@OneToMany(mappedBy = "novel", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	private List<NovelSource> novelSources = new ArrayList<>();

	@OneToMany(mappedBy = "novel", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	private List<Report> reports = new ArrayList<>();

	@Column(name = "total_views", nullable = true)
	private Long totalViews = 0L;

	@Column(name = "monthly_views", nullable = true)
	private Long monthlyViews = 0L;

	@Column(name = "daily_views", nullable = true)
	private Long dailyViews = 0L;

	@Column(name = "last_view_reset")
	private LocalDateTime lastViewReset;
}
