package com.jobsphere.jobsite.service.seeker;

import com.jobsphere.jobsite.constant.UserType;
import com.jobsphere.jobsite.dto.seeker.MediaDto;
import com.jobsphere.jobsite.exception.AuthException;
import com.jobsphere.jobsite.model.User;
import com.jobsphere.jobsite.model.seeker.Seeker;
import com.jobsphere.jobsite.model.seeker.SeekerCV;
import com.jobsphere.jobsite.repository.UserRepository;
import com.jobsphere.jobsite.repository.seeker.SeekerCVRepository;
import com.jobsphere.jobsite.repository.seeker.SeekerRepository;
import com.jobsphere.jobsite.service.shared.CloudinaryFileService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SeekerDetailsService {
    private final SeekerRepository seekerRepository;
    private final SeekerCVRepository seekerCVRepository;
    private final UserRepository userRepository;
    private final CloudinaryFileService cloudinaryFileService;

    private User getAuthenticatedUser() {
        return userRepository.findByEmail(
                SecurityContextHolder.getContext().getAuthentication().getName())
                .orElseThrow(() -> new AuthException("User not found"));
    }

    private void validateSeekerUser(User user) {
        if (user.getUserType() != UserType.SEEKER) {
            throw new AuthException("Only seekers can perform this action");
        }
    }

    @Transactional(readOnly = true)
    public MediaDto getMedia() {
        User user = getAuthenticatedUser();
        validateSeekerUser(user);
        UUID seekerId = user.getId();

        Seeker seeker = seekerRepository.findById(seekerId).orElse(null);

        if (seeker == null) {
            return MediaDto.builder().build();
        }

        return MediaDto.builder()
                .profileImageUrl(cloudinaryFileService.generateSignedUrl(seeker.getProfileImageUrl()))
                .cvUrl(cloudinaryFileService.generateSignedUrl(seeker.getCvUrl()))
                .build();
    }

    @Transactional
    public MediaDto uploadProfileImage(MultipartFile file) throws IOException {
        User user = getAuthenticatedUser();
        validateSeekerUser(user);
        UUID seekerId = user.getId();

        Seeker seeker = seekerRepository.findById(seekerId).orElse(null);
        if (seeker == null) {
            String defaultName = user.getEmail().split("@")[0];
            seeker = Seeker.builder()
                    .id(seekerId)
                    .firstName(defaultName)
                    .middleName("")
                    .phone("")
                    .build();
        }

        // Delete existing image if present
        if (seeker.getProfileImageUrl() != null) {
            cloudinaryFileService.deleteFile(seeker.getProfileImageUrl());
        }

        String imageUrl = cloudinaryFileService.uploadImage(file, "seekers/profile");
        seeker.setProfileImageUrl(imageUrl);
        seekerRepository.save(seeker);

        return MediaDto.builder()
                .profileImageUrl(cloudinaryFileService.generateSignedUrl(seeker.getProfileImageUrl()))
                .cvUrl(cloudinaryFileService.generateSignedUrl(seeker.getCvUrl()))
                .build();
    }

    @Transactional
    public MediaDto deleteProfileImage() {
        User user = getAuthenticatedUser();
        validateSeekerUser(user);
        UUID seekerId = user.getId();

        Seeker seeker = seekerRepository.findById(seekerId).orElse(null);

        if (seeker != null && seeker.getProfileImageUrl() != null) {
            try {
                cloudinaryFileService.deleteFile(seeker.getProfileImageUrl());
            } catch (IOException ignored) {
            }
            seeker.setProfileImageUrl(null);
            seekerRepository.save(seeker);
        }

        if (seeker == null) {
            return MediaDto.builder().build();
        }

        return MediaDto.builder()
                .profileImageUrl(cloudinaryFileService.generateSignedUrl(seeker.getProfileImageUrl()))
                .cvUrl(cloudinaryFileService.generateSignedUrl(seeker.getCvUrl()))
                .build();
    }

    @Transactional
    public MediaDto uploadCv(MultipartFile file) throws IOException {
        User user = getAuthenticatedUser();
        validateSeekerUser(user);
        UUID seekerId = user.getId();

        Seeker seeker = seekerRepository.findById(seekerId).orElse(null);
        if (seeker == null) {
            String defaultName = user.getEmail().split("@")[0];
            seeker = Seeker.builder()
                    .id(seekerId)
                    .firstName(defaultName)
                    .middleName("")
                    .phone("")
                    .build();
        }

        // Delete existing CV if present
        if (seeker.getCvUrl() != null) {
            cloudinaryFileService.deleteFile(seeker.getCvUrl());
        }

        String cvUrl = cloudinaryFileService.uploadDocument(file, "seekers/cv");
        seeker.setCvUrl(cvUrl);
        seekerRepository.save(seeker);

        // Sync with SeekerCV record for structured profile views
        final String finalCvUrl = cvUrl;
        seekerCVRepository.findBySeekerId(seekerId).ifPresentOrElse(
                cv -> {
                    cv.setCvUrl(finalCvUrl);
                    cv.setFileName(file.getOriginalFilename());
                    cv.setFileSize(formatFileSize(file.getSize()));
                    seekerCVRepository.save(cv);
                },
                () -> {
                    SeekerCV cv = SeekerCV.builder()
                            .seekerId(seekerId)
                            .cvUrl(finalCvUrl)
                            .fileName(file.getOriginalFilename())
                            .fileSize(formatFileSize(file.getSize()))
                            .title("Uploaded CV")
                            .build();
                    seekerCVRepository.save(cv);
                });

        return MediaDto.builder()
                .profileImageUrl(cloudinaryFileService.generateSignedUrl(seeker.getProfileImageUrl()))
                .cvUrl(cloudinaryFileService.generateSignedUrl(seeker.getCvUrl()))
                .build();
    }

    private String formatFileSize(long bytes) {
        if (bytes < 1024)
            return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(1024));
        String pre = "KMGTPE".charAt(exp - 1) + "";
        return String.format("%.1f %sB", bytes / Math.pow(1024, exp), pre);
    }

    @Transactional
    public MediaDto deleteCv() {
        User user = getAuthenticatedUser();
        validateSeekerUser(user);
        UUID seekerId = user.getId();

        Seeker seeker = seekerRepository.findById(seekerId).orElse(null);

        if (seeker != null && seeker.getCvUrl() != null) {
            try {
                cloudinaryFileService.deleteFile(seeker.getCvUrl());
            } catch (IOException ignored) {
            }
            seeker.setCvUrl(null);
            seekerRepository.save(seeker);
        }

        if (seeker == null) {
            return MediaDto.builder().build();
        }

        return MediaDto.builder()
                .profileImageUrl(cloudinaryFileService.generateSignedUrl(seeker.getProfileImageUrl()))
                .cvUrl(cloudinaryFileService.generateSignedUrl(seeker.getCvUrl()))
                .build();
    }
}
