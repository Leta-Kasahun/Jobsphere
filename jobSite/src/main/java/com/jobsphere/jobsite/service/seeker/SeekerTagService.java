package com.jobsphere.jobsite.service.seeker;

import com.jobsphere.jobsite.constant.UserType;
import com.jobsphere.jobsite.dto.seeker.TagDto;
import com.jobsphere.jobsite.exception.AuthException;
import com.jobsphere.jobsite.model.User;
import com.jobsphere.jobsite.model.seeker.SeekerTag;
import com.jobsphere.jobsite.repository.UserRepository;
import com.jobsphere.jobsite.repository.seeker.SeekerTagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SeekerTagService {
    private final SeekerTagRepository seekerTagRepository;
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
    public List<TagDto> getTags() {
        User user = getAuthenticatedUser();
        validateSeekerUser(user);
        UUID seekerId = user.getId();
        
        List<SeekerTag> tags = seekerTagRepository.findBySeekerId(seekerId);
        
        return tags.stream()
                .map(tag -> TagDto.builder()
                        .tag(tag.getTag())
                        .build())
                .collect(Collectors.toList());
    }

    @Transactional
    public TagDto addTag(TagDto tagDto) {
        User user = getAuthenticatedUser();
        validateSeekerUser(user);
        UUID seekerId = user.getId();
        
        // Check if tag already exists for this seeker
        if (seekerTagRepository.findBySeekerIdAndTag(seekerId, tagDto.getTag()).isPresent()) {
            throw new IllegalArgumentException("Tag already exists");
        }
        
        SeekerTag seekerTag = SeekerTag.builder()
                .seekerId(seekerId)
                .tag(tagDto.getTag())
                .build();
        
        seekerTagRepository.save(seekerTag);
        
        return TagDto.builder()
                .tag(seekerTag.getTag())
                .build();
    }

    @Transactional
    public void deleteTag(TagDto tagDto) {
        User user = getAuthenticatedUser();
        validateSeekerUser(user);
        UUID seekerId = user.getId();
        
        seekerTagRepository.deleteBySeekerIdAndTag(seekerId, tagDto.getTag());
    }
}

