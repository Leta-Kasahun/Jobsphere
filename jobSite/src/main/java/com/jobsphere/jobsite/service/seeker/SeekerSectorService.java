package com.jobsphere.jobsite.service.seeker;

import com.jobsphere.jobsite.constant.UserType;
import com.jobsphere.jobsite.dto.seeker.SectorDto;
import com.jobsphere.jobsite.exception.AuthException;
import com.jobsphere.jobsite.exception.ResourceNotFoundException;
import com.jobsphere.jobsite.model.User;
import com.jobsphere.jobsite.model.seeker.SeekerSector;
import com.jobsphere.jobsite.repository.UserRepository;
import com.jobsphere.jobsite.repository.seeker.SeekerSectorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SeekerSectorService {
    private final SeekerSectorRepository seekerSectorRepository;
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
    public List<SectorDto> getSectors() {
        User user = getAuthenticatedUser();
        validateSeekerUser(user);
        UUID seekerId = user.getId();

        List<SeekerSector> sectors = seekerSectorRepository.findBySeekerId(seekerId);

        return sectors.stream()
                .map(s -> SectorDto.builder()
                        .id(s.getSeekerId())
                        .sector(s.getSector())
                        .build())
                .collect(Collectors.toList());
    }

    @Transactional
    public SectorDto addSector(SectorDto sectorDto) {
        User user = getAuthenticatedUser();
        validateSeekerUser(user);
        UUID seekerId = user.getId();

        // Check if sector already added for this seeker
        if (seekerSectorRepository.findBySeekerIdAndSector(seekerId, sectorDto.getSector()).isPresent()) {
            throw new IllegalArgumentException("Sector already added");
        }

        SeekerSector seekerSector = SeekerSector.builder()
                .seekerId(seekerId)
                .sector(sectorDto.getSector())
                .build();

        SeekerSector saved = seekerSectorRepository.save(seekerSector);

        return SectorDto.builder()
                .id(saved.getSeekerId())
                .sector(saved.getSector())
                .build();
    }

    @Transactional
    public void deleteSector(UUID id) {
        User user = getAuthenticatedUser();
        validateSeekerUser(user);
        UUID seekerId = user.getId();

        if (!id.equals(seekerId)) {
            throw new ResourceNotFoundException("Sector not found");
        }

        SeekerSector sector = seekerSectorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sector not found"));

        seekerSectorRepository.delete(sector);
    }
}
