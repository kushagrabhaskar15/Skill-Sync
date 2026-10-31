package com.skillsync.skill;

import com.skillsync.enums.SkillCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SkillRepository extends JpaRepository<Skill, Long> {

    Optional<Skill> findByNameIgnoreCase(String name);

    List<Skill> findByCategory(SkillCategory category);

    List<Skill> findByNameContainingIgnoreCase(String query);
}
