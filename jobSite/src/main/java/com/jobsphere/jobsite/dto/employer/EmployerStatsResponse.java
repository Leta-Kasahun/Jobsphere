package com.jobsphere.jobsite.dto.employer;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployerStatsResponse {
    private long totalJobs;
    private long activeJobs;
    private long totalApplicants;
    private long hiredApplicants;
    private Map<String, Long> applicationGrowth; // Date -> Count
    private Map<String, Long> statusDistribution; // Status -> Count
    private Map<String, Long> categoryDistribution; // Category -> Count
}
