package com.novel.vippro.Services;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.novel.vippro.DTO.Message.CreateMessageDTO;
import com.novel.vippro.DTO.Message.MessageDTO;
import com.novel.vippro.Security.UserDetailsImpl;

@Service
public class ChatService {
    @Autowired
    private MessageService messageService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    public MessageDTO sendToNovel(UUID novelId, CreateMessageDTO dto) {
        MessageDTO saved = messageService.createMessage(dto);
        messagingTemplate.convertAndSend("/topic/novel." + novelId, saved);
        return saved;
    }

    public MessageDTO sendToGroup(UUID groupId, CreateMessageDTO dto) {
        var groupMessage = CreateMessageDTO.builder()
                        .groupId(groupId)
                        .content(dto.content())
                        .build();
        MessageDTO saved = messageService.createMessage(groupMessage);
        messagingTemplate.convertAndSend("/topic/group." + groupId, saved);
        return saved;
    }

    public MessageDTO sendDirect(UUID receiverId, CreateMessageDTO dto) {
        var directMessage = CreateMessageDTO.builder()
                            .receiverId(receiverId)
                            .content(dto.content())
                            .build();
        MessageDTO saved = messageService.createMessage(directMessage);
        UUID currentUserId = UserDetailsImpl.getCurrentUserId();
        String channel = formatDmChannel(currentUserId, receiverId);
        messagingTemplate.convertAndSend("/topic/dm." + channel, saved);
        return saved;
    }

    private String formatDmChannel(UUID user1, UUID user2) {
        List<UUID> ids = Arrays.asList(user1, user2);
        ids.sort(Comparator.comparing(UUID::toString));
        return ids.get(0) + "." + ids.get(1);
    }

    @Transactional
    public MessageDTO sendToGroupOrDm(CreateMessageDTO dto) {
        if (dto.groupId() != null) {
            return sendToGroup(dto.groupId(), dto);
        }
        if (dto.receiverId() != null) {
            return sendDirect(dto.receiverId(), dto);
        }
        return messageService.createMessage(dto);
    }
}
