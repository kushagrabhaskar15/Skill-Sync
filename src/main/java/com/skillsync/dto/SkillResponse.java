package com.skillsync.dto;

import com.skillsync.enums.SkillCategory;

public record SkillResponse(
        Long id,
        String name,
        String description,
        SkillCategory category
) {
}
