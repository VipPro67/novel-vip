package com.novel.vippro.DTO.Novel;

import java.util.stream.Stream;

import lombok.Builder;

@Builder
public record NovelSearchDTO(
    String keyword,
    String title,
    String author,
    String category,
    String genre,
    String tag
) {
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
        return normalize(keyword);
    }

    public String normalizedTitle() {
        return normalize(title);
    }

    public String normalizedAuthor() {
        return normalize(author);
    }

    public String normalizedCategory() {
        return normalize(category);
    }

    public String normalizedGenre() {
        return normalize(genre);
    }

    public String normalizedTag() {
        return normalize(tag);
    }

    public NovelSearchDTO cleanedCopy() {
        return NovelSearchDTO.builder()
                .keyword(normalizedKeyword())
                .title(normalizedTitle())
                .author(normalizedAuthor())
                .category(normalizedCategory())
                .genre(normalizedGenre())
                .tag(normalizedTag())
                .build();
    }

    private String normalize(String value) {
        if (value != null && !value.trim().isEmpty()) {
            return value.trim();
        }
        return null;
    }
}
