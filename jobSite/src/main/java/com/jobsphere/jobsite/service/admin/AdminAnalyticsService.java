package com.jobsphere.jobsite.service.admin;

import com.jobsphere.jobsite.constant.UserType;
import com.jobsphere.jobsite.dto.admin.AdminStatsResponse;
import com.jobsphere.jobsite.repository.UserRepository;
import com.jobsphere.jobsite.repository.admin.AdminRepository;
import com.jobsphere.jobsite.repository.employer.CompanyVerificationRepository;
import com.jobsphere.jobsite.repository.job.JobRepository;
import com.jobsphere.jobsite.repository.payment.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminAnalyticsService {

    private final UserRepository userRepository;
    private final JobRepository jobRepository;
    private final PaymentRepository paymentRepository;
    private final CompanyVerificationRepository verificationRepository;
    private final AdminRepository adminRepository;

    public AdminStatsResponse getDashboardStats() {
        log.info("Generating Etworks Admin Analytics Payload...");
        AdminStatsResponse stats = new AdminStatsResponse();

        try {
            // Basic Stats
            stats.setTotalUsers(userRepository.count());
            stats.setTotalJobs(jobRepository.count());
            stats.setPendingVerifications(verificationRepository.countByStatus("PENDING"));

            // Calculate Total Revenue accurately
            BigDecimal totalRevenue = paymentRepository.sumTotalRevenue();
            stats.setTotalRevenue(totalRevenue != null ? totalRevenue.doubleValue() : 0.0);

            // Growth Data (Last 7 Days)
            stats.setUserGrowth(getRecentUserGrowth());
            stats.setJobGrowth(getRecentJobGrowth());
            stats.setRevenueGrowth(getRecentRevenueGrowth());

            // User Type Distribution
            Map<String, Long> distribution = new HashMap<>();
            distribution.put("Seekers", userRepository.countByUserType(UserType.SEEKER));
            distribution.put("Employers", userRepository.countByUserType(UserType.EMPLOYER));
            distribution.put("Admins", adminRepository.count());
            stats.setUserTypeDistribution(distribution);

            log.info("Analytics Payload generated successfully: {} users, {} jobs", stats.getTotalUsers(),
                    stats.getTotalJobs());
        } catch (Exception e) {
            log.error("Failed to generate analytics payload: {}", e.getMessage());
        }

        return stats;
    }

    private Map<String, Long> getRecentUserGrowth() {
        Map<String, Long> growth = new LinkedHashMap<>();
        Instant now = Instant.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd").withZone(ZoneId.systemDefault());

        for (int i = 6; i >= 0; i--) {
            // Start of day i days ago
            Instant start = now.minus(i, ChronoUnit.DAYS).truncatedTo(ChronoUnit.DAYS);
            // End of that day
            Instant end = start.plus(1, ChronoUnit.DAYS);
            String dateLabel = formatter.format(start);

            long count = userRepository.countByCreatedAtBetween(start, end);
            growth.put(dateLabel, count);
        }
        return growth;
    }

    private Map<String, Long> getRecentJobGrowth() {
        Map<String, Long> growth = new LinkedHashMap<>();
        Instant now = Instant.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd").withZone(ZoneId.systemDefault());

        for (int i = 6; i >= 0; i--) {
            Instant start = now.minus(i, ChronoUnit.DAYS).truncatedTo(ChronoUnit.DAYS);
            Instant end = start.plus(1, ChronoUnit.DAYS);
            String dateLabel = formatter.format(start);

            long count = jobRepository.countByCreatedAtBetween(start, end);
            growth.put(dateLabel, count);
        }
        return growth;
    }

    private Map<String, Double> getRecentRevenueGrowth() {
        Map<String, Double> growth = new LinkedHashMap<>();
        // Use UTC for internal comparisons as per properties
        LocalDateTime nowUtc = LocalDateTime.now(ZoneId.of("UTC"));
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd");

        for (int i = 6; i >= 0; i--) {
            LocalDateTime start = nowUtc.minusDays(i).withHour(0).withMinute(0).withSecond(0).withNano(0);
            LocalDateTime end = start.plusDays(1);
            String dateLabel = formatter.format(start);

            BigDecimal amount = paymentRepository.sumRevenueBetween(start, end);
            growth.put(dateLabel, amount != null ? amount.doubleValue() : 0.0);
        }
        return growth;
    }
}
