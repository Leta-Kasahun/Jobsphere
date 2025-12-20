package com.jobsphere.jobsite.service.seeker;

import com.jobsphere.jobsite.constant.UserType;
import com.jobsphere.jobsite.dto.seeker.SectorDto;
import com.jobsphere.jobsite.exception.AuthException;
import com.jobsphere.jobsite.exception.ResourceNotFoundException;
import com.jobsphere.jobsite.model.User;
import com.jobsphere.jobsite.model.seeker.Seeker;
import com.jobsphere.jobsite.model.seeker.SeekerSector;
import com.jobsphere.jobsite.repository.UserRepository;
import com.jobsphere.jobsite.repository.seeker.SeekerRepository;
import com.jobsphere.jobsite.repository.seeker.SeekerSectorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SeekerSectorService {
    private final SeekerSectorRepository seekerSectorRepository;
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
    public SectorDto getSector() {
        User user = getAuthenticatedUser();
        validateSeekerUser(user);
        UUID seekerId = user.getId();
        
        SeekerSector seekerSector = seekerSectorRepository.findBySeekerId(seekerId)
                .orElse(null);
        
        if (seekerSector == null) {
            return SectorDto.builder().build();
        }
        
        return SectorDto.builder()
                .sector(seekerSector.getSector())
                .build();
    }

    @Transactional
    public SectorDto createSector(SectorDto sectorDto) {
        User user = getAuthenticatedUser();
        validateSeekerUser(user);
        UUID seekerId = user.getId();
        
        if (seekerSectorRepository.findBySeekerId(seekerId).isPresent()) {
            throw new AuthException("Sector already exists. Use PUT to update.");
        }
        
        Seeker seeker = seekerRepository.findById(seekerId)
                .orElseThrow(() -> new ResourceNotFoundException("Seeker profile not found"));
        
        SeekerSector seekerSector = SeekerSector.builder()
                .seeker(seeker)
                .sector(sectorDto.getSector())
                .build();
        
        seekerSectorRepository.save(seekerSector);
        
        return SectorDto.builder()
                .sector(seekerSector.getSector())
                .build();
    }

    @Transactional
    public SectorDto updateSector(SectorDto sectorDto) {
        User user = getAuthenticatedUser();
        validateSeekerUser(user);
        UUID seekerId = user.getId();
        
        Seeker seeker = seekerRepository.findById(seekerId)
                .orElseThrow(() -> new ResourceNotFoundException("Seeker profile not found"));
        
        SeekerSector seekerSector = seekerSectorRepository.findBySeekerId(seekerId)
                .orElse(null);
        
        if (seekerSector == null) {
            seekerSector = SeekerSector.builder()
                    .seeker(seeker)
                    .sector(sectorDto.getSector())
                    .build();
        } else {
            seekerSector.setSector(sectorDto.getSector());
        }
        
        seekerSectorRepository.save(seekerSector);
        
        return SectorDto.builder()
                .sector(seekerSector.getSector())
                .build();
    }
}

