package com.skillsync.chat;

import com.skillsync.dto.*;
import com.skillsync.security.SecurityUtils;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/chat")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @GetMapping("/conversations")
    public List<ConversationResponse> getConversations() {
        return chatService.getConversations(SecurityUtils.currentUserId());
    }

    @PostMapping("/conversations")
    @ResponseStatus(HttpStatus.CREATED)
    public ConversationResponse createConversation(@Valid @RequestBody CreateConversationRequest request) {
        return chatService.createConversation(SecurityUtils.currentUserId(), request.otherUserId());
    }

    @GetMapping("/conversations/{id}/messages")
    public Page<MessageResponse> getMessages(
            @PathVariable Long id,
            @PageableDefault(size = 50) Pageable pageable
    ) {
        return chatService.getMessages(SecurityUtils.currentUserId(), id, pageable);
    }

    @PostMapping("/conversations/{id}/messages")
    @ResponseStatus(HttpStatus.CREATED)
    public MessageResponse sendMessage(
            @PathVariable Long id,
            @Valid @RequestBody SendMessageRequest request
    ) {
        return chatService.sendMessage(SecurityUtils.currentUserId(), id, request);
    }

    @PatchMapping("/conversations/{id}/read")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void markRead(@PathVariable Long id) {
        chatService.markRead(SecurityUtils.currentUserId(), id);
    }
}
