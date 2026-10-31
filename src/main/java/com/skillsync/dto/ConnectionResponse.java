package com.skillsync.dto;

import com.skillsync.enums.ConnectionStatus;

import java.time.LocalDateTime;

public record ConnectionResponse(
        Long id,
        UserSummary requester,
        UserSummary receiver,
        ConnectionStatus status,
        String message,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
