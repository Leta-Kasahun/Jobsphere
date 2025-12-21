package com.jobsphere.jobsite.service.employer;

import com.jobsphere.jobsite.dto.employer.CompanyVerificationRequest;
import com.jobsphere.jobsite.dto.employer.CompanyVerificationResponse;
import com.jobsphere.jobsite.exception.ResourceNotFoundException;
import com.jobsphere.jobsite.model.User;
import com.jobsphere.jobsite.model.employer.CompanyVerification;
import com.jobsphere.jobsite.repository.UserRepository;
import com.jobsphere.jobsite.repository.employer.CompanyVerificationRepository;
import com.jobsphere.jobsite.service.shared.EmailNotificationService;
import com.jobsphere.jobsite.service.shared.VerificationCodeGeneratorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class CompanyVerificationService {
    private final CompanyVerificationRepository verificationRepository;
    private final UserRepository userRepository;
    private final EmailNotificationService emailNotificationService;
    private final VerificationCodeGeneratorService verificationCodeGenerator;

    @Transactional
    public CompanyVerificationResponse submitVerification(UUID userId, CompanyVerificationRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Check if user already has a pending or approved verification
        if (verificationRepository.existsByUserIdAndStatus(userId, "PENDING") ||
                verificationRepository.existsByUserIdAndStatus(userId, "APPROVED")) {
            throw new IllegalStateException("You already have a pending or approved verification request");
        }

        String tradeLicenseUrl = "uploaded"; // Simplified - in production, implement proper file storage

        CompanyVerification verification = CompanyVerification.builder()
                .userId(userId)
                .companyName(request.companyName())
                .tradeLicenseUrl(tradeLicenseUrl)
                .tinNumber(request.tinNumber())
                .website(request.website())
                .status("PENDING")
                .submittedAt(Instant.now())
                .codeUsed(false)
                .build();

        verification = verificationRepository.save(verification);

        log.info("Company verification submitted for user {} with company {}", userId, request.companyName());
        return mapToResponse(verification);
    }

    public CompanyVerificationResponse getVerificationStatus(UUID userId) {
        CompanyVerification verification = verificationRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("No verification request found"));

        return mapToResponse(verification);
    }

    @Transactional
    public boolean verifyCode(UUID userId, String code) {
        CompanyVerification verification = verificationRepository
                .findByUserIdAndStatus(userId, "APPROVED")
                .orElseThrow(() -> new ResourceNotFoundException("No approved verification found"));

        if (verification.getCodeUsed()) {
            throw new IllegalStateException("Verification code has already been used");
        }

        if (!code.equals(verification.getVerificationCode())) {
            throw new IllegalArgumentException("Invalid verification code");
        }

        verification.setCodeUsed(true);
        verificationRepository.save(verification);

        log.info("Verification code used successfully for user {}", userId);
        return true;
    }

    @Transactional
    public void approveVerification(UUID verificationId, String adminEmail) {
        CompanyVerification verification = verificationRepository.findById(verificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Verification request not found"));

        if (!"PENDING".equals(verification.getStatus())) {
            throw new IllegalStateException("Verification is not in pending status");
        }

        // Generate verification code
        String verificationCode = verificationCodeGenerator.generateSixDigitCode();

        verification.setStatus("APPROVED");
        verification.setVerificationCode(verificationCode);
        verification.setReviewedAt(Instant.now());
        verificationRepository.save(verification);

        // Get user email
        User user = userRepository.findById(verification.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Send notification with code
        emailNotificationService.sendVerificationCode(user.getEmail(), verificationCode);

        log.info("Verification approved for company {} by admin {}", verification.getCompanyName(), adminEmail);
    }

    @Transactional
    public void rejectVerification(UUID verificationId, String rejectionReason, String adminEmail) {
        CompanyVerification verification = verificationRepository.findById(verificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Verification request not found"));

        if (!"PENDING".equals(verification.getStatus())) {
            throw new IllegalStateException("Verification is not in pending status");
        }

        verification.setStatus("BANNED");
        verification.setRejectionReason(rejectionReason);
        verification.setReviewedAt(Instant.now());
        verificationRepository.save(verification);

        // Get user email
        User user = userRepository.findById(verification.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Send rejection notification
        emailNotificationService.sendVerificationRejection(user.getEmail(), verification.getCompanyName(), rejectionReason);

        // Clean up trade license if needed (simplified)

        log.info("Verification rejected for company {} by admin {}", verification.getCompanyName(), adminEmail);
    }

    @Transactional
    public void deleteVerification(UUID verificationId) {
        CompanyVerification verification = verificationRepository.findById(verificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Verification request not found"));

        // File cleanup would be handled here if needed (simplified)

        verificationRepository.delete(verification);
        log.info("Verification deleted for company {}", verification.getCompanyName());
    }

    private CompanyVerificationResponse mapToResponse(CompanyVerification verification) {
        return new CompanyVerificationResponse(
                verification.getId(),
                verification.getCompanyName(),
                verification.getTradeLicenseUrl(),
                verification.getTinNumber(),
                verification.getWebsite(),
                verification.getStatus(),
                verification.getCodeUsed(),
                verification.getRejectionReason(),
                verification.getSubmittedAt(),
                verification.getReviewedAt());
    }
}
