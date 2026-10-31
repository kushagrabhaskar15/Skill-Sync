package com.skillsync.common;

import com.skillsync.chat.Message;
import com.skillsync.dto.MessageResponse;
import com.skillsync.dto.UserSkillResponse;
import com.skillsync.dto.UserSummary;
import com.skillsync.skill.UserSkill;
import com.skillsync.user.User;

public final class MapperUtils {

    private MapperUtils() {
    }

    public static UserSummary toUserSummary(User user) {
        return new UserSummary(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getProfileImageUrl()
        );
    }

    public static UserSkillResponse toUserSkillResponse(UserSkill userSkill) {
        return new UserSkillResponse(
                userSkill.getId(),
                userSkill.getSkill().getId(),
                userSkill.getSkill().getName(),
                userSkill.getSkill().getCategory(),
                userSkill.getSkillType(),
                userSkill.getProficiencyLevel(),
                userSkill.getYearsExperience()
        );
    }

    public static MessageResponse toMessageResponse(Message message) {
        String content = Boolean.TRUE.equals(message.getIsDeleted()) ? "[deleted]" : message.getContent();
        return new MessageResponse(
                message.getId(),
                message.getConversation().getId(),
                message.getSender().getId(),
                message.getSender().getName(),
                content,
                message.getSentAt(),
                message.getEditedAt()
        );
    }
}
