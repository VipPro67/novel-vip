package com.novel.vippro.Services;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.novel.vippro.DTO.Message.MessageDTO;
import com.novel.vippro.Mapper.Mapper;
import com.novel.vippro.Models.Message;
import com.novel.vippro.Repository.MessageRepository;

@Service
public class MessageService {
    @Autowired
    private MessageRepository messageRepository;

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

    public MessageDTO createMessage(MessageDTO messageDTO) {
        Message message = mapper.DTOtoMessage(messageDTO);
        message = messageRepository.save(message);
        return mapper.MessagetoDTO(message);
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
}