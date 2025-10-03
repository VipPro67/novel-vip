package com.novel.vippro.Services;

import java.util.List;

import org.springframework.stereotype.Service;

import com.novel.vippro.DTO.Novel.SearchSuggestion;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SuggestService {

    private final SearchService searchService;

    public List<SearchSuggestion> suggest(String query, int size) {
        return searchService.suggest(query, size);
    }
}
