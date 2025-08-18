package com.novel.vippro.Services;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.client.elc.QueryBuilders;

import org.springframework.stereotype.Service;

import com.novel.vippro.Mapper.Mapper;
import com.novel.vippro.Models.Novel;
import com.novel.vippro.Models.NovelDocument;
import com.novel.vippro.Repository.NovelRepository;

/**
 * Service layer for indexing and searching novels in Elasticsearch.
 */
@Service
public class NovelSearchService {

    private static final Logger logger = LoggerFactory.getLogger(NovelSearchService.class);

    @Autowired
    private ElasticsearchOperations elasticsearchOperations;

    @Autowired
    private NovelRepository novelRepository;

    @Autowired
    private Mapper mapper;

    /**
     * Index a novel in Elasticsearch.
     */
    public void indexNovel(Novel novel) {
        try {
            NovelDocument document = mapper.NoveltoDocument(novel);
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
			// Build match query exactly like Kibana
			NativeQuery searchQuery = NativeQuery.builder()
				.withQuery(q -> q.matchPhrase(m -> m
					.field("title")
					.query(keyword)
				))
				.withPageable(pageable)
				.build();

			SearchHits<NovelDocument> hits =
				elasticsearchOperations.search(searchQuery, NovelDocument.class);

			List<UUID> ids = hits.getSearchHits().stream()
				.map(hit -> hit.getContent().getId())
				.toList();

			if (ids.isEmpty()) {
				return Page.empty(pageable);
			}

			// Preserve order
			List<Novel> novels = novelRepository.findAllById(ids);
			Map<UUID, Novel> novelMap = novels.stream()
				.collect(Collectors.toMap(Novel::getId, Function.identity()));

			List<Novel> ordered = ids.stream()
				.map(novelMap::get)
				.filter(Objects::nonNull)
				.toList();

			return new PageImpl<>(ordered, pageable, hits.getTotalHits());
		} catch (Exception e) {
			logger.error("Error searching novels", e);
			return Page.empty(pageable);
		}
	}

}

