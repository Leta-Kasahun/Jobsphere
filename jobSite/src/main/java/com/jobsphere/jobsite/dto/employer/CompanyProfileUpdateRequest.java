package com.jobsphere.jobsite.dto.employer;

import jakarta.validation.constraints.Size;

public record CompanyProfileUpdateRequest(
    String description,

    @Size(max = 255, message = "Location must be less than 255 characters")
    String location,

    @Size(max = 255, message = "Industry must be less than 255 characters")
    String industry,

    @Size(max = 50, message = "Legal status must be less than 50 characters")
    String legalStatus,

    String socialLinks,

    String logo
) {}
