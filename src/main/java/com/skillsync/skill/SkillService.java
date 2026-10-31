package com.skillsync.skill;

import com.skillsync.common.MapperUtils;
import com.skillsync.dto.SkillResponse;
import com.skillsync.dto.UserSkillRequest;
import com.skillsync.dto.UserSkillResponse;
import com.skillsync.enums.SkillType;
import com.skillsync.exception.ConflictException;
import com.skillsync.exception.ResourceNotFoundException;
import com.skillsync.user.User;
import com.skillsync.user.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SkillService {

    private final SkillRepository skillRepository;
    private final UserSkillRepository userSkillRepository;
    private final UserService userService;

    public SkillService(
            SkillRepository skillRepository,
            UserSkillRepository userSkillRepository,
            UserService userService
    ) {
        this.skillRepository = skillRepository;
        this.userSkillRepository = userSkillRepository;
        this.userService = userService;
    }

    @Transactional(readOnly = true)
    public List<SkillResponse> searchSkills(String query) {
        List<Skill> skills = (query == null || query.isBlank())
                ? skillRepository.findAll()
                : skillRepository.findByNameContainingIgnoreCase(query.trim());
        return skills.stream()
                .map(skill -> new SkillResponse(skill.getId(), skill.getName(), skill.getDescription(), skill.getCategory()))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<UserSkillResponse> getUserSkills(Long userId, SkillType type) {
        List<UserSkill> skills = type == null
                ? userSkillRepository.findByUserId(userId)
                : userSkillRepository.findByUserIdAndSkillType(userId, type);
        return skills.stream().map(MapperUtils::toUserSkillResponse).toList();
    }

    @Transactional
    public UserSkillResponse addSkill(Long userId, UserSkillRequest request) {
        User user = userService.requireUser(userId);
        Skill skill = skillRepository.findById(request.skillId())
                .orElseThrow(() -> new ResourceNotFoundException("Skill not found"));

        if (userSkillRepository.existsByUserIdAndSkillIdAndSkillType(userId, skill.getId(), request.skillType())) {
            throw new ConflictException("Skill already added for this type");
        }

        UserSkill userSkill = UserSkill.builder()
                .user(user)
                .skill(skill)
                .skillType(request.skillType())
                .proficiencyLevel(request.proficiencyLevel())
                .yearsExperience(request.yearsExperience())
                .build();

        return MapperUtils.toUserSkillResponse(userSkillRepository.save(userSkill));
    }

    @Transactional
    public UserSkillResponse updateSkill(Long userId, Long mappingId, UserSkillRequest request) {
        UserSkill userSkill = userSkillRepository.findByIdAndUserId(mappingId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("User skill mapping not found"));

        if (!userSkill.getSkill().getId().equals(request.skillId())
                || userSkill.getSkillType() != request.skillType()) {
            boolean exists = userSkillRepository.existsByUserIdAndSkillIdAndSkillType(
                    userId, request.skillId(), request.skillType());
            if (exists) {
                throw new ConflictException("Skill already added for this type");
            }
            Skill skill = skillRepository.findById(request.skillId())
                    .orElseThrow(() -> new ResourceNotFoundException("Skill not found"));
            userSkill.setSkill(skill);
            userSkill.setSkillType(request.skillType());
        }

        userSkill.setProficiencyLevel(request.proficiencyLevel());
        userSkill.setYearsExperience(request.yearsExperience());
        return MapperUtils.toUserSkillResponse(userSkill);
    }

    @Transactional
    public void removeSkill(Long userId, Long mappingId) {
        UserSkill userSkill = userSkillRepository.findByIdAndUserId(mappingId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("User skill mapping not found"));
        userSkillRepository.delete(userSkill);
    }
}
