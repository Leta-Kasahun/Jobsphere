package com.jobsphere.jobsite.dto.seeker;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SocialLinkDto {
    @NotBlank
    @Size(max = 50)
    private String platform;

    @NotBlank
    @Size(max = 500)
    private String url;
}

