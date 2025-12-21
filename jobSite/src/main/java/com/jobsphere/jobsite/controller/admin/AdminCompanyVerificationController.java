package com.jobsphere.jobsite.controller.admin;

import com.jobsphere.jobsite.dto.admin.CompanyVerificationAdminResponse;
import com.jobsphere.jobsite.dto.admin.CompanyVerificationReviewRequest;
import com.jobsphere.jobsite.service.admin.AdminCompanyVerificationService;
import com.jobsphere.jobsite.service.shared.AuthenticationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/company-verifications")
@RequiredArgsConstructor
public class AdminCompanyVerificationController {
    private final AdminCompanyVerificationService adminService;
    private final AuthenticationService authenticationService;

    @GetMapping("/pending")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<CompanyVerificationAdminResponse>> getPendingVerifications() {
        List<CompanyVerificationAdminResponse> verifications = adminService.getPendingVerifications();
        return ResponseEntity.ok(verifications);
    }

    @GetMapping("/{verificationId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CompanyVerificationAdminResponse> getVerificationById(@PathVariable UUID verificationId) {
        CompanyVerificationAdminResponse verification = adminService.getVerificationById(verificationId);
        return ResponseEntity.ok(verification);
    }

    @PutMapping("/{verificationId}/review")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CompanyVerificationAdminResponse> reviewVerification(
            @PathVariable UUID verificationId,
            @Valid @RequestBody CompanyVerificationReviewRequest request) {

        String adminEmail = authenticationService.getCurrentUserEmail();
        CompanyVerificationAdminResponse updatedVerification = adminService.reviewVerification(verificationId, request, adminEmail);

        return ResponseEntity.ok(updatedVerification);
    }

    @DeleteMapping("/{verificationId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> deleteVerification(@PathVariable UUID verificationId) {
        String adminEmail = authenticationService.getCurrentUserEmail();
        adminService.deleteVerification(verificationId, adminEmail);

        return ResponseEntity.ok(Map.of(
            "success", true,
            "message", "Company verification deleted successfully"
        ));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<CompanyVerificationAdminResponse>> getAllVerifications() {
        List<CompanyVerificationAdminResponse> verifications = adminService.getAllVerifications();
        return ResponseEntity.ok(verifications);
    }
}
