package com.novel.vippro.Services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * Service to interact with FlareSolverr API for bypassing Cloudflare protection
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class FlareSolverrService {
    
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    
    @Value("${flaresolverr.url:http://flaresolverr:8191}")
    private String flareSolverrUrl;
    
    @Value("${flaresolverr.timeout:60000}")
    private int timeout;
    
    /**
     * Fetch HTML content from a URL using FlareSolverr to bypass Cloudflare
     * 
     * @param url The URL to fetch
     * @return The HTML content
     * @throws Exception if the request fails
     */
    public String fetchHtml(String url) throws Exception {
        return fetchHtml(url, null);
    }
    
    /**
     * Fetch HTML content from a URL using FlareSolverr with optional session
     * 
     * @param url The URL to fetch
     * @param sessionId Optional session ID for reusing cookies
     * @return The HTML content
     * @throws Exception if the request fails
     */
    public String fetchHtml(String url, String sessionId) throws Exception {
        log.debug("Fetching URL via FlareSolverr: {}", url);
        
        // Prepare request payload
        Map<String, Object> payload = new HashMap<>();
        payload.put("cmd", "request.get");
        payload.put("url", url);
        payload.put("maxTimeout", timeout);
        
        if (sessionId != null && !sessionId.isEmpty()) {
            payload.put("session", sessionId);
        }
        
        // Create headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        // Create HTTP entity
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);
        
        try {
            // Send request to FlareSolverr
            ResponseEntity<String> response = restTemplate.exchange(
                flareSolverrUrl + "/v1",
                HttpMethod.POST,
                entity,
                String.class
            );
            
            if (response.getStatusCode() == HttpStatus.OK) {
                JsonNode jsonResponse = objectMapper.readTree(response.getBody());
                
                // Check if request was successful
                String status = jsonResponse.path("status").asText();
                if (!"ok".equals(status)) {
                    String message = jsonResponse.path("message").asText("Unknown error");
                    throw new Exception("FlareSolverr request failed: " + message);
                }
                
                // Extract HTML content
                String html = jsonResponse.path("solution").path("response").asText();
                
                if (html == null || html.isEmpty()) {
                    throw new Exception("Empty HTML response from FlareSolverr");
                }
                
                log.debug("Successfully fetched {} bytes of HTML content", html.length());
                return html;
                
            } else {
                throw new Exception("FlareSolverr returned status: " + response.getStatusCode());
            }
            
        } catch (Exception e) {
            log.error("Error fetching URL via FlareSolverr: {}", e.getMessage(), e);
            throw new Exception("Failed to fetch URL via FlareSolverr: " + e.getMessage(), e);
        }
    }
    
    /**
     * Create a new FlareSolverr session for reusing cookies
     * 
     * @return The session ID
     * @throws Exception if session creation fails
     */
    public String createSession() throws Exception {
        log.debug("Creating new FlareSolverr session");
        
        Map<String, Object> payload = new HashMap<>();
        payload.put("cmd", "sessions.create");
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);
        
        try {
            ResponseEntity<String> response = restTemplate.exchange(
                flareSolverrUrl + "/v1",
                HttpMethod.POST,
                entity,
                String.class
            );
            
            if (response.getStatusCode() == HttpStatus.OK) {
                JsonNode jsonResponse = objectMapper.readTree(response.getBody());
                String sessionId = jsonResponse.path("session").asText();
                log.debug("Created session: {}", sessionId);
                return sessionId;
            } else {
                throw new Exception("Failed to create session: " + response.getStatusCode());
            }
            
        } catch (Exception e) {
            log.error("Error creating FlareSolverr session: {}", e.getMessage(), e);
            throw new Exception("Failed to create FlareSolverr session: " + e.getMessage(), e);
        }
    }
    
    /**
     * Destroy a FlareSolverr session
     * 
     * @param sessionId The session ID to destroy
     * @throws Exception if session destruction fails
     */
    public void destroySession(String sessionId) throws Exception {
        log.debug("Destroying FlareSolverr session: {}", sessionId);
        
        Map<String, Object> payload = new HashMap<>();
        payload.put("cmd", "sessions.destroy");
        payload.put("session", sessionId);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);
        
        try {
            restTemplate.exchange(
                flareSolverrUrl + "/v1",
                HttpMethod.POST,
                entity,
                String.class
            );
            log.debug("Destroyed session: {}", sessionId);
            
        } catch (Exception e) {
            log.warn("Error destroying FlareSolverr session: {}", e.getMessage());
        }
    }
}
