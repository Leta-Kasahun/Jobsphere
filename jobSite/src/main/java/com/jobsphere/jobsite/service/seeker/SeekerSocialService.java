package com.jobsphere.jobsite.service.seeker;

import com.jobsphere.jobsite.constant.UserType;
import com.jobsphere.jobsite.dto.seeker.SocialLinkDto;
import com.jobsphere.jobsite.exception.AuthException;
import com.jobsphere.jobsite.model.User;
import com.jobsphere.jobsite.model.seeker.SeekerSocialLink;
import com.jobsphere.jobsite.repository.UserRepository;
import com.jobsphere.jobsite.repository.seeker.SeekerSocialLinkRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SeekerSocialService {
    private final SeekerSocialLinkRepository seekerSocialLinkRepository;
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
    public List<SocialLinkDto> getSocialLinks() {
        User user = getAuthenticatedUser();
        validateSeekerUser(user);
        UUID seekerId = user.getId();
        
        List<SeekerSocialLink> socialLinks = seekerSocialLinkRepository.findBySeekerId(seekerId);
        
        return socialLinks.stream()
                .map(link -> SocialLinkDto.builder()
                        .platform(link.getPlatform())
                        .url(link.getUrl())
                        .build())
                .collect(Collectors.toList());
    }

    @Transactional
    public SocialLinkDto addSocialLink(SocialLinkDto socialLinkDto) {
        User user = getAuthenticatedUser();
        validateSeekerUser(user);
        UUID seekerId = user.getId();
        
        SeekerSocialLink socialLink = SeekerSocialLink.builder()
                .seekerId(seekerId)
                .platform(socialLinkDto.getPlatform())
                .url(socialLinkDto.getUrl())
                .build();
        
        seekerSocialLinkRepository.save(socialLink);
        
        return SocialLinkDto.builder()
                .platform(socialLink.getPlatform())
                .url(socialLink.getUrl())
                .build();
    }

    @Transactional
    public void deleteSocialLink(SocialLinkDto socialLinkDto) {
        User user = getAuthenticatedUser();
        validateSeekerUser(user);
        UUID seekerId = user.getId();
        
        List<SeekerSocialLink> links = seekerSocialLinkRepository.findBySeekerId(seekerId);
        links.stream()
                .filter(link -> link.getPlatform().equals(socialLinkDto.getPlatform()) 
                        && link.getUrl().equals(socialLinkDto.getUrl()))
                .findFirst()
                .ifPresent(seekerSocialLinkRepository::delete);
    }
}

