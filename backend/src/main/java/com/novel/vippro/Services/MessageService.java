package com.novel.vippro.Services;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.Comparator;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.novel.vippro.DTO.Message.CreateMessageDTO;
import com.novel.vippro.DTO.Message.MessageDTO;
import com.novel.vippro.Mapper.Mapper;
import com.novel.vippro.Models.Message;
import com.novel.vippro.Models.User;
import com.novel.vippro.Repository.MessageRepository;
import com.novel.vippro.Payload.Response.PageResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.novel.vippro.Repository.UserRepository;
import com.novel.vippro.Security.UserDetailsImpl;

@Service
public class MessageService {
    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GroupService groupService;

    @Autowired
    private Mapper mapper;

    public PageResponse<MessageDTO> getAllMessages(Pageable pageable) {
        Page<Message> page = messageRepository.findAll(pageable);
        return new PageResponse<>(page.map(mapper::MessagetoDTO));
    }

    public MessageDTO getMessageById(UUID id) {
        Message message = messageRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Message not found"));
        return mapper.MessagetoDTO(message);
    }

    @Transactional
    public MessageDTO createMessage(CreateMessageDTO messageDTO) {
        Message message = mapper.CreateDTOtoMessage(messageDTO);
        UUID currentUserId = UserDetailsImpl.getCurrentUserId();
        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        message.setSender(user);
        if (messageDTO.groupId() == null && messageDTO.receiverId() == null) {
            throw new RuntimeException("Either groupId or receiverId must be provided");
        }
        if (messageDTO.groupId() != null) {
            message.setGroup(mapper.DTOtoGroup(groupService.getGroupById(messageDTO.groupId())));
        } else {
            User receiver = userRepository.findById(messageDTO.receiverId())
                    .orElseThrow(() -> new RuntimeException("Receiver not found"));
            message.setReceiver(receiver);
        }
        message = messageRepository.save(message);
        messageRepository.flush();
        return mapper.MessagetoDTO(message);
    }

    public List<MessageDTO> searchMessages(String content) {
        return messageRepository.findByContentContaining(content).stream()
                .map(mapper::MessagetoDTO)
                .sorted(Comparator.comparing(MessageDTO::createdAt).reversed())
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

    @Transactional(readOnly = true)
    public List<MessageDTO> getMyConversations() {
        UUID currentUserId = UserDetailsImpl.getCurrentUserId();
        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        List<Message> myConversations = new ArrayList<Message>();
        List<Message> privateConversations = getMyPrivateConversations(user);
        List<Message> groupConversations = getMyGroupConversations(user);
        myConversations.addAll(
                privateConversations.stream()
                        .filter(m -> m.getCreatedAt().equals(privateConversations.stream()
                                .filter(m2 -> Objects.equals(m.getReceiver().getId(), m2.getReceiver().getId()))))
                        .collect(Collectors.toList()));
        myConversations.addAll(
                groupConversations.stream()
                        .filter(m -> m.getCreatedAt().equals(groupConversations.stream()
                                .filter(m2 -> Objects.equals(m.getGroup().getId(), m2.getGroup().getId()))))
                        .collect(Collectors.toList()));
        // Sort by createdAt in descending order
        myConversations.sort(Comparator.comparing(Message::getCreatedAt).reversed());
        // Remove duplicates
        myConversations = myConversations.stream()
                .distinct()
                .collect(Collectors.toList());
        return myConversations.stream()
                .map(mapper::MessagetoDTO)
                .collect(Collectors.toList());
    }

    public List<Message> getMyPrivateConversations(User user) {
        return messageRepository.findBySenderOrReceiver(user.getId()).stream()
                .toList();
    }

    public List<Message> getMyGroupConversations(User user) {
        return messageRepository.findByGroupMembers(user.getId()).stream()
                .toList();
    }

    @Transactional(readOnly = true)
    public PageResponse<MessageDTO> getMessagesByReceiverOrGroup(UUID id, Pageable pageable) {
        Page<Message> page = messageRepository.getMessagesByReceiverOrGroup(id, pageable);
        return new PageResponse<>(page.map(mapper::MessagetoDTO));
    }
}