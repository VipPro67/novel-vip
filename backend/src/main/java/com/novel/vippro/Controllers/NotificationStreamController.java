package com.novel.vippro.Controllers;

import com.novel.vippro.Security.UserDetailsImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@RestController
// TODO: Restrict CORS to specific origins in production for security
@CrossOrigin(origins = "*")
@RequestMapping("/api/notifications")
@PreAuthorize("isAuthenticated()")
public class NotificationStreamController {

    private static final Logger logger = LoggerFactory.getLogger(NotificationStreamController.class);
    private static final long SSE_TIMEOUT = 5 * 60 * 1000L; // 5 minutes (reduced from 30)
    private static final long HEARTBEAT_INTERVAL = 15 * 1000L; // Send heartbeat every 15 seconds
    
    // Heartbeat scheduler to keep connections alive and detect dead ones
    private final ScheduledExecutorService heartbeatScheduler = Executors.newSingleThreadScheduledExecutor();
    
    public NotificationStreamController() {
        // Start heartbeat task to send periodic keep-alive messages
        heartbeatScheduler.scheduleAtFixedRate(this::sendHeartbeats, 
            HEARTBEAT_INTERVAL, HEARTBEAT_INTERVAL, TimeUnit.MILLISECONDS);
    }
    
    private void sendHeartbeats() {
        for (Map.Entry<UUID, CopyOnWriteArrayList<SseEmitter>> entry : userEmitters.entrySet()) {
            UUID userId = entry.getKey();
            CopyOnWriteArrayList<SseEmitter> emitters = entry.getValue();
            
            for (SseEmitter emitter : emitters) {
                try {
                    emitter.send(SseEmitter.event()
                        .comment("heartbeat")); // Send comment to keep connection alive
                } catch (IOException e) {
                    logger.warn("Heartbeat failed for user {}, removing dead connection", userId);
                    emitters.remove(emitter);
                    if (emitters.isEmpty()) {
                        userEmitters.remove(userId);
                    }
                }
            }
        }
    }
    
    // Store active SSE connections per user
    private final Map<UUID, CopyOnWriteArrayList<SseEmitter>> userEmitters = new ConcurrentHashMap<>();

    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamNotifications(HttpServletResponse response) {
        UUID userId = UserDetailsImpl.getCurrentUserId();
        logger.info("Client connecting to notification stream for user: {}", userId);
        
        // Add SSE-specific headers to prevent caching and improve compatibility
        response.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache, no-transform");
        response.setHeader(HttpHeaders.CONNECTION, "keep-alive");
        response.setHeader("X-Accel-Buffering", "no"); // Disable buffering for nginx/proxies
        
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
        
        // Create a copy of the emitters list to avoid concurrent modification
        CopyOnWriteArrayList<SseEmitter> emittersCopy = new CopyOnWriteArrayList<>(emitters);
        
        for (SseEmitter emitter : emittersCopy) {
            try {
                emitter.send(SseEmitter.event()
                    .name("notification")
                    .data(notification));
            } catch (IOException e) {
                logger.error("Failed to send notification to user {}, removing dead connection", userId, e);
                // Use the original emitters list for removal (it's already thread-safe)
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
    
    /**
     * Admin endpoint to view active connections (for debugging)
     */
    @GetMapping("/connections/stats")
    public ResponseEntity<Map<String, Object>> getConnectionStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalConnections", getActiveConnectionCount());
        stats.put("uniqueUsers", userEmitters.size());
        return ResponseEntity.ok(stats);
    }
}
