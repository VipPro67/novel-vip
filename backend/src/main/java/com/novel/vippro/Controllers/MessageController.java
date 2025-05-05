package com.novel.vippro.Controllers;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.novel.vippro.DTO.Message.MessageDTO;
import com.novel.vippro.Payload.Response.ControllerResponse;
import com.novel.vippro.Services.MessageService;

@RestController
@RequestMapping("/api/messages")
public class MessageController {

    @Autowired
    private MessageService messageService;

    @GetMapping
    public ControllerResponse<List<MessageDTO>> getAllMessages() {
        return ControllerResponse.success("Messages retrieved successfully", messageService.getAllMessages());
    }

    @GetMapping("/{id}")
    public ControllerResponse<MessageDTO> getMessageById(@PathVariable UUID id) {
        return ControllerResponse.success("Message retrieved successfully", messageService.getMessageById(id));
    }

    @PostMapping
    public ControllerResponse<MessageDTO> createMessage(@RequestBody MessageDTO messageDTO) {
        return ControllerResponse.success("Message created successfully", messageService.createMessage(messageDTO));
    }

    @PutMapping("/{id}")
    public ControllerResponse<MessageDTO> updateMessage(@PathVariable UUID id, @RequestBody MessageDTO messageDTO) {
        return ControllerResponse.success("Message updated successfully", messageService.updateMessage(id, messageDTO));
    }

    @DeleteMapping("/{id}")
    public ControllerResponse<Void> deleteMessage(@PathVariable UUID id) {
        messageService.deleteMessage(id);
        return ControllerResponse.success("Message deleted successfully", null);
    }
}