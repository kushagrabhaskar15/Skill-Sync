package com.skillsync.dto;

import jakarta.validation.constraints.Size;

import java.util.List;

public record UpdateProfileRequest(
        @Size(max = 100) String name,
        @Size(max = 2000) String bio,
        @Size(max = 255) String location,
        @Size(max = 512) String profileImageUrl,
        List<@Size(max = 100) String> interests
) {
}
