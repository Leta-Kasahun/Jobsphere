package com.jobsphere.jobsite.dto.employer;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CompanyProfileCreateRequest(
        @NotBlank(message = "Description is required") String description,

        @Size(max = 1000, message = "Location must be less than 1000 characters") String location,

        @Size(max = 1000, message = "Industry must be less than 1000 characters") String industry,

        @Size(max = 255, message = "Legal status must be less than 255 characters") String legalStatus,

        String socialLinks,

        String logo) {
}
