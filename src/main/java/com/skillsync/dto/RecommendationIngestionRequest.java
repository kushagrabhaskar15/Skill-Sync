package com.skillsync.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.List;

public record RecommendationIngestionRequest(
        @NotNull Long userId,
        String sourceVersion,
        @NotEmpty List<@Valid RecommendationResultItem> results
) {
    public record RecommendationResultItem(
            @NotNull Long recommendedUserId,
            @NotNull @DecimalMin("0.0000") @DecimalMax("1.0000") BigDecimal score,
            String explanation
    ) {
    }
}
