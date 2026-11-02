package com.skillsync.user;

import com.skillsync.dto.UpdateProfileRequest;
import com.skillsync.dto.UserProfileResponse;
import com.skillsync.security.SecurityUtils;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    public UserProfileResponse getMyProfile() {
        return userService.getMyProfile(SecurityUtils.currentUserId());
    }

    @PutMapping("/me")
    public UserProfileResponse updateMyProfile(@Valid @RequestBody UpdateProfileRequest request) {
        return userService.updateProfile(SecurityUtils.currentUserId(), request);
    }

    @GetMapping("/search")
    public Page<UserProfileResponse> discoverUsers(
            @RequestParam(required = false) String query,
            @PageableDefault(size = 50) Pageable pageable
    ) {
        Pageable safePageable = PageRequest.of(
                pageable.getPageNumber(),
                Math.min(pageable.getPageSize(), 100),
                Sort.by(Sort.Direction.ASC, "name")
        );
        return userService.discoverUsers(SecurityUtils.currentUserId(), query, safePageable);
    }
    @GetMapping("/{id}")
    public UserProfileResponse getPublicProfile(@PathVariable Long id) {
        return userService.getPublicProfile(id);
    }

    @PostMapping("/me/deactivate")
    public void deactivate() {
        userService.deactivateAccount(SecurityUtils.currentUserId());
    }
}

