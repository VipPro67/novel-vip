package com.novel.vippro.Services;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Suggestion;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import com.novel.vippro.Models.NovelDocument;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class SuggestService {

	private final ElasticsearchClient es;

	private static final String INDEX = "novels";
	private static final String SUGGEST_NAME = "novel-suggest";

	public List<String> suggest(String q, int size) {
		if (q == null || q.isBlank())
			return List.of();

		// 1) Completion suggester (if you’ve added a `suggest` completion field)
		try {
			SearchRequest req = SearchRequest.of(s -> s
					.index(INDEX)
					.suggest(sb -> sb
							.suggesters(SUGGEST_NAME, sug -> sug
									.prefix(q)
									.completion(c -> c
											.field("suggest")
											.skipDuplicates(true))))
					.size(0));

			SearchResponse<NovelDocument> resp = es.search(req, NovelDocument.class);

			Map<String, List<Suggestion<NovelDocument>>> suggestMap = resp.suggest();
			if (suggestMap != null) {
				List<Suggestion<NovelDocument>> entries = suggestMap.get(SUGGEST_NAME);
				if (entries != null && !entries.isEmpty()) {
					List<String> out = new ArrayList<>();
					for (Suggestion<NovelDocument> s : entries) {
						if (s != null && s.completion() != null) {
							s.completion().options().forEach(opt -> {
								if (out.size() < size)
									out.add(opt.text());
							});
						}
					}
					if (!out.isEmpty()) {
						return out.stream().distinct().limit(size).toList();
					}
				}
			}
		} catch (Exception e) {
			log.debug("Completion suggest failed or mapping missing. Falling back. {}", e.getMessage());
		}

		// 2) Fallback: fast prefix search on `title`
		try {
			SearchResponse<NovelDocument> resp = es.search(s -> s
					.index(INDEX)
					.query(qb -> qb.matchBoolPrefix(m -> m.field("title").query(q)))
					.size(size)
					.source(src -> src.filter(f -> f.includes("title"))), NovelDocument.class);

			return resp.hits().hits().stream()
					.map(h -> h.source() != null ? h.source().getTitle() : null)
					.filter(t -> t != null && !t.isBlank())
					.distinct()
					.limit(size)
					.toList();
		} catch (Exception e) {
			log.warn("Fallback prefix search failed: {}", e.getMessage());
			return List.of();
		}
	}
}
