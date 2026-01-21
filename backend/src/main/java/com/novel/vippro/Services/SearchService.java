package com.novel.vippro.Services;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.novel.vippro.DTO.Novel.NovelSearchDTO;
import com.novel.vippro.DTO.Novel.SearchSuggestion;
import com.novel.vippro.Models.Novel;

/**
 * Provides indexing, querying, and suggestion capabilities for the search backend.
 */
public interface SearchService {

    void indexNovels(List<Novel> novel);
    
    default void indexNovel(Novel novel) {
        indexNovels(List.of(novel));
    }

    void deleteNovel(UUID id);

    Page<Novel> search(NovelSearchDTO searchDTO, Pageable pageable);

    List<SearchSuggestion> suggest(String query, int limit);
}
