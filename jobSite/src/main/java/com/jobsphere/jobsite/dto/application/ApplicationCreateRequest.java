package com.jobsphere.jobsite.dto.application;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.UUID;

public record ApplicationCreateRequest(
        @NotNull(message = "Job ID is required") UUID jobId,

        @NotBlank(message = "Cover letter is required") @Size(max = 10000, message = "Cover letter cannot exceed 10,000 characters") String coverLetter,

        @NotNull(message = "Expected salary is required") @jakarta.validation.constraints.Positive(message = "Expected salary must be greater than zero") BigDecimal expectedSalary) {
}
