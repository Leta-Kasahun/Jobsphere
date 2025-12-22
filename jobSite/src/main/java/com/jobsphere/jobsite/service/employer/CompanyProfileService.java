package com.jobsphere.jobsite.service.employer;

import com.jobsphere.jobsite.dto.employer.CompanyProfileCreateRequest;
import com.jobsphere.jobsite.dto.employer.CompanyProfileResponse;
import com.jobsphere.jobsite.dto.employer.CompanyProfileUpdateRequest;
import com.jobsphere.jobsite.exception.ResourceNotFoundException;
import com.jobsphere.jobsite.model.employer.CompanyProfile;
import com.jobsphere.jobsite.model.employer.CompanyVerification;
import com.jobsphere.jobsite.repository.employer.CompanyProfileRepository;
import com.jobsphere.jobsite.repository.employer.CompanyVerificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class CompanyProfileService {
    private final CompanyProfileRepository profileRepository;
    private final CompanyVerificationRepository verificationRepository;
    private final com.jobsphere.jobsite.service.shared.CloudinaryImageService cloudinaryImageService;

    @Transactional
    public CompanyProfileResponse uploadLogo(UUID userId, org.springframework.web.multipart.MultipartFile file)
            throws IOException {
        validateVerificationAccess(userId);

        CompanyProfile profile = profileRepository.findByUserId(userId)
                .orElseGet(() -> {
                    CompanyVerification verification = verificationRepository.findByUserIdAndStatus(userId, "APPROVED")
                            .orElseThrow(() -> new IllegalStateException("No approved company verification found"));

                    return CompanyProfile.builder()
                            .userId(userId)
                            .companyName(verification.getCompanyName())
                            .website(verification.getWebsite())
                            .description("") // Required by not-null constraint
                            .build();
                });

        // Delete old logo if exists
        if (StringUtils.hasText(profile.getLogoUrl())) {
            cloudinaryImageService.deleteImage(profile.getLogoUrl());
        }

        String logoUrl = cloudinaryImageService.uploadImage(file, "companies/logos");
        profile.setLogoUrl(logoUrl);

        profile = profileRepository.save(profile);
        log.info("Company logo uploaded for user {}", userId);

        return mapToResponse(profile);
    }

    @Transactional
    public CompanyProfileResponse createProfile(UUID userId, CompanyProfileCreateRequest request) {
        validateVerificationAccess(userId);

        if (profileRepository.existsByUserId(userId)) {
            throw new IllegalStateException("Company profile already exists");
        }

        CompanyVerification verification = verificationRepository.findByUserIdAndStatus(userId, "APPROVED")
                .orElseThrow(() -> new IllegalStateException("No approved company verification found"));

        String logoUrl = request.logo();

        CompanyProfile profile = CompanyProfile.builder()
                .userId(userId)
                .companyName(verification.getCompanyName())
                .description(request.description())
                .logoUrl(logoUrl)
                .website(verification.getWebsite())
                .location(request.location())
                .industry(request.industry())
                .legalStatus(request.legalStatus())
                .socialLinks(request.socialLinks())
                .build();

        profile = profileRepository.save(profile);
        log.info("Company profile created for user {}", userId);

        return mapToResponse(profile);
    }

    public CompanyProfileResponse getProfile(UUID userId) {
        validateVerificationAccess(userId);

        return profileRepository.findByUserId(userId)
                .map(this::mapToResponse)
                .orElseGet(() -> {
                    CompanyVerification verification = verificationRepository.findByUserIdAndStatus(userId, "APPROVED")
                            .orElseThrow(() -> new IllegalStateException("No approved company verification found"));

                    return new CompanyProfileResponse(
                            null,
                            verification.getCompanyName(),
                            null,
                            null,
                            verification.getWebsite(),
                            null,
                            null,
                            null,
                            null,
                            null,
                            null);
                });
    }

    @Transactional
    public CompanyProfileResponse updateProfile(UUID userId, CompanyProfileUpdateRequest request) {
        validateVerificationAccess(userId);

        CompanyProfile profile = profileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Company profile not found"));

        if (StringUtils.hasText(request.description())) {
            profile.setDescription(request.description());
        }
        if (StringUtils.hasText(request.location())) {
            profile.setLocation(request.location());
        }
        if (StringUtils.hasText(request.industry())) {
            profile.setIndustry(request.industry());
        }
        if (StringUtils.hasText(request.legalStatus())) {
            profile.setLegalStatus(request.legalStatus());
        }
        if (request.socialLinks() != null) {
            profile.setSocialLinks(request.socialLinks());
        }

        if (request.logo() != null) {
            profile.setLogoUrl(request.logo());
        }

        profile = profileRepository.save(profile);
        log.info("Company profile updated for user {}", userId);

        return mapToResponse(profile);
    }

    private void validateVerificationAccess(UUID userId) {
        CompanyVerification verification = verificationRepository.findByUserIdAndStatus(userId, "APPROVED")
                .orElseThrow(() -> new IllegalStateException("No approved company verification found"));

        if (!Boolean.TRUE.equals(verification.getCodeUsed())) {
            throw new IllegalStateException("Company verification code must be used before accessing profile");
        }
    }

    private CompanyProfileResponse mapToResponse(CompanyProfile profile) {
        return new CompanyProfileResponse(
                profile.getId(),
                profile.getCompanyName(),
                profile.getDescription(),
                profile.getLogoUrl(),
                profile.getWebsite(),
                profile.getLocation(),
                profile.getIndustry(),
                profile.getLegalStatus(),
                profile.getSocialLinks(),
                profile.getCreatedAt(),
                profile.getUpdatedAt());
    }
}
