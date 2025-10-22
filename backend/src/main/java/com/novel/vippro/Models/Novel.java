package com.novel.vippro.Models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import java.util.*;

import org.hibernate.annotations.BatchSize;
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

	@BatchSize(size = 20)
	@ManyToMany(fetch = FetchType.LAZY)
	@JoinTable(name = "novel_categories", joinColumns = @JoinColumn(name = "novel_id"), inverseJoinColumns = @JoinColumn(name = "category_id"))
	private List<Category> categories;

	@BatchSize(size = 20)
	@ManyToMany(fetch = FetchType.LAZY)
	@JoinTable(name = "novel_tags", joinColumns = @JoinColumn(name = "novel_id"), inverseJoinColumns = @JoinColumn(name = "tag_id"))
	private List<Tag> tags;

	@BatchSize(size = 20)
	@ManyToMany(fetch = FetchType.LAZY)
	@JoinTable(name = "novel_genres", joinColumns = @JoinColumn(name = "novel_id"), inverseJoinColumns = @JoinColumn(name = "genre_id"))
	private List<Genre> genres;

	@BatchSize(size = 20)
	@ManyToOne(fetch = FetchType.LAZY)
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
}