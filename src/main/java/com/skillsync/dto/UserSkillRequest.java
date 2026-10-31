package com.skillsync.dto;

import com.skillsync.enums.ProficiencyLevel;
import com.skillsync.enums.SkillType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record UserSkillRequest(
        @NotNull Long skillId,
        @NotNull SkillType skillType,
        @NotNull ProficiencyLevel proficiencyLevel,
        @PositiveOrZero Integer yearsExperience
) {
}
