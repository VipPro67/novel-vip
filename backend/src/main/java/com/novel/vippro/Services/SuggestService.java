package com.novel.vippro.Services;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.CompletionSuggestOption;
import co.elastic.clients.elasticsearch.core.search.Suggestion;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import com.novel.vippro.Models.NovelDocument;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SuggestService {

	private final ElasticsearchClient es;

	private static final String INDEX = "novels";
	private static final String SUGGEST_NAME = "title_suggest";

	public record SuggestItemDTO(String id, String title) {
	}

	public List<SuggestItemDTO> suggest(String q, int size) {
		if (q == null || q.isBlank())
			return List.of();
		try {
			SearchRequest req = SearchRequest.of(s -> s
					.index(INDEX)
					.suggest(sb -> sb.suggesters(SUGGEST_NAME, st -> st
							.completion(c -> c.field("suggest").skipDuplicates(true))
							.prefix(q)))
					.size(0));

			SearchResponse<NovelDocument> resp = es.search(req, NovelDocument.class);

			Map<String, List<Suggestion<NovelDocument>>> sm = resp.suggest();
			if (sm != null) {
				List<Suggestion<NovelDocument>> entries = sm.get(SUGGEST_NAME);
				if (entries != null && !entries.isEmpty()) {
					List<String> titles = entries.stream()
							.filter(Objects::nonNull)
							.filter(sug -> sug.completion() != null)
							.flatMap(sug -> sug.completion().options().stream())
							.map(CompletionSuggestOption::text)
							.filter(t -> t != null && !t.isBlank())
							.distinct()
							.limit(size)
							.toList();

					if (!titles.isEmpty()) {
						SearchResponse<NovelDocument> resolve = es.search(sr -> sr
								.index(INDEX)
								.size(size)
								.query(qb -> qb.terms(t -> t
										.field("title.keyword")
										.terms(v -> v.value(
												titles.stream().map(FieldValue::of).toList()))))
								.source(src -> src.filter(f -> f.includes("title"))),
								NovelDocument.class);
						Map<String, String> titleToId = resolve.hits().hits().stream()
								.filter(h -> h.source() != null && h.source().getTitle() != null)
								.collect(Collectors.toMap(
										h -> h.source().getTitle(),
										h -> h.id(),
										(a, b) -> a));
						List<SuggestItemDTO> out = titles.stream()
								.map(t -> {
									String id = titleToId.get(t);
									return (id != null) ? new SuggestItemDTO(id, t) : null;
								})
								.filter(Objects::nonNull)
								.collect(Collectors.toCollection(LinkedList::new));

						if (!out.isEmpty())
							return out;
					}
				}
			}
		} catch (Exception e) {
			log.debug("Completion suggest failed; falling back. {}", e.getMessage());
		}
		try {
			SearchResponse<NovelDocument> resp = es.search(s -> s
					.index(INDEX)
					.query(qb -> qb.matchBoolPrefix(m -> m.field("title").query(q)))
					.size(size)
					.source(src -> src.filter(f -> f.includes("title"))),
					NovelDocument.class);
			return resp.hits().hits().stream()
					.filter(h -> h.source() != null && h.source().getTitle() != null)
					.map(h -> new SuggestItemDTO(h.id(), h.source().getTitle()))
					.collect(Collectors.collectingAndThen(
							Collectors.toMap(
									SuggestItemDTO::title,
									Function.identity(),
									(a, b) -> a,
									LinkedHashMap::new),
							m -> m.values().stream().limit(size).toList()));

		} catch (Exception e) {
			log.warn("Fallback prefix search failed: {}", e.getMessage());
			return List.of();
		}
	}
}