package com.jobsphere.jobsite.controller.job;

import com.jobsphere.jobsite.dto.job.JobResponse;
import com.jobsphere.jobsite.service.job.SavedJobService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/jobs/saved")
@RequiredArgsConstructor
public class SavedJobController {
    private final SavedJobService savedJobService;

    @PostMapping("/{jobId}")
    public ResponseEntity<Map<String, Object>> toggleSave(@PathVariable UUID jobId) {
        boolean saved = savedJobService.toggleSave(jobId);
        return ResponseEntity.ok(Map.of(
                "saved", saved,
                "message", saved ? "Job saved successfully" : "Job removed from saved"));
    }

    @GetMapping("/check/{jobId}")
    public ResponseEntity<Map<String, Boolean>> isSaved(@PathVariable UUID jobId) {
        return ResponseEntity.ok(Map.of("isSaved", savedJobService.isSaved(jobId)));
    }

    @GetMapping
    public ResponseEntity<Page<JobResponse>> getSavedJobs(
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(savedJobService.getSavedJobsAsResponses(pageable));
    }
}
