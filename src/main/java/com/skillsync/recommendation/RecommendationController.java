package com.skillsync.recommendation;

import com.skillsync.dto.RecommendationResponse;
import com.skillsync.security.SecurityUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/recommendations")
public class RecommendationController {

    private final RecommendationService recommendationService;

    public RecommendationController(RecommendationService recommendationService) {
        this.recommendationService = recommendationService;
    }

    @GetMapping
    public List<RecommendationResponse> getRecommendations() {
        return recommendationService.getRecommendations(SecurityUtils.currentUserId());
    }

    @PatchMapping("/{id}/viewed")
    public RecommendationResponse markViewed(@PathVariable Long id) {
        return recommendationService.markViewed(SecurityUtils.currentUserId(), id);
    }

    @PatchMapping("/{id}/dismiss")
    public RecommendationResponse dismiss(@PathVariable Long id) {
        return recommendationService.dismiss(SecurityUtils.currentUserId(), id);
    }
}
