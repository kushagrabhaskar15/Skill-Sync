package com.skillsync.skill;

import com.skillsync.enums.SkillType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserSkillRepository extends JpaRepository<UserSkill, Long> {

    List<UserSkill> findByUserIdAndSkillType(Long userId, SkillType skillType);

    List<UserSkill> findByUserId(Long userId);

    boolean existsByUserIdAndSkillIdAndSkillType(Long userId, Long skillId, SkillType skillType);

    Optional<UserSkill> findByIdAndUserId(Long id, Long userId);
}
