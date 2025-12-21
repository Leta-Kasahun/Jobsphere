package com.jobsphere.jobsite.dto.employer;

import java.time.Instant;
import java.util.UUID;

public record CompanyVerificationResponse(
    UUID id,
    String companyName,
    String tradeLicenseUrl,
    String tinNumber,
    String website,
    String status,
    Instant submittedAt,
    Instant reviewedAt
) {}
