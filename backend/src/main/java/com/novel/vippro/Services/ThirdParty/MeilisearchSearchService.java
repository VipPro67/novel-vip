package com.novel.vippro.Services.ThirdParty;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.meilisearch.sdk.Client;
import com.meilisearch.sdk.Config;
import com.meilisearch.sdk.Index;
import com.meilisearch.sdk.SearchRequest;
import com.meilisearch.sdk.model.SearchResult;
import com.meilisearch.sdk.model.Searchable;
import com.meilisearch.sdk.model.Settings;
import com.novel.vippro.DTO.Novel.NovelSearchDTO;
import com.novel.vippro.DTO.Novel.SearchSuggestion;
import com.novel.vippro.Mapper.Mapper;
import com.novel.vippro.Mapper.NovelMapper;
import com.novel.vippro.Models.Novel;
import com.novel.vippro.Models.NovelDocument;
import com.novel.vippro.Services.SearchService;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "search.provider", havingValue = "meilisearch")
public class MeilisearchSearchService implements SearchService {

    private static final Logger logger = LoggerFactory.getLogger(MeilisearchSearchService.class);

    private final Mapper mapper;
    private final ObjectMapper objectMapper;
    private Client client;

    @Value("${search.meili.host:http://localhost:7700}")
    private String host;

    @Value("${search.meili.api-key:}")
    private String apiKey;

    @Value("${search.index:novels}")
    private String indexName;

    @PostConstruct
    public void init() {
        this.client = new Client(new Config(host, apiKey));
        // Đảm bảo Index tồn tại và cấu hình Typo Tolerance cho tiếng Việt
        try {
            client.createIndex(indexName, "id");
            Index index = client.index(indexName);
            // Có thể cấu hình thêm searchableAttributes, filterableAttributes tại đây
            Settings settings = new Settings();
            settings.setFilterableAttributes(new String[] {"categories", "genres", "tags"});
            index.updateSettings(settings);
        } catch (Exception e) {
            logger.debug("Index already exists or failed to connect: {}", e.getMessage());
        }
    }

    @Override
    public void indexNovels(List<Novel> novels) {
        try {
            List<NovelDocument> documents = novels.stream()
                    .map(mapper::NoveltoDocument)
                    .toList();
            
            String json = objectMapper.writeValueAsString(documents);
            client.index(indexName).addDocuments(json);
            logger.info("✅ Indexed {} novels to Meilisearch", novels.size());
        } catch (Exception e) {
            logger.error("❌ Failed to index novels to Meilisearch", e);
        }
    }

    @Override
    public void deleteNovel(UUID id) {
        try {
            client.index(indexName).deleteDocument(id.toString());
        } catch (Exception e) {
            logger.error("Failed to delete novel {} from Meilisearch", id, e);
        }
    }

    @Override
    public Page<Novel> search(NovelSearchDTO searchDTO, Pageable pageable) {
        if (searchDTO == null || !searchDTO.hasFilters()) {
            return Page.empty(pageable);
        }

        try {
            NovelSearchDTO filters = searchDTO.cleanedCopy();
            String q = filters.keyword() != null ? filters.keyword() : 
                       (filters.title() != null ? filters.title() : "");

            // Build filter string (ví dụ: "categories = 'Tiên Hiệp'")
            List<String> filterList = new ArrayList<>();
            if (filters.category() != null) filterList.add("categories = '" + filters.category() + "'");
            if (filters.genre() != null) filterList.add("genres = '" + filters.genre() + "'");

            SearchRequest request = SearchRequest.builder()
                    .q(q)
                    .offset(Math.toIntExact(pageable.getOffset()))
                    .limit(pageable.getPageSize())
                    .filter(filterList.toArray(new String[0]))
                    .build();

            Searchable searchable = client.index(indexName).search(request);
            SearchResult results = (SearchResult) searchable;
            
            List<Novel> items = results.getHits().stream()
                    .map(hit -> objectMapper.convertValue(hit, NovelDocument.class))
                    .map(NovelMapper::DocumenttoNovel)
                    .collect(Collectors.toList());

            return new PageImpl<>(items, pageable, results.getEstimatedTotalHits());
        } catch (Exception e) {
            logger.error("Meilisearch query failed", e);
            return Page.empty(pageable);
        }
    }

    @Override
    public List<SearchSuggestion> suggest(String query, int limit) {
        if (query == null || query.isBlank()) return List.of();

        try {
            // Meilisearch cực nhanh nên ta dùng chính search làm suggestion
            SearchRequest request = SearchRequest.builder()
                    .q(query)
                    .limit(limit)
                    .attributesToRetrieve(new String[] {"id", "title"})
                    .build();

            Searchable searchable = client.index(indexName).search(request);
            SearchResult results = (SearchResult) searchable;
            
            return results.getHits().stream()
                    .map(hit -> new SearchSuggestion(
                            (String) hit.get("id"),
                            (String) hit.get("title")
                    ))
                    .toList();
        } catch (Exception e) {
            logger.warn("Meilisearch suggestion failed", e);
            return List.of();
        }
    }
}