package com.novel.vippro.Services;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.Comparator;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.novel.vippro.DTO.Message.CreateMessageDTO;
import com.novel.vippro.DTO.Message.MessageDTO;
import com.novel.vippro.Mapper.Mapper;
import com.novel.vippro.Models.Message;
import com.novel.vippro.Models.User;
import com.novel.vippro.Repository.MessageRepository;

@Service
public class MessageService {
    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private GroupService groupService;

    @Autowired
    private Mapper mapper;

    public List<MessageDTO> getAllMessages() {
        return messageRepository.findAll().stream()
                .map(mapper::MessagetoDTO)
                .toList();
    }

    public MessageDTO getMessageById(UUID id) {
        Message message = messageRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Message not found"));
        return mapper.MessagetoDTO(message);
    }

    public MessageDTO createMessage(CreateMessageDTO messageDTO) {
        Message message = mapper.CreateDTOtoMessage(messageDTO);
        User user = userService.getCurrentUser();
        message.setSender(user);
        if (messageDTO.getGroupId() == null && messageDTO.getReceiverId() == null) {
            throw new RuntimeException("Either groupId or receiverId must be provided");
        }
        if (messageDTO.getGroupId() != null) {
            message.setGroup(mapper.DTOtoGroup(groupService.getGroupById(messageDTO.getGroupId())));
        } else {
            message.setReceiver(userService.getUserById(messageDTO.getReceiverId()));
        }
        message = messageRepository.save(message);
        messageRepository.flush();
        return mapper.MessagetoDTO(message);
    }

    public List<MessageDTO> searchMessages(String content) {
        return messageRepository.findByContentContaining(content).stream()
                .map(mapper::MessagetoDTO)
                .sorted(Comparator.comparing(MessageDTO::getCreatedAt).reversed())
                .collect(Collectors.toList());
    }

    public MessageDTO updateMessage(UUID id, MessageDTO messageDTO) {
        Message existingMessage = messageRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Message not found"));
        mapper.updateMessageFromDTO(messageDTO, existingMessage);
        existingMessage = messageRepository.save(existingMessage);
        return mapper.MessagetoDTO(existingMessage);
    }

    public void deleteMessage(UUID id) {
        messageRepository.deleteById(id);
    }

    public List<MessageDTO> getMyConversations() {
        User user = userService.getCurrentUser();
        List<MessageDTO> myConversations = new ArrayList<MessageDTO>();
        List<MessageDTO> privateConversations = getMyPrivateConversations(user);
        List<MessageDTO> groupConversations = getMyGroupConversations(user);
        myConversations.addAll(
                privateConversations.stream()
                        .filter(m -> m.getCreatedAt().equals(privateConversations.stream()
                                .filter(m2 -> Objects.equals(m.getReceiverId(), m2.getReceiverId()))
                                .map(MessageDTO::getCreatedAt)
                                .max(Comparator.naturalOrder())
                                .orElse(null)))
                        .collect(Collectors.toList()));
        myConversations.addAll(
                groupConversations.stream()
                        .filter(m -> m.getCreatedAt().equals(groupConversations.stream()
                                .filter(m2 -> Objects.equals(m.getGroupId(), m2.getGroupId()))
                                .map(MessageDTO::getCreatedAt)
                                .max(Comparator.naturalOrder())
                                .orElse(null)))
                        .collect(Collectors.toList()));
        // Sort by createdAt in descending order
        myConversations.sort(Comparator.comparing(MessageDTO::getCreatedAt).reversed());
        // Remove duplicates
        myConversations = myConversations.stream()
                .distinct()
                .collect(Collectors.toList());
        return myConversations;
    }

    public List<MessageDTO> getMyPrivateConversations(User user) {
        return messageRepository.findBySenderOrReceiver(user.getId()).stream()
                .map(mapper::MessagetoDTO)
                .toList();
    }

    public List<MessageDTO> getMyGroupConversations(User user) {
        return messageRepository.findByGroupMembers(user.getId()).stream()
                .map(mapper::MessagetoDTO)
                .toList();
    }

    public List<MessageDTO> getMessagesByReceiverOrGroup(UUID id) {
        List<MessageDTO> messages = new ArrayList<>();
        messages.addAll(messageRepository.findBySenderOrReceiver(id).stream()
                .map(mapper::MessagetoDTO)
                .toList());
        messages.addAll(messageRepository.findByGroupMembers(id).stream()
                .map(mapper::MessagetoDTO)
                .toList());
        messages.sort(Comparator.comparing(MessageDTO::getCreatedAt).reversed());
        return messages;

    }
}