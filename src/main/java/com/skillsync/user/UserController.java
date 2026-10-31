package com.skillsync.user;

import com.skillsync.dto.UpdateProfileRequest;
import com.skillsync.dto.UserProfileResponse;
import com.skillsync.security.SecurityUtils;
import jakarta.validation.Valid;
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

    @GetMapping("/{id}")
    public UserProfileResponse getPublicProfile(@PathVariable Long id) {
        return userService.getPublicProfile(id);
    }

    @PostMapping("/me/deactivate")
    public void deactivate() {
        userService.deactivateAccount(SecurityUtils.currentUserId());
    }
}
