package com.jobsphere.jobsite.controller.job;

import com.jobsphere.jobsite.dto.job.JobCreateRequest;
import com.jobsphere.jobsite.dto.job.JobResponse;
import com.jobsphere.jobsite.dto.job.JobUpdateRequest;
import com.jobsphere.jobsite.service.job.JobService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/jobs")
@RequiredArgsConstructor
public class JobController {
    private final JobService jobService;

    @PostMapping
    public ResponseEntity<JobResponse> createJob(@Valid @RequestBody JobCreateRequest request) {
        JobResponse response = jobService.createJob(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<Page<JobResponse>> listJobs(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String jobType,
            @RequestParam(required = false) String workplaceType,
            @RequestParam(required = false) String city,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<JobResponse> response = jobService.listJobs(category, jobType, workplaceType, city, pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{jobId}")
    public ResponseEntity<JobResponse> getJob(@PathVariable UUID jobId) {
        JobResponse response = jobService.getJob(jobId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{jobId}")
    public ResponseEntity<JobResponse> updateJob(
            @PathVariable UUID jobId,
            @Valid @RequestBody JobUpdateRequest request) {
        JobResponse response = jobService.updateJob(jobId, request);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{jobId}/deactivate")
    public ResponseEntity<Map<String, Object>> deactivateJob(@PathVariable UUID jobId) {
        jobService.deactivateJob(jobId);
        return ResponseEntity.ok(Map.of("message", "Job deactivated successfully"));
    }

    @PutMapping("/{jobId}/status")
    public ResponseEntity<JobResponse> updateJobStatus(
            @PathVariable UUID jobId,
            @RequestParam String status) {
        JobResponse response = jobService.updateJobStatus(jobId, status);
        return ResponseEntity.ok(response);
    }
}



