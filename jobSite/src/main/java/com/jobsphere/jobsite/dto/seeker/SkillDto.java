package com.jobsphere.jobsite.dto.seeker;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SkillDto {
    private UUID id;

    @NotBlank
    @Size(max = 100)
    private String skill;

    @Size(max = 50)
    private String proficiency; // BEGINNER, INTERMEDIATE, ADVANCED, EXPERT
}
