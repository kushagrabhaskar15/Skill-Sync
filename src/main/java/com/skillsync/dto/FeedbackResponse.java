package com.skillsync.dto;

import java.time.LocalDateTime;

public record FeedbackResponse(
        Long id,
        Long recommendationId,
        Integer rating,
        String feedbackText,
        LocalDateTime createdAt
) {
}
