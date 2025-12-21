package com.jobsphere.jobsite.controller.application;

import com.jobsphere.jobsite.dto.application.ApplicationCreateRequest;
import com.jobsphere.jobsite.dto.application.ApplicationResponse;
import com.jobsphere.jobsite.dto.application.ApplicationUpdateRequest;
import com.jobsphere.jobsite.service.application.ApplicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/applications")
@RequiredArgsConstructor
@Tag(name = "Job Applications", description = "API for managing job applications")
public class ApplicationController {
    private final ApplicationService applicationService;

    @PostMapping
    @Operation(summary = "Apply for a job")
    public ResponseEntity<ApplicationResponse> applyForJob(@Valid @RequestBody ApplicationCreateRequest request) {
        ApplicationResponse response = applicationService.applyForJob(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{applicationId}")
    @Operation(summary = "Get application details")
    public ResponseEntity<ApplicationResponse> getApplication(@PathVariable UUID applicationId) {
        ApplicationResponse response = applicationService.getApplication(applicationId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/job/{jobId}")
    @Operation(summary = "Get applications for a job (Employer only)")
    public ResponseEntity<Page<ApplicationResponse>> getApplicationsByJob(
            @PathVariable UUID jobId,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<ApplicationResponse> response = applicationService.getApplicationsByJob(jobId, pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/my-applications")
    @Operation(summary = "Get my applications (Seeker only)")
    public ResponseEntity<Page<ApplicationResponse>> getMyApplications(
            @PageableDefault(size = 20) Pageable pageable) {
        Page<ApplicationResponse> response = applicationService.getMyApplications(pageable);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{applicationId}")
    @Operation(summary = "Update application status (Employer only)")
    public ResponseEntity<ApplicationResponse> updateApplicationStatus(
            @PathVariable UUID applicationId,
            @Valid @RequestBody ApplicationUpdateRequest request) {
        ApplicationResponse response = applicationService.updateApplicationStatus(applicationId, request);
        return ResponseEntity.ok(response);
    }
}
