package com.novel.vippro.controllers;

import com.novel.vippro.config.SupabaseConfig;
import com.novel.vippro.models.User;
import com.novel.vippro.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.Console;
import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/webhooks")
@CrossOrigin(origins = "*", maxAge = 3600)
public class SupabaseWebhookController {

    @Autowired
    private UserService userService;

    @Autowired
    private SupabaseConfig supabaseConfig;

    @PostMapping("/supabase/auth")
    public ResponseEntity<?> handleSupabaseAuthWebhook(
            @RequestHeader(value = "X-Supabase-Webhook-Secret", required = false) String webhookSecret,
            @RequestBody Map<String, Object> payload) {

        // Verify webhook secret if configured
        if (supabaseConfig.getWebhookSecret() != null && !supabaseConfig.getWebhookSecret().isEmpty()) {
            if (webhookSecret == null || !webhookSecret.equals(supabaseConfig.getWebhookSecret())) {
                return ResponseEntity.status(401).body(Map.of("error", "Invalid webhook secret"));
            }
        }

        try {
            // Extract the event type and data from the payload
            String eventType = (String) payload.get("type");
            Map<String, Object> record = (Map<String, Object>) payload.get("record");
            // Handle user creation event
            if ("INSERT".equals(eventType)) {
                String supabaseId = (String) record.get("id");
                String email = (String) record.get("email");

                // Check if user already exists
                if (!userService.existsBySupabaseId(supabaseId)) {
                    // Create a new user
                    User user = new User();
                    user.setSupabaseId(supabaseId);
                    user.setEmail(email);
                    user.setUsername(email.split("@")[0]); // Use part of email as username
                    user.setPassword(""); // No password needed for Supabase auth
                    user.setCreatedAt(LocalDateTime.now());
                    user.setUpdatedAt(LocalDateTime.now());

                    // Save the user
                    userService.save(user);

                    return ResponseEntity.ok().body(Map.of(
                            "message", "User created successfully",
                            "userId", user.getId()));
                } else {
                    return ResponseEntity.ok().body(Map.of(
                            "message", "User already exists",
                            "supabaseId", supabaseId));
                }
            }

            return ResponseEntity.ok().body(Map.of("message", "Event processed"));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}