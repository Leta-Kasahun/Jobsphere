package com.jobsphere.jobsite.dto.job;

import com.jobsphere.jobsite.constant.JobConstants;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record JobUpdateRequest(
    @Size(max = JobConstants.TITLE_MAX_LENGTH)
    String title,

    String description,

    @Size(max = JobConstants.JOB_TYPE_MAX_LENGTH)
    String jobType,

    @Size(max = JobConstants.WORKPLACE_TYPE_MAX_LENGTH)
    String workplaceType,

    @Size(max = JobConstants.CATEGORY_MAX_LENGTH)
    String category,

    @Size(max = JobConstants.EDUCATION_LEVEL_MAX_LENGTH)
    String educationLevel,

    @Size(max = JobConstants.GENDER_REQUIREMENT_MAX_LENGTH)
    String genderRequirement,

    @Min(value = JobConstants.VACANCY_COUNT_MIN)
    Integer vacancyCount,

    @Size(max = JobConstants.EXPERIENCE_LEVEL_MAX_LENGTH)
    String experienceLevel,

    String experienceDescription,

    @DecimalMin(value = "0.0")
    BigDecimal salaryMin,

    @DecimalMin(value = "0.0")
    BigDecimal salaryMax,

    LocalDate deadline,

    UUID addressId
) {}
