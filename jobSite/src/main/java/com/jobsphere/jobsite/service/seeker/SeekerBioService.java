package com.jobsphere.jobsite.service.seeker;

import com.jobsphere.jobsite.constant.UserType;
import com.jobsphere.jobsite.dto.seeker.BioDto;
import com.jobsphere.jobsite.exception.AuthException;
import com.jobsphere.jobsite.exception.ResourceNotFoundException;
import com.jobsphere.jobsite.model.User;
import com.jobsphere.jobsite.model.seeker.Seeker;
import com.jobsphere.jobsite.model.seeker.SeekerBio;
import com.jobsphere.jobsite.repository.UserRepository;
import com.jobsphere.jobsite.repository.seeker.SeekerBioRepository;
import com.jobsphere.jobsite.repository.seeker.SeekerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SeekerBioService {
    private final SeekerBioRepository seekerBioRepository;
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
    public BioDto getBio() {
        User user = getAuthenticatedUser();
        validateSeekerUser(user);
        UUID seekerId = user.getId();
        
        SeekerBio seekerBio = seekerBioRepository.findBySeekerId(seekerId)
                .orElse(null);
        
        if (seekerBio == null) {
            return BioDto.builder().build();
        }
        
        return BioDto.builder()
                .title(seekerBio.getTitle())
                .bio(seekerBio.getBio())
                .build();
    }

    @Transactional
    public BioDto createBio(BioDto bioDto) {
        User user = getAuthenticatedUser();
        validateSeekerUser(user);
        UUID seekerId = user.getId();
        
        if (seekerBioRepository.findBySeekerId(seekerId).isPresent()) {
            throw new AuthException("Bio already exists. Use PUT to update.");
        }
        
        Seeker seeker = seekerRepository.findById(seekerId)
                .orElseThrow(() -> new ResourceNotFoundException("Seeker profile not found"));
        
        SeekerBio seekerBio = SeekerBio.builder()
                .seeker(seeker)
                .title(bioDto.getTitle())
                .bio(bioDto.getBio())
                .build();
        
        seekerBioRepository.save(seekerBio);
        
        return BioDto.builder()
                .title(seekerBio.getTitle())
                .bio(seekerBio.getBio())
                .build();
    }

    @Transactional
    public BioDto updateBio(BioDto bioDto) {
        User user = getAuthenticatedUser();
        validateSeekerUser(user);
        UUID seekerId = user.getId();
        
        Seeker seeker = seekerRepository.findById(seekerId)
                .orElseThrow(() -> new ResourceNotFoundException("Seeker profile not found"));
        
        SeekerBio seekerBio = seekerBioRepository.findBySeekerId(seekerId)
                .orElse(null);
        
        if (seekerBio == null) {
            seekerBio = SeekerBio.builder()
                    .seeker(seeker)
                    .title(bioDto.getTitle())
                    .bio(bioDto.getBio())
                    .build();
        } else {
            seekerBio.setTitle(bioDto.getTitle());
            seekerBio.setBio(bioDto.getBio());
        }
        
        seekerBioRepository.save(seekerBio);
        
        return BioDto.builder()
                .title(seekerBio.getTitle())
                .bio(seekerBio.getBio())
                .build();
    }

    @Transactional
    public void deleteBio() {
        User user = getAuthenticatedUser();
        validateSeekerUser(user);
        UUID seekerId = user.getId();
        
        seekerBioRepository.deleteBySeekerId(seekerId);
    }
}

