package com.skillsync.dto;

import jakarta.validation.constraints.NotNull;

public record CreateConversationRequest(
        @NotNull Long otherUserId
) {
}
