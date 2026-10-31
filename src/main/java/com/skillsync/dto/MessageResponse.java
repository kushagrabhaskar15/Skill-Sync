package com.skillsync.dto;

import java.time.LocalDateTime;

public record MessageResponse(
        Long id,
        Long conversationId,
        Long senderId,
        String senderName,
        String content,
        LocalDateTime sentAt,
        LocalDateTime editedAt
) {
}
