package com.skillsync.feedback;

import com.skillsync.dto.FeedbackRequest;
import com.skillsync.dto.FeedbackResponse;
import com.skillsync.security.SecurityUtils;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/recommendations")
public class FeedbackController {

    private final FeedbackService feedbackService;

    public FeedbackController(FeedbackService feedbackService) {
        this.feedbackService = feedbackService;
    }

    @PostMapping("/{id}/feedback")
    @ResponseStatus(HttpStatus.CREATED)
    public FeedbackResponse submitFeedback(
            @PathVariable Long id,
            @Valid @RequestBody FeedbackRequest request
    ) {
        return feedbackService.submitFeedback(SecurityUtils.currentUserId(), id, request);
    }
}
