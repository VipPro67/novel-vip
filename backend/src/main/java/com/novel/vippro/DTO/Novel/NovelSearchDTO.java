package com.novel.vippro.DTO.Novel;

import java.util.stream.Stream;

import lombok.Data;

@Data
public class NovelSearchDTO {

    private String keyword;
    private String title;
    private String author;
    private String category;
    private String genre;
    private String tag;


    public boolean hasFilters() {
        return Stream.of(keyword, title, author, category, genre, tag)
                .anyMatch(value -> value != null && !value.trim().isEmpty());
    }

    public String cacheKey() {
        return Stream.of(keyword, title, author, category, genre, tag)
                .map(value -> value == null ? "" : value.trim())
                .map(value -> value.replace('-', '_'))
                .reduce((left, right) -> left + "-" + right)
                .orElse("");
    }

    public String normalizedKeyword() {
        if (keyword != null && !keyword.trim().isEmpty()) {
            return keyword.trim();
        }
        return null;
    }

    public String normalizedTitle() {
        if (title != null && !title.trim().isEmpty()) {
            return title.trim();
        }
        return null;
    }

    public String normalizedAuthor() {
        if (author != null && !author.trim().isEmpty()) {
            return author.trim();
        }
        return null;
    }

    public String normalizedCategory() {
        if (category != null && !category.trim().isEmpty()) {
            return category.trim();
        }
        return null;
    }

    public String normalizedGenre() {
        if (genre != null && !genre.trim().isEmpty()) {
            return genre.trim();
        }
        return null;
    }

    public NovelSearchDTO cleanedCopy() {
        NovelSearchDTO copy = new NovelSearchDTO();
        copy.setKeyword(normalizedKeyword());
        copy.setTitle(normalizedTitle());
        copy.setAuthor(normalizedAuthor());
        copy.setCategory(normalizedCategory());
        copy.setGenre(normalizedGenre());
        return copy;
    }
}
