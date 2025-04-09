package com.novel.vippro.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SupabaseConfig {

    @Value("${supabase.webhook.secret:}")
    private String webhookSecret;

    public String getWebhookSecret() {
        return webhookSecret;
    }
} 