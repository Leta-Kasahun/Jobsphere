package com.jobsphere.jobsite.controller.employer;

import com.jobsphere.jobsite.dto.employer.EmployerStatsResponse;
import com.jobsphere.jobsite.service.employer.EmployerAnalyticsService;
import com.jobsphere.jobsite.service.auth.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/employer/analytics")
@RequiredArgsConstructor
@PreAuthorize("hasRole('EMPLOYER')")
public class EmployerAnalyticsController {

    private final EmployerAnalyticsService analyticsService;
    private final AuthService authService;

    @GetMapping("/stats")
    public ResponseEntity<EmployerStatsResponse> getStats() {
        UUID userId = authService.getCurrentUserId();
        return ResponseEntity.ok(analyticsService.getStats(userId));
    }
}
