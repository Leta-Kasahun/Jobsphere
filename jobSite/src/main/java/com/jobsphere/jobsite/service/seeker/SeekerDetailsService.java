package com.jobsphere.jobsite.service.seeker;

import com.jobsphere.jobsite.constant.UserType;
import com.jobsphere.jobsite.dto.seeker.MediaDto;
import com.jobsphere.jobsite.exception.AuthException;
import com.jobsphere.jobsite.model.User;
import com.jobsphere.jobsite.model.seeker.Seeker;
import com.jobsphere.jobsite.repository.UserRepository;
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
                .profileImageUrl(seeker.getProfileImageUrl())
                .cvUrl(seeker.getCvUrl())
                .build();
    }

    @Transactional
    public MediaDto uploadProfileImage(MultipartFile file) throws IOException {
        User user = getAuthenticatedUser();
        validateSeekerUser(user);
        UUID seekerId = user.getId();
        
        Seeker seeker = seekerRepository.findById(seekerId).orElse(null);
        if (seeker == null) {
            seeker = Seeker.builder().id(seekerId).build();
        }
        
        // Delete existing image if present
        if (seeker.getProfileImageUrl() != null) {
            cloudinaryFileService.deleteFile(seeker.getProfileImageUrl());
        }
        
        String imageUrl = cloudinaryFileService.uploadImage(file, "seekers/profile");
        seeker.setProfileImageUrl(imageUrl);
        seekerRepository.save(seeker);
        
        return MediaDto.builder()
                .profileImageUrl(seeker.getProfileImageUrl())
                .cvUrl(seeker.getCvUrl())
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
                .profileImageUrl(seeker.getProfileImageUrl())
                .cvUrl(seeker.getCvUrl())
                .build();
    }

    @Transactional
    public MediaDto uploadCv(MultipartFile file) throws IOException {
        User user = getAuthenticatedUser();
        validateSeekerUser(user);
        UUID seekerId = user.getId();
        
        Seeker seeker = seekerRepository.findById(seekerId).orElse(null);
        if (seeker == null) {
            seeker = Seeker.builder().id(seekerId).build();
        }
        
        // Delete existing CV if present
        if (seeker.getCvUrl() != null) {
            cloudinaryFileService.deleteFile(seeker.getCvUrl());
        }
        
        String cvUrl = cloudinaryFileService.uploadDocument(file, "seekers/cv");
        seeker.setCvUrl(cvUrl);
        seekerRepository.save(seeker);
        
        return MediaDto.builder()
                .profileImageUrl(seeker.getProfileImageUrl())
                .cvUrl(seeker.getCvUrl())
                .build();
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
                .profileImageUrl(seeker.getProfileImageUrl())
                .cvUrl(seeker.getCvUrl())
                .build();
    }
}

