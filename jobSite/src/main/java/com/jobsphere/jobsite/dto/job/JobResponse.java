package com.jobsphere.jobsite.dto.job;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record JobResponse(
    UUID id,
    UUID companyProfileId,
    String companyName,
    UUID addressId,
    String addressCountry,
    String addressRegion,
    String addressCity,
    String addressSubCity,
    String addressStreet,
    String title,
    String description,
    String jobType,
    String workplaceType,
    String category,
    String educationLevel,
    String genderRequirement,
    Integer vacancyCount,
    String experienceLevel,
    String experienceDescription,
    BigDecimal salaryMin,
    BigDecimal salaryMax,
    LocalDate deadline,
    Boolean isActive,
    String status,
    Integer filledCount,
    Instant createdAt,
    Instant updatedAt
) {}
