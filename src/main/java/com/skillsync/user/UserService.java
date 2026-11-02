package com.skillsync.user;

import com.skillsync.common.MapperUtils;
import com.skillsync.dto.UpdateProfileRequest;
import com.skillsync.dto.UserProfileResponse;
import com.skillsync.dto.UserSkillResponse;
import com.skillsync.enums.SkillType;
import com.skillsync.enums.UserStatus;
import com.skillsync.exception.ResourceNotFoundException;
import com.skillsync.skill.UserSkill;
import com.skillsync.skill.UserSkillRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final UserSkillRepository userSkillRepository;
    private final UserInterestRepository userInterestRepository;

    public UserService(
            UserRepository userRepository,
            UserSkillRepository userSkillRepository,
            UserInterestRepository userInterestRepository
    ) {
        this.userRepository = userRepository;
        this.userSkillRepository = userSkillRepository;
        this.userInterestRepository = userInterestRepository;
    }

    @Transactional(readOnly = true)
    public Page<UserProfileResponse> discoverUsers(Long currentUserId, String query, Pageable pageable) {
        requireUser(currentUserId);
        String searchTerm = query == null ? "" : query.trim();
        Page<User> users = searchTerm.isEmpty()
                ? userRepository.findByStatusAndIdNot(UserStatus.ACTIVE, currentUserId, pageable)
                : userRepository.findByStatusAndIdNotAndNameContainingIgnoreCase(
                        UserStatus.ACTIVE, currentUserId, searchTerm, pageable);
        return users.map(this::buildProfile);
    }
    @Transactional(readOnly = true)
    public UserProfileResponse getMyProfile(Long userId) {
        return buildProfile(requireUser(userId));
    }

    @Transactional(readOnly = true)
    public UserProfileResponse getPublicProfile(Long userId) {
        User user = requireUser(userId);
        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new ResourceNotFoundException("User not found");
        }
        return buildProfile(user);
    }

    @Transactional
    public UserProfileResponse updateProfile(Long userId, UpdateProfileRequest request) {
        User user = requireUser(userId);

        if (request.name() != null && !request.name().isBlank()) {
            user.setName(request.name().trim());
        }
        if (request.bio() != null) {
            user.setBio(request.bio());
        }
        if (request.location() != null) {
            user.setLocation(request.location());
        }
        if (request.profileImageUrl() != null) {
            user.setProfileImageUrl(request.profileImageUrl());
        }

        if (request.interests() != null) {
            userInterestRepository.deleteByUserId(userId);
            for (String interest : request.interests()) {
                if (interest != null && !interest.isBlank()) {
                    userInterestRepository.save(UserInterest.builder()
                            .user(user)
                            .interestName(interest.trim())
                            .build());
                }
            }
        }

        return buildProfile(user);
    }

    @Transactional
    public void deactivateAccount(Long userId) {
        User user = requireUser(userId);
        user.setStatus(UserStatus.DEACTIVATED);
    }

    public User requireUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private UserProfileResponse buildProfile(User user) {
        List<UserSkill> skills = userSkillRepository.findByUserId(user.getId());
        List<UserSkillResponse> teachSkills = skills.stream()
                .filter(s -> s.getSkillType() == SkillType.TEACH)
                .map(MapperUtils::toUserSkillResponse)
                .toList();
        List<UserSkillResponse> learnSkills = skills.stream()
                .filter(s -> s.getSkillType() == SkillType.LEARN)
                .map(MapperUtils::toUserSkillResponse)
                .toList();
        List<String> interests = userInterestRepository.findByUserId(user.getId()).stream()
                .map(UserInterest::getInterestName)
                .toList();

        return new UserProfileResponse(
                user.getId(),
                user.getName(),
                user.getBio(),
                user.getLocation(),
                user.getProfileImageUrl(),
                teachSkills,
                learnSkills,
                interests
        );
    }
}


