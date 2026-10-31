package com.skillsync.feedback;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FeedbackRepository extends JpaRepository<RecommendationFeedback, Long> {

    List<RecommendationFeedback> findByRecommendationId(Long recommendationId);
}
