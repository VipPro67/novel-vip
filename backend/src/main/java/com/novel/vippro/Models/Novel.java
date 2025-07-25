package com.novel.vippro.Models;

import jakarta.persistence.*;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import java.util.*;
import java.text.Normalizer;
import java.time.LocalDateTime;

@Entity
@Table(name = "novels", indexes = {
		@Index(name = "idx_novel_title", columnList = "title"),
		@Index(name = "idx_novel_slug", columnList = "slug")
})
@Data
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class Novel {
	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

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

	@ManyToMany
	@JoinTable(name = "novel_categories", joinColumns = @JoinColumn(name = "novel_id"), inverseJoinColumns = @JoinColumn(name = "category_id"))
	private Set<Category> categories = new HashSet<>();

	@ManyToMany
	@JoinTable(name = "novel_tags", joinColumns = @JoinColumn(name = "novel_id"), inverseJoinColumns = @JoinColumn(name = "tag_id"))
	private Set<Tag> tags = new HashSet<>();

	@ManyToMany
	@JoinTable(name = "novel_genres", joinColumns = @JoinColumn(name = "novel_id"), inverseJoinColumns = @JoinColumn(name = "genre_id"))
	private Set<Genre> genres = new HashSet<>();

	@ManyToOne
	@JoinColumn(name = "owner_id", referencedColumnName = "id")
	private User owner;

	@Column(nullable = false)
	private boolean isPublic = false;

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
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime createdAt;

	@Column(nullable = false)
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime updatedAt;

	public void addCategory(Category category) {
		if (this.categories == null) {
			this.categories = new HashSet<>();
		}
		this.categories.add(category);
		// Don't call category.addNovel(this) to avoid infinite recursion
	}

	public void removeCategory(Category category) {
		if (this.categories != null) {
			this.categories.remove(category);
		}
		// Don't call category.removeNovel(this) to avoid infinite recursion
	}

	public void setCategories(Set<Category> categories) {
		if (this.categories == null) {
			this.categories = new HashSet<>();
		} else {
			this.categories.clear();
		}
		if (categories != null) {
			this.categories.addAll(categories);
		}
	}

	private void NormalizeFields() {
		if (this.title != null) {
			this.titleNormalized = Normalizer.normalize(this.title, Normalizer.Form.NFD)
					.replaceAll("\\p{M}", "")
					.toUpperCase();
		}
	}
}