package com.skillsync.dto;

import com.skillsync.enums.RecommendationStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record RecommendationResponse(
        Long id,
        UserSummary recommendedUser,
        BigDecimal score,
        String explanation,
        RecommendationStatus status,
        LocalDateTime generatedAt
) {
}
