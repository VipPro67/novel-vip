package com.novel.vippro.Services;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.Criteria;
import org.springframework.data.elasticsearch.core.query.CriteriaQuery;
import org.springframework.stereotype.Service;

import com.novel.vippro.Models.Novel;
import com.novel.vippro.Repository.NovelRepository;
import com.novel.vippro.Search.NovelDocument;

/**
 * Service layer for indexing and searching novels in Elasticsearch.
 */
@Service
public class NovelSearchService {

    @Autowired
    private ElasticsearchOperations elasticsearchOperations;

    @Autowired
    private NovelRepository novelRepository;

    /**
     * Index a novel in Elasticsearch.
     */
    public void indexNovel(Novel novel) {
        try {
            NovelDocument document = new NovelDocument(novel.getId(), novel.getTitle(), novel.getDescription(),
                    novel.getAuthor());
            elasticsearchOperations.save(document);
        } catch (Exception e) {
            // Elasticsearch may be unavailable; log and continue without failing.
        }
    }

    /**
     * Remove a novel from the Elasticsearch index.
     */
    public void deleteNovel(UUID id) {
        try {
            elasticsearchOperations.delete(id.toString(), NovelDocument.class);
        } catch (Exception e) {
            // Ignore failures when Elasticsearch is unavailable.
        }
    }

    /**
     * Search novels using Elasticsearch. Falls back to an empty page on failure.
     */
    public Page<Novel> search(String keyword, Pageable pageable) {
        try {
            Criteria criteria = new Criteria("title").matches(keyword)
                    .or(new Criteria("description").matches(keyword))
                    .or(new Criteria("author").matches(keyword));
            CriteriaQuery query = new CriteriaQuery(criteria);
            query.setPageable(pageable);

            SearchHits<NovelDocument> hits = elasticsearchOperations.search(query, NovelDocument.class);
            List<UUID> ids = hits.getSearchHits().stream()
                    .map(hit -> hit.getContent().getId())
                    .toList();

            if (ids.isEmpty()) {
                return Page.empty(pageable);
            }

            List<Novel> novels = novelRepository.findAllById(ids);
            Map<UUID, Novel> novelMap = novels.stream()
                    .collect(Collectors.toMap(Novel::getId, Function.identity()));

            List<Novel> ordered = ids.stream()
                    .map(novelMap::get)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            return new PageImpl<>(ordered, pageable, hits.getTotalHits());
        } catch (Exception e) {
            // Elasticsearch may be unavailable; return empty page so caller can fallback.
            return Page.empty(pageable);
        }
    }
}

