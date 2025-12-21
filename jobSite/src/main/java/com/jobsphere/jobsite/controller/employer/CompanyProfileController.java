package com.jobsphere.jobsite.controller.employer;

import com.jobsphere.jobsite.dto.employer.CompanyProfileCreateRequest;
import com.jobsphere.jobsite.dto.employer.CompanyProfileResponse;
import com.jobsphere.jobsite.dto.employer.CompanyProfileUpdateRequest;
import com.jobsphere.jobsite.service.employer.CompanyProfileService;
import com.jobsphere.jobsite.service.shared.AuthenticationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/company-profile")
@RequiredArgsConstructor
public class CompanyProfileController {
    private final CompanyProfileService profileService;
    private final AuthenticationService authenticationService;

    @PostMapping
    public ResponseEntity<CompanyProfileResponse> createProfile(@Valid @RequestBody CompanyProfileCreateRequest request) {
        UUID userId = authenticationService.getCurrentUserId();
        CompanyProfileResponse response = profileService.createProfile(userId, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    public ResponseEntity<CompanyProfileResponse> getProfile() {
        UUID userId = authenticationService.getCurrentUserId();
        CompanyProfileResponse response = profileService.getProfile(userId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/me")
    public ResponseEntity<CompanyProfileResponse> updateProfile(@Valid @RequestBody CompanyProfileUpdateRequest request) {
        UUID userId = authenticationService.getCurrentUserId();
        CompanyProfileResponse response = profileService.updateProfile(userId, request);
        return ResponseEntity.ok(response);
    }
}
