package com.jobsphere.jobsite.service.admin;

import com.jobsphere.jobsite.dto.admin.CompanyVerificationAdminResponse;
import com.jobsphere.jobsite.dto.admin.CompanyVerificationReviewRequest;
import com.jobsphere.jobsite.exception.ResourceNotFoundException;
import com.jobsphere.jobsite.model.User;
import com.jobsphere.jobsite.model.employer.CompanyVerification;
import com.jobsphere.jobsite.repository.UserRepository;
import com.jobsphere.jobsite.repository.employer.CompanyVerificationRepository;
import com.jobsphere.jobsite.service.employer.CompanyVerificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminCompanyVerificationService {
    private final CompanyVerificationRepository verificationRepository;
    private final UserRepository userRepository;
    private final CompanyVerificationService verificationService;

    public List<CompanyVerificationAdminResponse> getPendingVerifications() {
        log.info("Fetching all pending company verifications...");
        List<CompanyVerification> verifications = verificationRepository.findByStatusOrderBySubmittedAtAsc("PENDING");
        log.info("Found {} pending verifications in database", verifications.size());

        return verifications.stream()
                .map(this::mapToAdminResponseSilently)
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toList());
    }

    private CompanyVerificationAdminResponse mapToAdminResponseSilently(CompanyVerification v) {
        try {
            return mapToAdminResponse(v);
        } catch (Exception e) {
            log.error("Failed to map verification {} for user {}: {}", v.getId(), v.getUserId(), e.getMessage());
            return null;
        }
    }

    public CompanyVerificationAdminResponse getVerificationById(UUID verificationId) {
        CompanyVerification verification = verificationRepository.findById(verificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Verification request not found"));

        return mapToAdminResponse(verification);
    }

    @Transactional
    public CompanyVerificationAdminResponse reviewVerification(UUID verificationId,
            CompanyVerificationReviewRequest request, String adminEmail) {
        if ("APPROVE".equals(request.action())) {
            verificationService.approveVerification(verificationId, adminEmail);
        } else if ("REJECT".equals(request.action())) {
            verificationService.rejectVerification(verificationId, request.rejectionReason(), adminEmail);
        } else {
            throw new IllegalArgumentException("Invalid action: " + request.action());
        }

        // Return updated verification
        return getVerificationById(verificationId);
    }

    @Transactional
    public void deleteVerification(UUID verificationId, String adminEmail) {
        verificationService.deleteVerification(verificationId);
        log.info("Verification {} deleted by admin {}", verificationId, adminEmail);
    }

    public List<CompanyVerificationAdminResponse> getAllVerifications() {
        List<CompanyVerification> verifications = verificationRepository.findAll();

        return verifications.stream()
                .map(this::mapToAdminResponse)
                .collect(Collectors.toList());
    }

    private CompanyVerificationAdminResponse mapToAdminResponse(CompanyVerification verification) {
        User user = userRepository.findById(verification.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found for verification"));

        return new CompanyVerificationAdminResponse(
                verification.getId(),
                verification.getUserId(),
                user.getEmail(),
                verification.getCompanyName(),
                verification.getTradeLicenseUrl(),
                verification.getTinNumber(),
                verification.getWebsite(),
                verification.getStatus(),
                verification.getSubmittedAt(),
                verification.getReviewedAt());
    }
}
