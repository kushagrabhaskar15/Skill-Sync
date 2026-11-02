package com.skillsync.recommendation;

import com.skillsync.common.MapperUtils;
import com.skillsync.dto.RecommendationIngestionRequest;
import com.skillsync.dto.RecommendationResponse;
import com.skillsync.enums.RecommendationStatus;
import com.skillsync.exception.BadRequestException;
import com.skillsync.exception.ResourceNotFoundException;
import com.skillsync.user.User;
import com.skillsync.user.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class RecommendationService {

    private final RecommendationRepository recommendationRepository;
    private final UserService userService;
    private final RestClient aiRecommendationClient;
    private final TransactionTemplate transactionTemplate;
    private static final Logger log = LoggerFactory.getLogger(RecommendationService.class);

    public RecommendationService(
            RecommendationRepository recommendationRepository,
            UserService userService,
            @Value("${skillsync.ai.service-url:http://localhost:8001}") String aiServiceUrl
,
            PlatformTransactionManager transactionManager
    ) {
        this.recommendationRepository = recommendationRepository;
        this.userService = userService;
        this.aiRecommendationClient = RestClient.builder().baseUrl(aiServiceUrl).build();
        this.transactionTemplate = new TransactionTemplate(transactionManager);
        this.transactionTemplate.setReadOnly(true);
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

    public List<RecommendationResponse> getRecommendations(Long userId) {
        List<Recommendation> existing = recommendationRepository.findByUserIdOrderByGeneratedAtDesc(userId);
        boolean hasVisibleRecommendations = existing.stream()
                .anyMatch(item -> item.getStatus() != RecommendationStatus.DISMISSED);
        if (!hasVisibleRecommendations) {
            try {
                aiRecommendationClient.post()
                        .uri("/api/v1/recommendations/generate/{userId}", userId)
                        .retrieve()
                        .toBodilessEntity();
            } catch (RestClientException exception) {
                log.warn("AI recommendation generation is unavailable for user {}", userId);
            }
        }
        return transactionTemplate.execute(status -> recommendationRepository.findByUserIdOrderByGeneratedAtDesc(userId).stream().map(this::toResponse).toList());
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



