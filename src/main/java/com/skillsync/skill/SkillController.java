package com.skillsync.skill;

import com.skillsync.dto.SkillResponse;
import com.skillsync.dto.UserSkillRequest;
import com.skillsync.dto.UserSkillResponse;
import com.skillsync.enums.SkillType;
import com.skillsync.security.SecurityUtils;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class SkillController {

    private final SkillService skillService;

    public SkillController(SkillService skillService) {
        this.skillService = skillService;
    }

    @GetMapping("/api/v1/skills")
    public List<SkillResponse> searchSkills(@RequestParam(required = false) String query) {
        return skillService.searchSkills(query);
    }

    @GetMapping("/api/v1/users/me/skills")
    public List<UserSkillResponse> getMySkills(@RequestParam(required = false) SkillType type) {
        return skillService.getUserSkills(SecurityUtils.currentUserId(), type);
    }

    @PostMapping("/api/v1/users/me/skills")
    @ResponseStatus(HttpStatus.CREATED)
    public UserSkillResponse addSkill(@Valid @RequestBody UserSkillRequest request) {
        return skillService.addSkill(SecurityUtils.currentUserId(), request);
    }

    @PutMapping("/api/v1/users/me/skills/{id}")
    public UserSkillResponse updateSkill(
            @PathVariable Long id,
            @Valid @RequestBody UserSkillRequest request
    ) {
        return skillService.updateSkill(SecurityUtils.currentUserId(), id, request);
    }

    @DeleteMapping("/api/v1/users/me/skills/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeSkill(@PathVariable Long id) {
        skillService.removeSkill(SecurityUtils.currentUserId(), id);
    }
}
