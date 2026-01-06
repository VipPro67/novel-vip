package com.novel.vippro.Controllers;

import com.novel.vippro.Security.UserDetailsImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/notifications")
@PreAuthorize("isAuthenticated()")
public class NotificationStreamController {

    private static final Logger logger = LoggerFactory.getLogger(NotificationStreamController.class);
    private static final long SSE_TIMEOUT = 30 * 60 * 1000L; // 30 minutes
    
    // Store active SSE connections per user
    private final Map<UUID, CopyOnWriteArrayList<SseEmitter>> userEmitters = new ConcurrentHashMap<>();

    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamNotifications() {
        UUID userId = UserDetailsImpl.getCurrentUserId();
        logger.info("Client connecting to notification stream for user: {}", userId);
        
        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT);
        
        // Add emitter to user's list
        userEmitters.computeIfAbsent(userId, k -> new CopyOnWriteArrayList<>()).add(emitter);
        logger.info("Active connections for user {}: {}", userId, userEmitters.get(userId).size());
        
        // Set up cleanup on completion, timeout, or error
        Runnable cleanup = () -> {
            logger.info("Cleaning up SSE connection for user: {}", userId);
            CopyOnWriteArrayList<SseEmitter> emitters = userEmitters.get(userId);
            if (emitters != null) {
                emitters.remove(emitter);
                if (emitters.isEmpty()) {
                    userEmitters.remove(userId);
                    logger.info("Removed user {} from active connections", userId);
                }
            }
        };
        
        emitter.onCompletion(cleanup);
        emitter.onTimeout(cleanup);
        emitter.onError((ex) -> {
            logger.error("SSE error for user {}: {}", userId, ex.getMessage());
            cleanup.run();
        });
        
        // Send initial connection confirmation
        try {
            emitter.send(SseEmitter.event()
                .name("connected")
                .data("Connection established"));
        } catch (IOException e) {
            logger.error("Failed to send initial connection message to user {}", userId, e);
            cleanup.run();
        }
        
        return emitter;
    }
    
    /**
     * Send notification to specific user's SSE connections
     * This method is called by the notification service/listener
     */
    public void sendNotificationToUser(UUID userId, Object notification) {
        CopyOnWriteArrayList<SseEmitter> emitters = userEmitters.get(userId);
        if (emitters == null || emitters.isEmpty()) {
            logger.debug("No active SSE connections for user {}", userId);
            return;
        }
        
        logger.info("Sending notification to {} active connections for user {}", emitters.size(), userId);
        
        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event()
                    .name("notification")
                    .data(notification));
            } catch (IOException e) {
                logger.error("Failed to send notification to user {}, removing dead connection", userId, e);
                emitters.remove(emitter);
                if (emitters.isEmpty()) {
                    userEmitters.remove(userId);
                }
            }
        }
    }
    
    /**
     * Get count of active connections (useful for monitoring)
     */
    public int getActiveConnectionCount() {
        return userEmitters.values().stream()
            .mapToInt(CopyOnWriteArrayList::size)
            .sum();
    }
}
