package com.jobsphere.jobsite.service.seeker;

import com.jobsphere.jobsite.constant.UserType;
import com.jobsphere.jobsite.dto.seeker.SkillDto;
import com.jobsphere.jobsite.exception.AuthException;
import com.jobsphere.jobsite.exception.ResourceNotFoundException;
import com.jobsphere.jobsite.model.User;
import com.jobsphere.jobsite.model.seeker.SeekerSkill;
import com.jobsphere.jobsite.repository.UserRepository;
import com.jobsphere.jobsite.repository.seeker.SeekerSkillRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SeekerSkillService {
    private final SeekerSkillRepository seekerSkillRepository;
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
    public List<SkillDto> getSkills() {
        User user = getAuthenticatedUser();
        validateSeekerUser(user);
        UUID seekerId = user.getId();

        List<SeekerSkill> skills = seekerSkillRepository.findBySeekerId(seekerId);

        return skills.stream()
                .map(skill -> SkillDto.builder()
                        .id(skill.getId())
                        .skill(skill.getSkill())
                        .proficiency(skill.getProficiency())
                        .build())
                .collect(Collectors.toList());
    }

    @Transactional
    public SkillDto addSkill(SkillDto skillDto) {
        User user = getAuthenticatedUser();
        validateSeekerUser(user);
        UUID seekerId = user.getId();

        // Check if skill already exists for this seeker
        if (seekerSkillRepository.findBySeekerIdAndSkill(seekerId, skillDto.getSkill()).isPresent()) {
            throw new IllegalArgumentException("Skill already exists");
        }

        SeekerSkill seekerSkill = SeekerSkill.builder()
                .seekerId(seekerId)
                .skill(skillDto.getSkill())
                .proficiency(skillDto.getProficiency())
                .build();

        SeekerSkill savedSkill = seekerSkillRepository.save(seekerSkill);

        return SkillDto.builder()
                .id(savedSkill.getId())
                .skill(savedSkill.getSkill())
                .proficiency(savedSkill.getProficiency())
                .build();
    }

    @Transactional
    public SkillDto updateSkill(UUID skillId, SkillDto skillDto) {
        User user = getAuthenticatedUser();
        validateSeekerUser(user);
        UUID seekerId = user.getId();

        SeekerSkill seekerSkill = seekerSkillRepository.findByIdAndSeekerId(skillId, seekerId)
                .orElseThrow(() -> new ResourceNotFoundException("Skill not found"));

        // Check if another skill with the same name already exists (excluding current
        // one)
        Optional<SeekerSkill> existingSkill = seekerSkillRepository.findBySeekerIdAndSkill(seekerId,
                skillDto.getSkill());
        if (existingSkill.isPresent() && !existingSkill.get().getId().equals(skillId)) {
            throw new IllegalArgumentException("Skill already exists");
        }

        seekerSkill.setSkill(skillDto.getSkill());
        seekerSkill.setProficiency(skillDto.getProficiency());
        SeekerSkill savedSkill = seekerSkillRepository.save(seekerSkill);

        return SkillDto.builder()
                .id(savedSkill.getId())
                .skill(savedSkill.getSkill())
                .proficiency(savedSkill.getProficiency())
                .build();
    }
}
