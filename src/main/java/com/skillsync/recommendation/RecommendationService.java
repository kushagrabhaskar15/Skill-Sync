package com.skillsync.recommendation;

import com.skillsync.common.MapperUtils;
import com.skillsync.dto.RecommendationIngestionRequest;
import com.skillsync.dto.RecommendationResponse;
import com.skillsync.enums.RecommendationStatus;
import com.skillsync.exception.BadRequestException;
import com.skillsync.exception.ResourceNotFoundException;
import com.skillsync.user.User;
import com.skillsync.user.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class RecommendationService {

    private final RecommendationRepository recommendationRepository;
    private final UserService userService;

    public RecommendationService(RecommendationRepository recommendationRepository, UserService userService) {
        this.recommendationRepository = recommendationRepository;
        this.userService = userService;
    }

    @Transactional
    public List<RecommendationResponse> ingestResults(RecommendationIngestionRequest payload) {
        User user = userService.requireUser(payload.userId());
        List<Recommendation> saved = new ArrayList<>();

        for (RecommendationIngestionRequest.RecommendationResultItem item : payload.results()) {
            if (item.recommendedUserId().equals(payload.userId())) {
                throw new BadRequestException("Cannot recommend a user to themselves");
            }
            User recommendedUser = userService.requireUser(item.recommendedUserId());

            Recommendation recommendation = Recommendation.builder()
                    .user(user)
                    .recommendedUser(recommendedUser)
                    .score(item.score())
                    .explanation(item.explanation())
                    .status(RecommendationStatus.NEW)
                    .sourceVersion(payload.sourceVersion())
                    .generatedAt(LocalDateTime.now())
                    .build();
            saved.add(recommendationRepository.save(recommendation));
        }

        return saved.stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<RecommendationResponse> getRecommendations(Long userId) {
        return recommendationRepository.findByUserIdOrderByGeneratedAtDesc(userId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public RecommendationResponse markViewed(Long userId, Long id) {
        Recommendation recommendation = requireOwned(userId, id);
        if (recommendation.getStatus() == RecommendationStatus.NEW) {
            recommendation.setStatus(RecommendationStatus.VIEWED);
        }
        return toResponse(recommendation);
    }

    @Transactional
    public RecommendationResponse dismiss(Long userId, Long id) {
        Recommendation recommendation = requireOwned(userId, id);
        recommendation.setStatus(RecommendationStatus.DISMISSED);
        return toResponse(recommendation);
    }

    private Recommendation requireOwned(Long userId, Long id) {
        return recommendationRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Recommendation not found"));
    }

    private RecommendationResponse toResponse(Recommendation recommendation) {
        return new RecommendationResponse(
                recommendation.getId(),
                MapperUtils.toUserSummary(recommendation.getRecommendedUser()),
                recommendation.getScore(),
                recommendation.getExplanation(),
                recommendation.getStatus(),
                recommendation.getGeneratedAt()
        );
    }
}
