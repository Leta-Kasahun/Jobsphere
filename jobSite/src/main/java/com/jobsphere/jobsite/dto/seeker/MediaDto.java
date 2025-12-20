package com.jobsphere.jobsite.dto.seeker;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MediaDto {
    private String profileImageUrl;
    private String cvUrl;
}

