package com.novel.vippro.mapper;

import com.novel.vippro.dto.NovelDTO;
import com.novel.vippro.dto.ChapterDTO;
import com.novel.vippro.models.Novel;
import com.novel.vippro.models.Category;
import com.novel.vippro.models.Chapter;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class NovelMapper {

    public NovelDTO toDTO(Novel novel) {
        NovelDTO dto = new NovelDTO();
        dto.setId(novel.getId());
        dto.setTitle(novel.getTitle());
        dto.setDescription(novel.getDescription());
        dto.setAuthor(novel.getAuthor());
        dto.setCoverImage(novel.getCoverImage());
        dto.setStatus(novel.getStatus());
        dto.setCategories(novel.getCategories().stream()
                .map(Category::getName)
                .collect(Collectors.toList()));
        dto.setTotalChapters(novel.getTotalChapters());
        dto.setViews(novel.getViews());
        dto.setRating(novel.getRating());
        dto.setUpdatedAt(novel.getUpdatedAt());

        if (novel.getChapters() != null) {
            List<ChapterDTO> chapterDTOs = novel.getChapters().stream()
                    .map(this::toChapterDTO)
                    .collect(Collectors.toList());
            dto.setChapters(chapterDTOs);
        }

        return dto;
    }

    public ChapterDTO toChapterDTO(Chapter chapter) {
        ChapterDTO dto = new ChapterDTO();
        dto.setId(chapter.getId());
        dto.setTitle(chapter.getTitle());
        dto.setChapterNumber(chapter.getChapterNumber());
        dto.setUpdatedAt(chapter.getUpdatedAt());
        dto.setViews(chapter.getViews());
        return dto;
    }
}