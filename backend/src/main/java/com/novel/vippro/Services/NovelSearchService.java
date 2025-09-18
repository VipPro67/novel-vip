package com.novel.vippro.Services;

import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;

import org.springframework.stereotype.Service;

import com.novel.vippro.DTO.Novel.NovelSearchDTO;
import com.novel.vippro.Mapper.Mapper;
import com.novel.vippro.Mapper.NovelMapper;
import com.novel.vippro.Models.Novel;
import com.novel.vippro.Models.NovelDocument;

@Service
public class NovelSearchService {

    private static final Logger logger = LoggerFactory.getLogger(NovelSearchService.class);

    @Autowired
    private ElasticsearchOperations elasticsearchOperations;

    @Autowired
    private Mapper mapper;

    public void indexNovel(Novel novel) {
        try {
            NovelDocument document = mapper.NoveltoDocument(novel);
            elasticsearchOperations.save(document);
        } catch (Exception e) {
        }
    }

    public void deleteNovel(UUID id) {
        try {
            elasticsearchOperations.delete(id.toString(), NovelDocument.class);
        } catch (Exception e) {
        }
    }

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

            SearchHits<NovelDocument> hits =
                    elasticsearchOperations.search(searchQuery, NovelDocument.class);

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

}

