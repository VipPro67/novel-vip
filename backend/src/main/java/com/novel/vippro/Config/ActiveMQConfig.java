package com.novel.vippro.Config;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.ActiveMQSslConnectionFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.support.converter.MappingJackson2MessageConverter;
import org.springframework.jms.support.converter.MessageType;

@Configuration
@EnableJms
@ConditionalOnProperty(name = "app.messaging.provider", havingValue = "activemq")
public class ActiveMQConfig {

    @Value("${spring.activemq.broker-url}")
    private String brokerUrl;

    @Value("${spring.activemq.user:}")
    private String defaultUser;

    @Value("${spring.activemq.password:}")
    private String defaultPassword;

    @Value("${app.activemq.trust-store:}")
    private String defaultTrustStore;

    @Value("${app.activemq.trust-store-password:}")
    private String defaultTrustStorePassword;

    @Value("${app.activemq.key-store:}")
    private String defaultKeyStore;

    @Value("${app.activemq.key-store-password:}")
    private String defaultKeyStorePassword;

    @Bean
    public ActiveMQConnectionFactory connectionFactory() {
        String resolvedBrokerUrl = resolveEnv("SPRING_ACTIVEMQ_BROKER_URL", brokerUrl);
        String resolvedUser = resolveEnv("SPRING_ACTIVEMQ_USER", defaultUser);
        String resolvedPassword = resolveEnv("SPRING_ACTIVEMQ_PASSWORD", defaultPassword);
        String resolvedTrustStore = resolveEnv("SPRING_ACTIVEMQ_TRUSTSTORE", defaultTrustStore);
        String resolvedTrustStorePassword = resolveEnv("SPRING_ACTIVEMQ_TRUSTSTORE_PASSWORD", defaultTrustStorePassword);
        String resolvedKeyStore = resolveEnv("SPRING_ACTIVEMQ_KEYSTORE", defaultKeyStore);
        String resolvedKeyStorePassword = resolveEnv("SPRING_ACTIVEMQ_KEYSTORE_PASSWORD", defaultKeyStorePassword);

        ActiveMQConnectionFactory factory;
        if (resolvedBrokerUrl != null && resolvedBrokerUrl.startsWith("ssl")) {
            try {
                ActiveMQSslConnectionFactory sslFactory = new ActiveMQSslConnectionFactory(resolvedBrokerUrl);
                if (hasText(resolvedTrustStore)) {
                    sslFactory.setTrustStore(resolvedTrustStore);
                }
                if (hasText(resolvedTrustStorePassword)) {
                    sslFactory.setTrustStorePassword(resolvedTrustStorePassword);
                }
                if (hasText(resolvedKeyStore)) {
                    sslFactory.setKeyStore(resolvedKeyStore);
                }
                if (hasText(resolvedKeyStorePassword)) {
                    sslFactory.setKeyStorePassword(resolvedKeyStorePassword);
                }
                factory = sslFactory;
            } catch (Exception ex) {
                throw new IllegalStateException("Failed to configure ActiveMQ SSL connection", ex);
            }
        } else {
            factory = new ActiveMQConnectionFactory(resolvedBrokerUrl);
        }

        if (hasText(resolvedUser)) {
            factory.setUserName(resolvedUser);
        }
        if (hasText(resolvedPassword)) {
            factory.setPassword(resolvedPassword);
        }
        factory.setTrustAllPackages(true);
        return factory;
    }

    @Bean
    public DefaultJmsListenerContainerFactory jmsListenerContainerFactory(ActiveMQConnectionFactory connectionFactory,
            MappingJackson2MessageConverter converter) {
        DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setPubSubDomain(false); // true = topics, false = queues
        factory.setMessageConverter(converter);
        return factory;
    }

    private static String resolveEnv(String key, String fallback) {
        String value = System.getenv(key);
        if (value != null && !value.isBlank()) {
            return value;
        }
        return fallback;
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
