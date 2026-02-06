package com.novel.vippro.Services;

import java.util.List;

import org.springframework.stereotype.Service;

import com.novel.vippro.DTO.Novel.SearchSuggestion;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SuggestService {

    private final SearchService searchService;
    private final com.novel.vippro.Repository.NovelRepository novelRepository;

    public List<SearchSuggestion> suggest(String query, int size) {
        List<SearchSuggestion> suggestions = List.of();
        try {
            suggestions = searchService.suggest(query, size);
        } catch (Exception e) {
        }

        if (suggestions.isEmpty()) {
            org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(0, size);
            suggestions = novelRepository.findByTitleContainingIgnoreCaseOrderByRatingDesc(query, pageable)
                    .map(novel -> new SearchSuggestion(novel.getId().toString(), novel.getTitle()))
                    .getContent();
        }
        return suggestions;
    }
}
