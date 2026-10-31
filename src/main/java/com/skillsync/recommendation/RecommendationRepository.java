package com.skillsync.recommendation;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RecommendationRepository extends JpaRepository<Recommendation, Long> {

    List<Recommendation> findByUserIdOrderByGeneratedAtDesc(Long userId);

    Optional<Recommendation> findByIdAndUserId(Long id, Long userId);
}
