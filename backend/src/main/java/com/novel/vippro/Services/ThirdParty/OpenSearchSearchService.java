package com.novel.vippro.Services.ThirdParty;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.api.gax.rpc.NotFoundException;
import com.novel.vippro.DTO.Novel.NovelSearchDTO;
import com.novel.vippro.DTO.Novel.SearchSuggestion;
import com.novel.vippro.Mapper.Mapper;
import com.novel.vippro.Mapper.NovelMapper;
import com.novel.vippro.Models.Novel;
import com.novel.vippro.Models.NovelDocument;
import com.novel.vippro.Services.SearchService;

import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "search.provider", havingValue = "opensearch")
public class OpenSearchSearchService implements SearchService {

    private static final Logger logger = LoggerFactory.getLogger(OpenSearchSearchService.class);
    private static final String DEFAULT_INDEX = "novels";
    
    private static final String SUGGEST_NAME = "title_suggest";

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final Mapper mapper;

    @Value("${search.full-uri:https://compassionate-dougla-1h9hh5eb.us-east-1.bonsaisearch.net}")
    private String fullUri;

    @Value("${search.index:" + DEFAULT_INDEX + "}")
    private String indexName;

    @Value("${search.username:}")
    private String username;

    @Value("${search.password:}")
    private String password;

    private URI uri(String path) {
        String normalizedPath = path.startsWith("/") ? path : "/" + path;
        return URI.create(fullUri + normalizedPath);
    }

    @Override
    @Transactional
    public void indexNovels(List<Novel> novels) {
        try {
            StringBuilder bulkRequest = new StringBuilder();

            for (Novel novel : novels) {
                NovelDocument document = mapper.NoveltoDocument(novel);
                if (document.getId() == null) {
                    document.setId(novel.getId());
                }

                // 1️⃣ Bulk metadata line (action + metadata)
                bulkRequest.append("{\"index\":{\"_index\":\"")
                        .append(indexName)
                        .append("\",\"_id\":\"")
                        .append(document.getId())
                        .append("\"}}\n");

                // 2️⃣ Document JSON line
                bulkRequest.append(objectMapper.writeValueAsString(document))
                        .append("\n");
            }

            URI uri = uri("/_bulk");

            RequestEntity<String> request = RequestEntity
                    .post(uri)
                    .contentType(MediaType.APPLICATION_JSON)
                    .header(HttpHeaders.AUTHORIZATION,
                            "Basic " + Base64.getEncoder()
                                    .encodeToString((username + ":" + password)
                                    .getBytes(StandardCharsets.UTF_8)))
                    .body(bulkRequest.toString());

            restTemplate.exchange(request, String.class);
            logger.info("✅ Indexed {} novels successfully in bulk.", novels.size());

        } catch (Exception e) {
            logger.error("❌ Bulk index failed for {} novels", novels.size(), e);
        }
    }

    @Override
    public void deleteNovel(UUID id) {
        try {
            URI uri = uri(String.format("/%s/_doc/%s", indexName, id));
            RequestEntity<Void> request = RequestEntity
                    .delete(uri)
                    .header(HttpHeaders.AUTHORIZATION, "Basic " + Base64.getEncoder().encodeToString((username + ":" + password).getBytes(StandardCharsets.UTF_8)))
                    .build();
            restTemplate.exchange(request, String.class);
        } catch (Exception e) {
            logger.error("Failed to delete novel {} from OpenSearch", id, e);
        }
    }

