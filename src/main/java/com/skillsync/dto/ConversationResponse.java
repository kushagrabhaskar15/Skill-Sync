package com.skillsync.dto;

import java.time.LocalDateTime;

public record ConversationResponse(
        Long id,
        UserSummary otherParticipant,
        MessageResponse lastMessage,
        long unreadCount,
        LocalDateTime updatedAt
) {
}
