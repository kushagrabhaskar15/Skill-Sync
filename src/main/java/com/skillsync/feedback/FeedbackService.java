package com.skillsync.feedback;

import com.skillsync.dto.FeedbackRequest;
import com.skillsync.dto.FeedbackResponse;
import com.skillsync.exception.ResourceNotFoundException;
import com.skillsync.recommendation.Recommendation;
import com.skillsync.recommendation.RecommendationRepository;
import com.skillsync.user.User;
import com.skillsync.user.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FeedbackService {

    private final FeedbackRepository feedbackRepository;
    private final RecommendationRepository recommendationRepository;
    private final UserService userService;

    public FeedbackService(
            FeedbackRepository feedbackRepository,
            RecommendationRepository recommendationRepository,
            UserService userService
    ) {
        this.feedbackRepository = feedbackRepository;
        this.recommendationRepository = recommendationRepository;
        this.userService = userService;
    }

    @Transactional
    public FeedbackResponse submitFeedback(Long userId, Long recommendationId, FeedbackRequest request) {
        Recommendation recommendation = recommendationRepository.findByIdAndUserId(recommendationId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Recommendation not found"));
        User user = userService.requireUser(userId);

        RecommendationFeedback feedback = RecommendationFeedback.builder()
                .recommendation(recommendation)
                .user(user)
                .rating(request.rating())
                .feedbackText(request.feedbackText())
                .build();

        feedbackRepository.save(feedback);

        return new FeedbackResponse(
                feedback.getId(),
                recommendation.getId(),
                feedback.getRating(),
                feedback.getFeedbackText(),
                feedback.getCreatedAt()
        );
    }
}
