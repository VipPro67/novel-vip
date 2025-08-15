package com.novel.vippro.Controllers;

import com.novel.vippro.Config.RabbitMQConfig;
import com.novel.vippro.DTO.Comment.CommentDTO;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
public class CommentSocketController {

    private final SimpMessagingTemplate messagingTemplate;

    public CommentSocketController(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @RabbitListener(queues = RabbitMQConfig.COMMENT_QUEUE)
    public void handleNewComment(CommentDTO comment) {
        if (comment.getChapterId() != null) {
            messagingTemplate.convertAndSend("/topic/chapter." + comment.getChapterId(), comment);
        }
        // Novel notifications could be added similarly
    }
}
