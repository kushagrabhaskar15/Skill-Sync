package com.skillsync.dto;

import com.skillsync.enums.ProficiencyLevel;
import com.skillsync.enums.SkillCategory;
import com.skillsync.enums.SkillType;

public record UserSkillResponse(
        Long id,
        Long skillId,
        String skillName,
        SkillCategory category,
        SkillType skillType,
        ProficiencyLevel proficiencyLevel,
        Integer yearsExperience
) {
}
