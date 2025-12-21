package com.jobsphere.jobsite.dto.seeker;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectDto {
    private UUID id;

    @NotBlank
    @Size(max = 200)
    private String title;

    @Size(max = 1000)
    private String description;

    @Size(max = 500)
    private String projectUrl;

    @Size(max = 500)
    private String imageUrl; // Keep for backward compatibility or main image

    private List<String> imageUrls;

    @Size(max = 500)
    private String videoUrl;

    @Size(max = 20)
    private String videoType; // UPLOAD, YOUTUBE
}
