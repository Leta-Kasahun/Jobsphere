package com.jobsphere.jobsite.controller.employer;

import com.jobsphere.jobsite.dto.employer.CompanyVerificationRequest;
import com.jobsphere.jobsite.dto.employer.CompanyVerificationResponse;
import com.jobsphere.jobsite.dto.employer.VerificationCodeRequest;
import com.jobsphere.jobsite.service.employer.CompanyVerificationService;
import com.jobsphere.jobsite.service.shared.AuthenticationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/employer/verification")
@RequiredArgsConstructor
public class CompanyVerificationController {
    private final CompanyVerificationService verificationService;
    private final AuthenticationService authenticationService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CompanyVerificationResponse> submitVerification(
            @RequestParam("companyName") String companyName,
            @RequestParam("tradeLicense") MultipartFile tradeLicense,
            @RequestParam(value = "tinNumber", required = false) String tinNumber,
            @RequestParam(value = "website", required = false) String website) {

        UUID userId = authenticationService.getCurrentUserId();

        CompanyVerificationRequest request = new CompanyVerificationRequest(
            companyName, tradeLicense, tinNumber, website
        );

        CompanyVerificationResponse response = verificationService.submitVerification(userId, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/status")
    public ResponseEntity<CompanyVerificationResponse> getVerificationStatus() {
        UUID userId = authenticationService.getCurrentUserId();
        CompanyVerificationResponse response = verificationService.getVerificationStatus(userId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/verify-code")
    public ResponseEntity<Map<String, Object>> verifyCode(@Valid @RequestBody VerificationCodeRequest request) {
        UUID userId = authenticationService.getCurrentUserId();
        boolean success = verificationService.verifyCode(userId, request.code());

        return ResponseEntity.ok(Map.of(
            "success", true,
            "message", "Company verification completed successfully",
            "verified", success
        ));
    }
}
