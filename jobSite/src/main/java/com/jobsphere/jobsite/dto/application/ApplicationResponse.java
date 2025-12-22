package com.jobsphere.jobsite.dto.application;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import java.util.List;
import java.util.UUID;

public record ApplicationResponse(
        UUID id,
        UUID jobId,
        String jobTitle,
        UUID seekerId,
        String seekerName,
        String cvUrl,
        List<String> skills,
        List<String> sectors,
        List<String> tags,
        String coverLetter,
        BigDecimal expectedSalary,
        String status,
        Instant appliedAt,
        Instant reviewedAt,
        Boolean hiredFlag,
        String notes,
        Instant updatedAt) {
}
