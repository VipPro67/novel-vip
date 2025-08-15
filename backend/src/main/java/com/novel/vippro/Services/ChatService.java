package com.novel.vippro.Services;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com.novel.vippro.DTO.Message.CreateMessageDTO;
import com.novel.vippro.DTO.Message.MessageDTO;
import com.novel.vippro.Models.User;

@Service
public class ChatService {
    @Autowired
    private MessageService messageService;

    @Autowired
    private UserService userService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    public MessageDTO sendToNovel(UUID novelId, CreateMessageDTO dto) {
        MessageDTO saved = messageService.createMessage(dto);
        messagingTemplate.convertAndSend("/topic/novel." + novelId, saved);
        return saved;
    }

    public MessageDTO sendToGroup(UUID groupId, CreateMessageDTO dto) {
        dto.setGroupId(groupId);
        MessageDTO saved = messageService.createMessage(dto);
        messagingTemplate.convertAndSend("/topic/group." + groupId, saved);
        return saved;
    }

    public MessageDTO sendDirect(UUID receiverId, CreateMessageDTO dto) {
        dto.setReceiverId(receiverId);
        MessageDTO saved = messageService.createMessage(dto);
        User current = userService.getCurrentUser();
        String channel = formatDmChannel(current.getId(), receiverId);
        messagingTemplate.convertAndSend("/topic/dm." + channel, saved);
        return saved;
    }

    private String formatDmChannel(UUID user1, UUID user2) {
        List<UUID> ids = Arrays.asList(user1, user2);
        ids.sort(Comparator.comparing(UUID::toString));
        return ids.get(0) + "." + ids.get(1);
    }

    public MessageDTO sendToGroupOrDm(CreateMessageDTO dto) {
        if (dto.getGroupId() != null) {
            return sendToGroup(dto.getGroupId(), dto);
        }
        if (dto.getReceiverId() != null) {
            return sendDirect(dto.getReceiverId(), dto);
        }
        return messageService.createMessage(dto);
    }
}
