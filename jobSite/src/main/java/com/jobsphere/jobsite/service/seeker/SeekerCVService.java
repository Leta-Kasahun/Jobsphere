package com.jobsphere.jobsite.service.seeker;

import com.jobsphere.jobsite.constant.UserType;
import com.jobsphere.jobsite.dto.seeker.CVDto;
import com.jobsphere.jobsite.exception.AuthException;
import com.jobsphere.jobsite.exception.ResourceNotFoundException;
import com.jobsphere.jobsite.model.User;
import com.jobsphere.jobsite.model.seeker.Seeker;
import com.jobsphere.jobsite.model.seeker.SeekerCV;
import com.jobsphere.jobsite.repository.UserRepository;
import com.jobsphere.jobsite.repository.seeker.SeekerCVRepository;
import com.jobsphere.jobsite.repository.seeker.SeekerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SeekerCVService {
    private final SeekerCVRepository seekerCVRepository;
    private final SeekerRepository seekerRepository;
    private final UserRepository userRepository;

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
    public CVDto getCV() {
        User user = getAuthenticatedUser();
        validateSeekerUser(user);
        UUID seekerId = user.getId();
        
        SeekerCV seekerCV = seekerCVRepository.findBySeekerId(seekerId)
                .orElse(null);
        
        if (seekerCV == null) {
            return CVDto.builder().build();
        }
        
        return CVDto.builder()
                .id(seekerCV.getId())
                .title(seekerCV.getTitle())
                .about(seekerCV.getAbout())
                .details(seekerCV.getDetails())
                .build();
    }

    @Transactional
    public CVDto createOrUpdateCV(CVDto cvDto) {
        User user = getAuthenticatedUser();
        validateSeekerUser(user);
        UUID seekerId = user.getId();
        
        Seeker seeker = seekerRepository.findById(seekerId)
                .orElseThrow(() -> new ResourceNotFoundException("Seeker profile not found"));
        
        SeekerCV seekerCV = seekerCVRepository.findBySeekerId(seekerId)
                .orElse(null);
        
        if (seekerCV == null) {
            // Create new CV
            seekerCV = SeekerCV.builder()
                    .seekerId(seekerId)
                    .seeker(seeker)
                    .title(cvDto.getTitle())
                    .about(cvDto.getAbout())
                    .details(cvDto.getDetails())
                    .build();
        } else {
            // Update existing CV
            seekerCV.setTitle(cvDto.getTitle());
            seekerCV.setAbout(cvDto.getAbout());
            seekerCV.setDetails(cvDto.getDetails());
        }
        
        SeekerCV savedCV = seekerCVRepository.save(seekerCV);
        
        return CVDto.builder()
                .id(savedCV.getId())
                .title(savedCV.getTitle())
                .about(savedCV.getAbout())
                .details(savedCV.getDetails())
                .build();
    }

    @Transactional
    public CVDto updateCV(CVDto cvDto) {
        User user = getAuthenticatedUser();
        validateSeekerUser(user);
        UUID seekerId = user.getId();
        
        SeekerCV seekerCV = seekerCVRepository.findBySeekerId(seekerId)
                .orElseThrow(() -> new ResourceNotFoundException("CV not found. Use POST to create."));
        
        seekerCV.setTitle(cvDto.getTitle());
        seekerCV.setAbout(cvDto.getAbout());
        seekerCV.setDetails(cvDto.getDetails());
        
        SeekerCV savedCV = seekerCVRepository.save(seekerCV);
        
        return CVDto.builder()
                .id(savedCV.getId())
                .title(savedCV.getTitle())
                .about(savedCV.getAbout())
                .details(savedCV.getDetails())
                .build();
    }
}

