package com.novel.vippro.Config;

import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableRabbit
public class RabbitMQConfig {
public static final String NOTIFICATION_QUEUE = "notifications";
}

