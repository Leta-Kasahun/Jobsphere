package com.jobsphere.jobsite.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminStatsResponse {
    private long totalUsers;
    private long totalJobs;
    private double totalRevenue;
    private long pendingVerifications;
    private Map<String, Long> userGrowth; // Date -> Count
    private Map<String, Long> jobGrowth; // Date -> Count
    private Map<String, Double> revenueGrowth; // Date -> Amount
    private Map<String, Long> userTypeDistribution; // Role -> Count
}
