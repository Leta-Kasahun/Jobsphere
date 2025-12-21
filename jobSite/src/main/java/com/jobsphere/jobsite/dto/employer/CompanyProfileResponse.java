package com.jobsphere.jobsite.dto.employer;

import java.time.Instant;
import java.util.UUID;

public record CompanyProfileResponse(
    UUID id,
    String companyName,
    String description,
    String logoUrl,
    String website,
    String location,
    String industry,
    String legalStatus,
    String socialLinks,
    Instant createdAt,
    Instant updatedAt
) {}
