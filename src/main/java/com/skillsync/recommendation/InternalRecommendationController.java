package com.skillsync.recommendation;

import com.skillsync.dto.RecommendationIngestionRequest;
import com.skillsync.dto.RecommendationResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/internal/recommendations")
public class InternalRecommendationController {

    private final RecommendationService recommendationService;

    public InternalRecommendationController(RecommendationService recommendationService) {
        this.recommendationService = recommendationService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public List<RecommendationResponse> ingest(@Valid @RequestBody RecommendationIngestionRequest request) {
        return recommendationService.ingestResults(request);
    }
}