    @Override
    public Page<Novel> search(NovelSearchDTO searchDTO, Pageable pageable) {
        if (searchDTO == null || !searchDTO.hasFilters()) {
            return Page.empty(pageable);
        }

        try {
            ObjectNode body = objectMapper.createObjectNode();
            body.put("from", Math.toIntExact(pageable.getOffset()));
            body.put("size", pageable.getPageSize());

            ObjectNode boolQuery = body.putObject("query").putObject("bool");
            List<JsonNode> mustClauses = new ArrayList<>();

            NovelSearchDTO filters = searchDTO.cleanedCopy();

            if (filters.keyword() != null) {
                ObjectNode multiMatch = objectMapper.createObjectNode();
                multiMatch.putArray("fields")
                        .add("title^3")
                        .add("description^2")
                        .add("author");
                multiMatch.put("query", filters.keyword());
                mustClauses.add(objectMapper.createObjectNode().set("multi_match", multiMatch));
            }
            if (filters.title() != null) {
                ObjectNode matchPhrase = objectMapper.createObjectNode();
                matchPhrase.put("title", filters.title());
                mustClauses.add(objectMapper.createObjectNode().set("match_phrase", matchPhrase));
            }
            if (filters.author() != null) {
                ObjectNode matchPhrase = objectMapper.createObjectNode();
                matchPhrase.put("author", filters.author());
                mustClauses.add(objectMapper.createObjectNode().set("match_phrase", matchPhrase));
            }
            if (filters.category() != null) {
                ObjectNode term = objectMapper.createObjectNode();
                term.put("categories", filters.category());
                mustClauses.add(objectMapper.createObjectNode().set("term", term));
            }
            if (filters.genre() != null) {
                ObjectNode term = objectMapper.createObjectNode();
                term.put("genres", filters.genre());
                mustClauses.add(objectMapper.createObjectNode().set("term", term));
            }
            if (filters.tag() != null) {
                ObjectNode term = objectMapper.createObjectNode();
                term.put("tags", filters.tag());
                mustClauses.add(objectMapper.createObjectNode().set("term", term));
            }

            if (mustClauses.isEmpty()) {
                mustClauses.add(objectMapper.createObjectNode().set("match_all", objectMapper.createObjectNode()));
            }

            ArrayNode mustArray = objectMapper.createArrayNode();
            mustClauses.forEach(mustArray::add);
            boolQuery.set("must", mustArray);

            if (pageable.getSort().isSorted()) {
                ArrayNode sortArray = body.putArray("sort");
                for (Sort.Order order : pageable.getSort()) {
                    ObjectNode fieldSort = objectMapper.createObjectNode();
                    fieldSort.putObject(order.getProperty())
                            .put("order", order.getDirection().isAscending() ? "asc" : "desc");
                    sortArray.add(fieldSort);
                }
            }

            URI uri = uri(String.format("/%s/_search", indexName));
            RequestEntity<String> request = RequestEntity.post(uri)
                    .contentType(MediaType.APPLICATION_JSON)
                    .header(HttpHeaders.AUTHORIZATION, "Basic " + Base64.getEncoder().encodeToString((username + ":" + password).getBytes(StandardCharsets.UTF_8)))
                    .body(objectMapper.writeValueAsString(body));
            ResponseEntity<String> response = restTemplate.exchange(request, String.class);
            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                logger.warn("OpenSearch search returned non-success status: {}", response.getStatusCode());
                return Page.empty(pageable);
            }

            JsonNode root = objectMapper.readTree(response.getBody());
            ArrayNode hitsNode = (ArrayNode) root.path("hits").path("hits");
            if (hitsNode == null || hitsNode.isEmpty()) {
                return Page.empty(pageable);
            }

            List<Novel> items = new ArrayList<>();
            for (JsonNode hit : hitsNode) {
                JsonNode source = hit.path("_source");
                if (source == null || source.isMissingNode()) {
                    continue;
                }
                NovelDocument document = objectMapper.treeToValue(source, NovelDocument.class);
                String idValue = hit.path("_id").asText(null);
                if (document != null && idValue != null) {
                    try {
                        document.setId(UUID.fromString(idValue));
                    } catch (IllegalArgumentException ignored) {
                        // leave id as-is if not a UUID
                    }
                }
                if (document != null) {
                    items.add(NovelMapper.DocumenttoNovel(document));
                }
            }

            long total = extractTotalHits(root.path("hits").path("total"));
            return new PageImpl<>(items, pageable, total);
        }
        catch (NotFoundException e) {
            logger.warn("OpenSearch index '{}' not found: {}", indexName, e.getMessage());
            return Page.empty(pageable);
        }
        catch (Exception e) {
            logger.error("OpenSearch query failed", e);
            return Page.empty(pageable);
        }
    }

    @Override
    public List<SearchSuggestion> suggest(String query, int limit) {
        if (query == null || query.isBlank()) {
            return List.of();
        }

        List<SearchSuggestion> fromCompletion = completionSuggest(query, limit);
        if (!fromCompletion.isEmpty()) {
            return fromCompletion;
        }
        return prefixSuggest(query, limit);
    }

    private List<SearchSuggestion> completionSuggest(String query, int limit) {
        try {
            ObjectNode body = objectMapper.createObjectNode();
            ObjectNode suggestNode = body.putObject("suggest");
            ObjectNode titleSuggest = suggestNode.putObject(SUGGEST_NAME);
            titleSuggest.put("prefix", query);
            titleSuggest.putObject("completion")
                    .put("field", "suggest")
                    .put("skip_duplicates", true);
            body.put("size", 0);

            URI uri = uri(String.format("/%s/_search", indexName));
            RequestEntity<String> request = RequestEntity.post(uri)
                    .contentType(MediaType.APPLICATION_JSON)
                    .header(HttpHeaders.AUTHORIZATION, "Basic " + Base64.getEncoder().encodeToString((username + ":" + password).getBytes(StandardCharsets.UTF_8)))
                    .body(objectMapper.writeValueAsString(body));

            ResponseEntity<String> response = restTemplate.exchange(request, String.class);
            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                return List.of();
            }
            JsonNode root = objectMapper.readTree(response.getBody());
            JsonNode suggestions = root.path("suggest").path(SUGGEST_NAME);
            if (!suggestions.isArray() || suggestions.isEmpty()) {
                return List.of();
            }

            List<String> titles = new ArrayList<>();
            for (JsonNode entry : suggestions) {
                JsonNode options = entry.path("options");
                if (options.isArray()) {
                    for (JsonNode option : options) {
                        String text = option.path("text").asText(null);
                        if (text != null && !text.isBlank() && !titles.contains(text)) {
                            titles.add(text);
                            if (titles.size() >= limit) {
                                break;
                            }
                        }
                    }
                }
                if (titles.size() >= limit) {
                    break;
                }
            }

            if (titles.isEmpty()) {
                return List.of();
            }

            List<SearchSuggestion> resolved = resolveTitles(titles, limit);
            if (!resolved.isEmpty()) {
                return resolved;
            }
        } catch (Exception e) {
            logger.debug("OpenSearch completion suggestion failed: {}", e.getMessage());
        }
        return List.of();
    }

    private List<SearchSuggestion> resolveTitles(List<String> titles, int limit) {
        try {
            ObjectNode body = objectMapper.createObjectNode();
            ObjectNode query = body.putObject("query");
            ArrayNode values = objectMapper.createArrayNode();
            titles.forEach(values::add);
            ObjectNode terms = objectMapper.createObjectNode();
            terms.set("title.keyword", values);
            query.set("terms", terms);
            body.put("size", limit);
            body.putObject("_source").putArray("includes").add("title");
            URI uri = uri(String.format("/%s/_search", indexName));
            RequestEntity<String> request = RequestEntity.post(uri)
                    .contentType(MediaType.APPLICATION_JSON)
                    .header(HttpHeaders.AUTHORIZATION, "Basic " + Base64.getEncoder().encodeToString((username + ":" + password).getBytes(StandardCharsets.UTF_8)))
                    .body(objectMapper.writeValueAsString(body));
            ResponseEntity<String> response = restTemplate.exchange(request, String.class);
            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                return List.of();
            }

            JsonNode root = objectMapper.readTree(response.getBody());
            ArrayNode hitsNode = (ArrayNode) root.path("hits").path("hits");
            if (hitsNode == null || hitsNode.isEmpty()) {
                return List.of();
            }

            Map<String, String> titleToId = new LinkedHashMap<>();
            for (JsonNode hit : hitsNode) {
                JsonNode source = hit.path("_source");
                if (source == null || source.isMissingNode()) {
                    continue;
                }
                String title = source.path("title").asText(null);
                String id = hit.path("_id").asText(null);
                if (title != null && id != null && !titleToId.containsKey(title)) {
                    titleToId.put(title, id);
                }
            }

            return titles.stream()
                    .map(t -> {
                        String id = titleToId.get(t);
                        return (id != null) ? new SearchSuggestion(id, t) : null;
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toCollection(LinkedList::new));
        } catch (Exception e) {
            logger.debug("OpenSearch suggestion resolution failed: {}", e.getMessage());
            return List.of();
        }
    }

    private List<SearchSuggestion> prefixSuggest(String query, int limit) {
        try {
            ObjectNode body = objectMapper.createObjectNode();
            ObjectNode matchBoolPrefix = objectMapper.createObjectNode();
            ObjectNode titleNode = objectMapper.createObjectNode();
            titleNode.put("query", query);
            matchBoolPrefix.set("title", titleNode);
            body.putObject("query").set("match_bool_prefix", matchBoolPrefix);
            body.put("size", limit);
            body.putObject("_source").putArray("includes").add("title");

            URI uri = uri(String.format("/%s/_search", indexName));
            RequestEntity<String> request = RequestEntity.post(uri)
                    .contentType(MediaType.APPLICATION_JSON)
                    .header(HttpHeaders.AUTHORIZATION, "Basic " + Base64.getEncoder().encodeToString((username + ":" + password).getBytes(StandardCharsets.UTF_8)))
                    .body(objectMapper.writeValueAsString(body));

            ResponseEntity<String> response = restTemplate.exchange(request, String.class);
            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                return List.of();
            }

            JsonNode root = objectMapper.readTree(response.getBody());
            ArrayNode hitsNode = (ArrayNode) root.path("hits").path("hits");
            if (hitsNode == null || hitsNode.isEmpty()) {
                return List.of();
            }

            Map<String, SearchSuggestion> deduped = new LinkedHashMap<>();
            for (JsonNode hit : hitsNode) {
                JsonNode source = hit.path("_source");
                if (source == null || source.isMissingNode()) {
                    continue;
                }
                String title = source.path("title").asText(null);
                String id = hit.path("_id").asText(null);
                if (title != null && id != null && !deduped.containsKey(title)) {
                    deduped.put(title, new SearchSuggestion(id, title));
                }
            }
            return new ArrayList<>(deduped.values());
        } catch (Exception e) {
            logger.warn("OpenSearch prefix suggestion failed: {}", e.getMessage());
            return List.of();
        }
    }

    private long extractTotalHits(JsonNode totalNode) {
        if (totalNode == null || totalNode.isMissingNode()) {
            return 0L;
        }
        if (totalNode.isNumber()) {
            return totalNode.asLong();
        }
        return totalNode.path("value").asLong(0);
    }
}
