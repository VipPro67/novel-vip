package com.novel.vippro.Config;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private static final Logger logger = LogManager.getLogger(WebSocketConfig.class);

    @Value("${spring.websocket.relay.host}")
    private String relayHost;

    @Value("${spring.websocket.relay.port}")
    private int relayPort;

    @Value("${spring.websocket.relay.username}")
    private String relayUser;

    @Value("${spring.websocket.relay.password}")
    private String relayPass;

    @Value("${spring.websocket.relay.virtual-host}")
    private String relayVHost;

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")
                .withSockJS()
                .setSessionCookieNeeded(false)
                .setWebSocketEnabled(true);
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.setApplicationDestinationPrefixes("/app");

        logger.info("Configuring STOMP broker relay with host: {}, port: {}", relayHost, relayPort);

        registry.enableStompBrokerRelay("/topic", "/queue")
                .setRelayHost(relayHost)
                .setRelayPort(relayPort)
                .setClientLogin(relayUser)
                .setClientPasscode(relayPass)
                .setSystemLogin(relayUser)
                .setSystemPasscode(relayPass)
                .setVirtualHost(relayVHost);
    }
}
