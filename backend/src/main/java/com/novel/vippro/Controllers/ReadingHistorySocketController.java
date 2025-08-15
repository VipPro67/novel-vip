package com.novel.vippro.Controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import com.novel.vippro.Events.ReadingProgressEvent;

@Controller
public class ReadingHistorySocketController {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @EventListener
    public void handleReadingProgress(ReadingProgressEvent event) {
        messagingTemplate.convertAndSend("/topic/user." + event.getUserId(), event.getProgress());
    }
}
