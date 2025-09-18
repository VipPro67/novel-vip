package com.novel.vippro.Controllers;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import com.novel.vippro.DTO.Novel.NovelDTO;
import com.novel.vippro.DTO.Novel.NovelSearchDTO;
import com.novel.vippro.Payload.Response.PageResponse;
import com.novel.vippro.Services.NovelService;

/**
 * Handles live search requests over WebSocket. Incoming search queries are
 * debounced on the client but we additionally cancel any in-flight search on
 * the server to avoid returning stale results when a user types quickly.
 */
@Controller
public class SearchSocketController {

    private final SimpMessagingTemplate messagingTemplate;
    private final NovelService novelService;
    private final ExecutorService executor = Executors.newCachedThreadPool();
    private final Map<String, Future<?>> inFlight = new ConcurrentHashMap<>();

    @Autowired
    public SearchSocketController(SimpMessagingTemplate messagingTemplate, NovelService novelService) {
        this.messagingTemplate = messagingTemplate;
        this.novelService = novelService;
    }

    /**
     * Receives search queries and streams the top matching novels to a
     * session-scoped topic. Any previous search for the same session is
     * cancelled to prevent outdated results from being sent.
     */
    @MessageMapping("/search")
    public void handleSearch(String query, @Header("simpSessionId") String sessionId) {
        // cancel any previous search for this session
        Future<?> previous = inFlight.remove(sessionId);
        if (previous != null) {
            previous.cancel(true);
        }

        // if query is blank send empty suggestion list
        if (query == null || query.isBlank()) {
            messagingTemplate.convertAndSend("/topic/search", List.of());
            return;
        }

        Future<?> task = executor.submit(() -> {
            Pageable pageable = PageRequest.of(0, 5); // limit suggestions
            NovelSearchDTO searchDTO = new NovelSearchDTO();
            searchDTO.setKeyword(query);
            PageResponse<NovelDTO> page = novelService.searchNovels(searchDTO, pageable);
            messagingTemplate.convertAndSend("/topic/search", page.getContent());
        });

        inFlight.put(sessionId, task);
    }
}
