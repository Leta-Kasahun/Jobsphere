package com.jobsphere.jobsite.controller.job;

import com.jobsphere.jobsite.service.job.JobLikeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/jobs")
@RequiredArgsConstructor
@Tag(name = "Job Likes", description = "API for liking and unliking jobs")
public class JobLikeController {
    private final JobLikeService jobLikeService;

    @PostMapping("/{jobId}/like")
    @Operation(summary = "Toggle like for a job")
    public ResponseEntity<Map<String, Boolean>> toggleLike(@PathVariable UUID jobId) {
        boolean liked = jobLikeService.toggleLike(jobId);
        return ResponseEntity.ok(Map.of("liked", liked));
    }

    @GetMapping("/{jobId}/has-liked")
    @Operation(summary = "Check if user has liked a job")
    public ResponseEntity<Map<String, Boolean>> hasLiked(@PathVariable UUID jobId) {
        boolean liked = jobLikeService.hasLiked(jobId);
        return ResponseEntity.ok(Map.of("liked", liked));
    }

    @GetMapping("/{jobId}/like-count")
    @Operation(summary = "Get like count for a job")
    public ResponseEntity<Map<String, Long>> getLikeCount(@PathVariable UUID jobId) {
        long count = jobLikeService.getLikeCount(jobId);
        return ResponseEntity.ok(Map.of("count", count));
    }
}
