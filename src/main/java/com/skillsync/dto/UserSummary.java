package com.skillsync.dto;

public record UserSummary(
        Long id,
        String name,
        String email,
        String profileImageUrl
) {
}
