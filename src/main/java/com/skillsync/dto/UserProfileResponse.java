package com.skillsync.dto;

import java.util.List;

public record UserProfileResponse(
        Long id,
        String name,
        String bio,
        String location,
        String profileImageUrl,
        List<UserSkillResponse> teachSkills,
        List<UserSkillResponse> learnSkills,
        List<String> interests
) {
}
