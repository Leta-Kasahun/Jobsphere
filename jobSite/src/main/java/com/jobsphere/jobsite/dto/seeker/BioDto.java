package com.jobsphere.jobsite.dto.seeker;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BioDto {
    @Size(max = 200)
    private String title;

    private String bio;
}

