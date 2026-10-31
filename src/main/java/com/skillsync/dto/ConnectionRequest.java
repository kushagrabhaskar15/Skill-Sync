package com.skillsync.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ConnectionRequest(
        @NotNull Long receiverId,
        @Size(max = 500) String message
) {
}
