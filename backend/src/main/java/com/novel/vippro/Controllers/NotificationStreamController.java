package com.novel.vippro.Controllers;

import com.novel.vippro.Security.UserDetailsImpl;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/notifications")
@PreAuthorize("isAuthenticated()")
public class NotificationStreamController {

    private static final Logger logger = LoggerFactory.getLogger(NotificationStreamController.class);
    private static final long SSE_TIMEOUT = 5 * 60 * 1000L;
    private static final long HEARTBEAT_INTERVAL = 15 * 1000L;

    private final Map<UUID, SseEmitter> userEmitters = new ConcurrentHashMap<>();
    private final ScheduledExecutorService heartbeatScheduler = Executors.newSingleThreadScheduledExecutor();

    public NotificationStreamController() {
        heartbeatScheduler.scheduleAtFixedRate(
                this::sendHeartbeats,
                HEARTBEAT_INTERVAL,
                HEARTBEAT_INTERVAL,
                TimeUnit.MILLISECONDS);
    }

    private void sendHeartbeats() {
        for (Map.Entry<UUID, SseEmitter> entry : userEmitters.entrySet()) {
            try {
                entry.getValue().send(SseEmitter.event().comment("heartbeat"));
            } catch (IOException e) {
                userEmitters.remove(entry.getKey(), entry.getValue());
            }
        }
    }

    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamNotifications(HttpServletResponse response) {
        UUID userId = UserDetailsImpl.getCurrentUserId();

        response.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache, no-transform");
        response.setHeader(HttpHeaders.CONNECTION, "keep-alive");
        response.setHeader("X-Accel-Buffering", "no");

        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT);

        SseEmitter oldEmitter = userEmitters.put(userId, emitter);
        if (oldEmitter != null) {
            oldEmitter.complete();
        }

        Runnable cleanup = () -> userEmitters.remove(userId, emitter);

        emitter.onCompletion(cleanup);
        emitter.onTimeout(cleanup);
        emitter.onError(e -> cleanup.run());

        try {
            emitter.send(SseEmitter.event().name("connected").data("Connection established"));
        } catch (IOException e) {
            cleanup.run();
        }

        return emitter;
    }

    public void sendNotificationToUser(UUID userId, Object notification) {
        SseEmitter emitter = userEmitters.get(userId);
        if (emitter == null)
            return;

        try {
            emitter.send(SseEmitter.event().name("notification").data(notification));
        } catch (IOException e) {
            userEmitters.remove(userId, emitter);
        }
    }

    public int getActiveConnectionCount() {
        return userEmitters.size();
    }

    @GetMapping("/connections/stats")
    public ResponseEntity<Map<String, Object>> getConnectionStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalConnections", userEmitters.size());
        stats.put("uniqueUsers", userEmitters.size());
        return ResponseEntity.ok(stats);
    }
}
