package com.jobsphere.jobsite.dto.admin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record CompanyVerificationReviewRequest(
    @NotNull(message = "Action is required")
    @Pattern(regexp = "APPROVE|REJECT", message = "Action must be either APPROVE or REJECT")
    String action,

    String rejectionReason
) {
    public CompanyVerificationReviewRequest {
        if ("REJECT".equals(action) && (rejectionReason == null || rejectionReason.trim().isEmpty())) {
            throw new IllegalArgumentException("Rejection reason is required when rejecting verification");
        }
    }
}
