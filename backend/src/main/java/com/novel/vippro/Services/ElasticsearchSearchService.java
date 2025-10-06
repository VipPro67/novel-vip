package com.novel.vippro.Services;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.checkerframework.checker.units.qual.m;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Service;

import com.novel.vippro.DTO.Novel.NovelSearchDTO;
import com.novel.vippro.DTO.Novel.SearchSuggestion;
import com.novel.vippro.Mapper.Mapper;
import com.novel.vippro.Mapper.NovelMapper;
import com.novel.vippro.Models.Novel;
import com.novel.vippro.Models.NovelDocument;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.CompletionSuggestOption;
import co.elastic.clients.elasticsearch.core.search.Suggestion;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "search.provider", havingValue = "elasticsearch", matchIfMissing = true)
public class ElasticsearchSearchService implements SearchService {

    private static final Logger logger = LoggerFactory.getLogger(ElasticsearchSearchService.class);


    private static final String SUGGEST_NAME = "title_suggest";

    @Value("${search.index:novels}")
    private String indexName;

    private final ElasticsearchOperations elasticsearchOperations;
    private final Mapper mapper;
    private final ElasticsearchClient elasticsearchClient;

    @Override
    public void indexNovels(List<Novel> novel) {
        for (Novel n : novel) {
            try {
                NovelDocument doc = mapper.NoveltoDocument(n);
                if (doc != null) {
                    elasticsearchOperations.save(doc);
                }
            } catch (Exception e) {
                logger.error("Failed to index novel {}: {}", n.getId(), e.getMessage());
            }
        }
    }

    @Override
    public void deleteNovel(UUID id) {
        try {
            elasticsearchOperations.delete(id.toString(), NovelDocument.class);
        } catch (Exception e) {
            logger.error("Failed to delete novel {} from search index", id, e);
        }
    }

    @Override
    public Page<Novel> search(NovelSearchDTO searchDTO, Pageable pageable) {
        if (searchDTO == null || !searchDTO.hasFilters()) {
            return Page.empty(pageable);
        }

        try {
            NovelSearchDTO filters = searchDTO.cleanedCopy();
            final boolean[] clauseAdded = { false };

            NativeQuery searchQuery = NativeQuery.builder()
                    .withQuery(q -> q.bool(b -> {
                        if (filters.getKeyword() != null) {
                            clauseAdded[0] = true;
                            b.must(m -> m.multiMatch(mm -> mm
                                    .fields("title^3", "description^2", "author")
                                    .query(filters.getKeyword())));
                        }
                        if (filters.getTitle() != null) {
                            clauseAdded[0] = true;
                            b.must(m -> m.matchPhrase(mp -> mp
                                    .field("title")
                                    .query(filters.getTitle())));
                        }
                        if (filters.getAuthor() != null) {
                            clauseAdded[0] = true;
                            b.must(m -> m.matchPhrase(mp -> mp
                                    .field("author")
                                    .query(filters.getAuthor())));
                        }
                        if (filters.getCategory() != null) {
                            clauseAdded[0] = true;
                            b.must(m -> m.term(t -> t
                                    .field("categories")
                                    .value(v -> v.stringValue(filters.getCategory()))));
                        }
                        if (filters.getGenre() != null) {
                            clauseAdded[0] = true;
                            b.must(m -> m.term(t -> t
                                    .field("genres")
                                    .value(v -> v.stringValue(filters.getGenre()))));
                        }
                        if (!clauseAdded[0]) {
                            b.must(m -> m.matchAll(ma -> ma));
                        }
                        return b;
                    }))
                    .withPageable(pageable)
                    .build();

            SearchHits<NovelDocument> hits = elasticsearchOperations.search(searchQuery, NovelDocument.class);

            if (hits.isEmpty()) {
                return Page.empty(pageable);
            }

            List<Novel> ordered = hits.getSearchHits().stream()
                    .map(SearchHit::getContent)
                    .map(NovelMapper::DocumenttoNovel)
                    .toList();

            return new PageImpl<>(ordered, pageable, hits.getTotalHits());
        } catch (Exception e) {
            logger.error("Error searching novels", e);
            return Page.empty(pageable);
        }
    }

    @Override
    public List<SearchSuggestion> suggest(String query, int limit) {
        if (query == null || query.isBlank()) {
            return List.of();
        }
        try {
            SearchRequest request = SearchRequest.of(s -> s
                    .index(indexName)
                    .suggest(sb -> sb.suggesters(SUGGEST_NAME, st -> st
                            .completion(c -> c.field("suggest").skipDuplicates(true))
                            .prefix(query)))
                    .size(0));

            SearchResponse<NovelDocument> response = elasticsearchClient.search(request, NovelDocument.class);
            Map<String, List<Suggestion<NovelDocument>>> suggestions = response.suggest();

            if (suggestions != null) {
                List<Suggestion<NovelDocument>> entries = suggestions.get(SUGGEST_NAME);
                if (entries != null && !entries.isEmpty()) {
                    List<String> titles = entries.stream()
                            .filter(Objects::nonNull)
                            .filter(sug -> sug.completion() != null)
                            .flatMap(sug -> sug.completion().options().stream())
                            .map(CompletionSuggestOption::text)
                            .filter(t -> t != null && !t.isBlank())
                            .distinct()
                            .limit(limit)
                            .toList();

                    if (!titles.isEmpty()) {
                        SearchResponse<NovelDocument> resolve = elasticsearchClient.search(sr -> sr
                                .index(indexName)
                                .size(limit)
                                .query(qb -> qb.terms(t -> t
                                        .field("title.keyword")
                                        .terms(v -> v.value(titles.stream().map(FieldValue::of).toList()))))
                                .source(src -> src.filter(f -> f.includes("title"))),
                                NovelDocument.class);

                        Map<String, String> titleToId = resolve.hits().hits().stream()
                                .filter(h -> h.source() != null && h.source().getTitle() != null)
                                .collect(Collectors.toMap(
                                        h -> h.source().getTitle(),
                                        h -> h.id(),
                                        (a, b) -> a));

                        List<SearchSuggestion> output = titles.stream()
                                .map(t -> {
                                    String id = titleToId.get(t);
                                    return (id != null) ? new SearchSuggestion(id, t) : null;
                                })
                                .filter(Objects::nonNull)
                                .collect(Collectors.toCollection(LinkedList::new));

                        if (!output.isEmpty()) {
                            return output;
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.debug("Completion suggest failed; falling back. {}", e.getMessage());
        }

        try {
            SearchResponse<NovelDocument> response = elasticsearchClient.search(s -> s
                    .index(indexName)
                    .query(qb -> qb.matchBoolPrefix(m -> m.field("title").query(query)))
                    .size(limit)
                    .source(src -> src.filter(f -> f.includes("title"))),
                    NovelDocument.class);

            return response.hits().hits().stream()
                    .filter(h -> h.source() != null && h.source().getTitle() != null)
                    .map(h -> new SearchSuggestion(h.id(), h.source().getTitle()))
                    .collect(Collectors.collectingAndThen(
                            Collectors.toMap(
                                    SearchSuggestion::title,
                                    Function.identity(),
                                    (a, b) -> a,
                                    LinkedHashMap::new),
                            m -> new ArrayList<>(m.values())));
        } catch (Exception e) {
            logger.warn("Fallback prefix search failed: {}", e.getMessage());
            return List.of();
        }
    }
}

