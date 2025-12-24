package com.jobsphere.jobsite.service.employer;

import com.jobsphere.jobsite.dto.employer.EmployerStatsResponse;
import com.jobsphere.jobsite.exception.ResourceNotFoundException;
import com.jobsphere.jobsite.model.employer.CompanyProfile;
import com.jobsphere.jobsite.repository.application.ApplicationRepository;
import com.jobsphere.jobsite.repository.employer.CompanyProfileRepository;
import com.jobsphere.jobsite.repository.job.JobRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EmployerAnalyticsService {

    private final JobRepository jobRepository;
    private final ApplicationRepository applicationRepository;
    private final CompanyProfileRepository companyProfileRepository;

    public EmployerStatsResponse getStats(UUID userId) {
        CompanyProfile profile = companyProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Company profile not found for user: " + userId));

        UUID companyProfileId = profile.getId();

        EmployerStatsResponse stats = new EmployerStatsResponse();
        stats.setTotalJobs(jobRepository.countByCompanyProfileId(companyProfileId));
        stats.setActiveJobs(jobRepository.countByCompanyProfileIdAndIsActiveTrue(companyProfileId));
        stats.setTotalApplicants(applicationRepository.countByCompanyProfileId(companyProfileId));
        stats.setHiredApplicants(applicationRepository.countByCompanyProfileIdAndStatus(companyProfileId, "HIRED"));

        // Growth Data (Last 7 Days)
        stats.setApplicationGrowth(getRecentApplicationGrowth(companyProfileId));

        // Status Distribution
        stats.setStatusDistribution(getStatusDistribution(companyProfileId));

        // Category Distribution
        stats.setCategoryDistribution(getCategoryDistribution(companyProfileId));

        return stats;
    }

    private Map<String, Long> getRecentApplicationGrowth(UUID companyProfileId) {
        Map<String, Long> growth = new LinkedHashMap<>();
        Instant now = Instant.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd").withZone(ZoneId.systemDefault());

        for (int i = 6; i >= 0; i--) {
            Instant start = now.minus(i, ChronoUnit.DAYS).truncatedTo(ChronoUnit.DAYS);
            Instant end = start.plus(1, ChronoUnit.DAYS);
            String dateLabel = formatter.format(start);

            long count = applicationRepository.countByCompanyProfileIdAndAppliedAtBetween(companyProfileId, start, end);
            growth.put(dateLabel, count);
        }
        return growth;
    }

    private Map<String, Long> getStatusDistribution(UUID companyProfileId) {
        Map<String, Long> distribution = new HashMap<>();
        String[] statuses = { "PENDING", "REVIEWED", "REJECTED", "HIRED" };
        for (String status : statuses) {
            distribution.put(status, applicationRepository.countByCompanyProfileIdAndStatus(companyProfileId, status));
        }
        return distribution;
    }

    private Map<String, Long> getCategoryDistribution(UUID companyProfileId) {
        Map<String, Long> distribution = new HashMap<>();
        List<Object[]> results = jobRepository.countJobsByCategory(companyProfileId);
        for (Object[] result : results) {
            distribution.put((String) result[0], (Long) result[1]);
        }
        return distribution;
    }
}
