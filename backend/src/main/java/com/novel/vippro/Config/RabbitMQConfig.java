package com.novel.vippro.Config;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.novel.vippro.Messaging.MessageQueues;

@Configuration
@ConditionalOnProperty(name = "app.messaging.provider", havingValue = "rabbitmq", matchIfMissing = true)
public class RabbitMQConfig {

    @Bean
    public Queue notificationsQueue() {
        return new Queue(MessageQueues.NOTIFICATION, true, false, false);
    }

    @Bean
    public Queue commentsQueue() {
        return new Queue(MessageQueues.COMMENT, true, false, false);
    }

    @Bean
    public Queue epubUploadQueue() {
        return new Queue(MessageQueues.EPUB_UPLOAD, true, false, false);
    }

    @Bean
    public Queue chapterAudioQueue() {
        return new Queue(MessageQueues.CHAPTER_AUDIO, true, false, false);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
            return new Jackson2JsonMessageConverter();
    }

	@Bean
	public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, MessageConverter converter) {
		RabbitTemplate template = new RabbitTemplate(connectionFactory);
		template.setMessageConverter(converter);
		return template;
	}
}
