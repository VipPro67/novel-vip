package com.novel.vippro.Controllers;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.novel.vippro.DTO.Message.CreateMessageDTO;
import com.novel.vippro.DTO.Message.MessageDTO;
import com.novel.vippro.Payload.Response.ControllerResponse;
import com.novel.vippro.Services.MessageService;
import com.novel.vippro.Payload.Response.PageResponse;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import com.novel.vippro.Services.ChatService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/messages")
@CrossOrigin(origins = "*", maxAge = 3600)
@Tag(name = "Message", description = "Message API")
@SecurityRequirement(name = "bearerAuth")
public class MessageController {

    @Autowired
    private MessageService messageService;

    @Autowired
    private ChatService chatService;

    @Operation(summary = "Get all messages")
    @GetMapping
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ControllerResponse<PageResponse<MessageDTO>> getAllMessages(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(sortDir), sortBy));
        return ControllerResponse.success("Messages retrieved successfully", messageService.getAllMessages(pageable));
    }

    @Operation(summary = "Search messages by content")
    @GetMapping("/search")
    @PreAuthorize("isAuthenticated()")
    public ControllerResponse<List<MessageDTO>> searchMessages(@RequestParam String content) {
        return ControllerResponse.success("Messages retrieved successfully", messageService.searchMessages(content));
    }

    @Operation(summary = "Get my conversations")
    @GetMapping("/my-conversations")
    @PreAuthorize("isAuthenticated()")
    public ControllerResponse<List<MessageDTO>> getMyConversations() {
        return ControllerResponse.success("Conversations retrieved successfully", messageService.getMyConversations());
    }

    @Operation(summary = "Get all messages by rereiver or group id")
    @GetMapping("/by-receiver-or-group/{id}")
    @PreAuthorize("isAuthenticated()")
    public ControllerResponse<PageResponse<MessageDTO>> getMessagesByReceiverOrGroup(@PathVariable UUID id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(sortDir), sortBy));
        return ControllerResponse.success("Messages retrieved successfully",
                messageService.getMessagesByReceiverOrGroup(id, pageable));
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ControllerResponse<MessageDTO> createMessage(@RequestBody CreateMessageDTO messageDTO) {
        return ControllerResponse.success("Message created successfully", chatService.sendToGroupOrDm(messageDTO));
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ControllerResponse<MessageDTO> updateMessage(@PathVariable UUID id, @RequestBody MessageDTO messageDTO) {
        return ControllerResponse.success("Message updated successfully", messageService.updateMessage(id, messageDTO));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ControllerResponse<Void> deleteMessage(@PathVariable UUID id) {
        messageService.deleteMessage(id);
        return ControllerResponse.success("Message deleted successfully", null);
    }
}