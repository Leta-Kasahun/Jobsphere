package com.jobsphere.jobsite.service.job;

import com.jobsphere.jobsite.dto.job.JobCreateRequest;
import com.jobsphere.jobsite.dto.job.JobResponse;
import com.jobsphere.jobsite.dto.job.JobUpdateRequest;
import com.jobsphere.jobsite.exception.ResourceNotFoundException;
import com.jobsphere.jobsite.model.employer.CompanyProfile;
import com.jobsphere.jobsite.model.job.Job;
import com.jobsphere.jobsite.model.shared.Address;
import com.jobsphere.jobsite.repository.employer.CompanyProfileRepository;
import com.jobsphere.jobsite.repository.job.JobRepository;
import com.jobsphere.jobsite.repository.shared.AddressRepository;
import com.jobsphere.jobsite.repository.application.ApplicationRepository;
import com.jobsphere.jobsite.repository.employer.CompanyVerificationRepository;
import com.jobsphere.jobsite.repository.seeker.JobAlertRepository;
import com.jobsphere.jobsite.service.notification.NotificationService;
import com.jobsphere.jobsite.service.shared.AuthenticationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class JobService {
    private final JobRepository jobRepository;
    private final CompanyProfileRepository companyProfileRepository;
    private final AddressRepository addressRepository;
    private final AuthenticationService authenticationService;
    private final ApplicationRepository applicationRepository;
    private final JobAlertRepository jobAlertRepository;
    private final CompanyVerificationRepository verificationRepository;
    private final NotificationService notificationService;

    @Transactional
    public JobResponse createJob(JobCreateRequest request) {
        UUID userId = authenticationService.getCurrentUserId();
        return createJobForUser(request, userId, false);
    }

    @Transactional
    public JobResponse createJobForUser(JobCreateRequest request, UUID userId, boolean paymentVerified) {
        log.info("Creating job for user: {} with title: {}", userId, request.title());

        // Enforce Company Verification
        com.jobsphere.jobsite.model.employer.CompanyVerification verification = verificationRepository
                .findByUserIdAndStatus(userId, "APPROVED")
                .orElseThrow(() -> new IllegalStateException(
                        "Your company account must be verified before posting jobs. Please complete the verification process in your profile."));

        if (Boolean.FALSE.equals(verification.getCodeUsed())) {
            throw new IllegalStateException(
                    "You must complete the verification process by entering the code sent to your email before posting jobs.");
        }

        CompanyProfile companyProfile = companyProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalStateException(
                        "Company profile matching user not found. Please complete your company profile first."));

        Address address = null;
        if (request.addressId() != null) {
            address = addressRepository.findById(request.addressId())
                    .orElseThrow(() -> new ResourceNotFoundException("Address not found"));
        } else if (request.country() != null) {
            address = Address.builder()
                    .country(request.country())
                    .region(request.region())
                    .createdAt(Instant.now())
                    .build();
            address = addressRepository.save(address);
        }

        Job job = Job.builder()
                .companyProfile(companyProfile)
                .address(address)
                .title(request.title())
                .description(request.description())
                .jobType(request.jobType())
                .workplaceType(request.workplaceType())
                .category(request.category())
                .educationLevel(request.educationLevel())
                .genderRequirement(request.genderRequirement())
                .vacancyCount(request.vacancyCount())
                .experienceLevel(request.experienceLevel())
                .experienceDescription(request.experienceDescription())
                .salaryMin(request.salaryMin())
                .salaryMax(request.salaryMax())
                .compensationType(request.compensationType())
                .currency(request.currency())
                .deadline(request.deadline())
                .isActive(true)
                .paymentVerified(paymentVerified)
                .build();

        job = jobRepository.save(job);
        log.info("Job created: {} for company {}", job.getId(), companyProfile.getCompanyName());

        // Notify matching seekers
        final Job savedJob = job;
        jobAlertRepository.findByIsActiveTrue().stream()
                .filter(alert -> isMatch(savedJob, alert))
                .forEach(alert -> notificationService.notifyJobAlertMatch(
                        alert.getSeeker().getId(),
                        savedJob.getTitle(),
                        companyProfile.getCompanyName()));

        return mapToResponse(job);
    }

    private boolean isMatch(Job job, com.jobsphere.jobsite.model.seeker.JobAlert alert) {
        // 1. Category match (more lenient)
        if (alert.getCategory() != null && !alert.getCategory().isEmpty()) {
            String alertCat = alert.getCategory();
            String jobCat = job.getCategory();

            if (jobCat != null) {
                boolean match = alertCat.equalsIgnoreCase(jobCat) ||
                        isCategoryRelated(alertCat, jobCat);
                if (!match)
                    return false;
            }
        }

        // 2. Job Type match
        if (alert.getJobType() != null && !alert.getJobType().isEmpty()) {
            if (!alert.getJobType().equalsIgnoreCase(job.getJobType()))
                return false;
        }

        // 3. Location match
        if (alert.getPreferredLocation() != null && !alert.getPreferredLocation().isEmpty()) {
            String jobCity = job.getAddress() != null ? job.getAddress().getCity() : "";
            String jobRegion = job.getAddress() != null ? job.getAddress().getRegion() : "";
            boolean cityMatch = jobCity != null
                    && jobCity.toLowerCase().contains(alert.getPreferredLocation().toLowerCase());
            boolean regionMatch = jobRegion != null
                    && jobRegion.toLowerCase().contains(alert.getPreferredLocation().toLowerCase());
            if (!cityMatch && !regionMatch)
                return false;
        }

        // 4. Keyword match
        if (alert.getKeywords() != null && !alert.getKeywords().isEmpty()) {
            String[] keywords = alert.getKeywords().split(",");
            boolean keywordMatched = false;
            for (String kw : keywords) {
                String cleanKw = kw.trim().toLowerCase();
                if (cleanKw.isEmpty())
                    continue;
                if ((job.getTitle() != null && job.getTitle().toLowerCase().contains(cleanKw)) ||
                        (job.getDescription() != null && job.getDescription().toLowerCase().contains(cleanKw))) {
                    keywordMatched = true;
                    break;
                }
            }
            if (!keywordMatched)
                return false;
        }

        return true;
    }

    private boolean isCategoryRelated(String alertCat, String jobCat) {
        String a = alertCat.toLowerCase();
        String j = jobCat.toLowerCase();

        // Technology Mapping
        if ((a.contains("tech") || a.contains("it")) && j.equals("technology"))
            return true;
        // Marketing/Sales Mapping
        if ((a.contains("marketing") || a.contains("sales")) && j.equals("marketing"))
            return true;
        // Design Mapping
        if (a.contains("design") && j.equals("design"))
            return true;
        // Healthcare Mapping
        if (a.contains("health") && j.equals("healthcare"))
            return true;
        // Finance Mapping
        if ((a.contains("finance") || a.contains("banking")) && j.equals("finance"))
            return true;
        // Education Mapping
        if (a.contains("education") && j.equals("education"))
            return true;
        // Engineering Mapping
        if (a.contains("engineering") && j.equals("engineering"))
            return true;

        return a.contains(j) || j.contains(a);
    }

    @Transactional(readOnly = true)
    public Page<JobResponse> getEmployerJobs(Pageable pageable) {
        UUID userId = authenticationService.getCurrentUserId();
        var companyProfileOpt = companyProfileRepository.findByUserId(userId);

        if (companyProfileOpt.isEmpty()) {
            return Page.empty(pageable);
        }

        return jobRepository.findByCompanyProfileId(companyProfileOpt.get().getId(), pageable)
                .map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public Page<JobResponse> listJobs(String category, String jobType, String workplaceType, String location,
            Boolean isFeatured, Pageable pageable) {
        log.info("Fetching jobs with filters - category: {}, type: {}, workplace: {}, location: {}, featured: {}",
                category, jobType, workplaceType, location, isFeatured);
        Page<Job> jobs = jobRepository.findActiveJobsWithFilters(category, jobType, workplaceType, location, isFeatured,
                pageable);
        log.info("Found {} jobs", jobs.getTotalElements());
        return jobs.map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public JobResponse getJob(UUID jobId) {
        Job job = jobRepository.findActiveJobWithCompanyProfile(jobId);
        if (job == null) {
            throw new ResourceNotFoundException("Job not found");
        }
        return mapToResponse(job);
    }

    @Transactional
    public JobResponse updateJob(UUID jobId, JobUpdateRequest request) {
        UUID userId = authenticationService.getCurrentUserId();
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found"));

        if (!job.getCompanyProfile().getUserId().equals(userId)) {
            throw new IllegalStateException("You can only update your own jobs");
        }

        if (StringUtils.hasText(request.title())) {
            job.setTitle(request.title());
        }
        if (StringUtils.hasText(request.description())) {
            job.setDescription(request.description());
        }
        if (StringUtils.hasText(request.jobType())) {
            job.setJobType(request.jobType());
        }
        if (StringUtils.hasText(request.workplaceType())) {
            job.setWorkplaceType(request.workplaceType());
        }
        if (StringUtils.hasText(request.category())) {
            job.setCategory(request.category());
        }
        if (StringUtils.hasText(request.educationLevel())) {
            job.setEducationLevel(request.educationLevel());
        }
        if (request.genderRequirement() != null) {
            job.setGenderRequirement(request.genderRequirement());
        }
        if (request.vacancyCount() != null) {
            job.setVacancyCount(request.vacancyCount());
        }
        if (StringUtils.hasText(request.experienceLevel())) {
            job.setExperienceLevel(request.experienceLevel());
        }
        if (request.experienceDescription() != null) {
            job.setExperienceDescription(request.experienceDescription());
        }
        if (request.salaryMin() != null) {
            job.setSalaryMin(request.salaryMin());
        }
        if (request.salaryMax() != null) {
            job.setSalaryMax(request.salaryMax());
        }
        if (StringUtils.hasText(request.compensationType())) {
            job.setCompensationType(request.compensationType());
        }
        if (StringUtils.hasText(request.currency())) {
            job.setCurrency(request.currency());
        }
        if (request.deadline() != null) {
            job.setDeadline(request.deadline());
        }
        if (request.addressId() != null) {
            Address address = addressRepository.findById(request.addressId())
                    .orElseThrow(() -> new ResourceNotFoundException("Address not found"));
            job.setAddress(address);
        }

        job = jobRepository.save(job);
        log.info("Job updated: {}", job.getId());

        return mapToResponse(job);
    }

    @Transactional
    public void deactivateJob(UUID jobId) {
        UUID userId = authenticationService.getCurrentUserId();
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found"));

        if (!job.getCompanyProfile().getUserId().equals(userId)) {
            throw new IllegalStateException("You can only deactivate your own jobs");
        }

        job.setIsActive(false);
        job.setStatus("CLOSED");
        jobRepository.save(job);
        log.info("Job deactivated: {}", job.getId());
    }

    @Transactional
    public JobResponse updateJobStatus(UUID jobId, String status) {
        UUID userId = authenticationService.getCurrentUserId();
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found"));

        if (!job.getCompanyProfile().getUserId().equals(userId)) {
            throw new IllegalStateException("You can only update your own jobs");
        }

        validateStatusTransition(job.getStatus(), status);

        job.setStatus(status);
        job = jobRepository.save(job);
        log.info("Job status updated: {} to {}", job.getId(), status);

        return mapToResponse(job);
    }

    private void validateStatusTransition(String currentStatus, String newStatus) {
        switch (currentStatus) {
            case "OPEN":
                if (!"CLOSED".equals(newStatus)) {
                    throw new IllegalStateException("OPEN jobs can only be changed to CLOSED");
                }
                break;
            case "CLOSED":
                if (!"OPEN".equals(newStatus)) {
                    throw new IllegalStateException("CLOSED jobs can only be changed to OPEN");
                }
                break;
            case "HIRED":
                throw new IllegalStateException("HIRED jobs cannot have their status manually changed");
            default:
                throw new IllegalStateException("Invalid current status: " + currentStatus);
        }

        if (!"OPEN".equals(newStatus) && !"CLOSED".equals(newStatus)) {
            throw new IllegalStateException("Status must be either OPEN or CLOSED");
        }
    }

    public JobResponse mapToResponse(Job job) {
        long applicantCount = applicationRepository.countByJobId(job.getId());

        // Check if company is verified
        boolean companyVerified = verificationRepository
                .findByUserIdAndStatus(job.getCompanyProfile().getUserId(), "APPROVED")
                .map(v -> Boolean.TRUE.equals(v.getCodeUsed()))
                .orElse(false);

        return new JobResponse(
                job.getId(),
                job.getCompanyProfile().getId(),
                job.getCompanyProfile().getCompanyName(),
                job.getCompanyProfile().getLogoUrl(),
                job.getAddress() != null ? job.getAddress().getId() : null,
                job.getAddress() != null ? job.getAddress().getCountry() : null,
                job.getAddress() != null ? job.getAddress().getRegion() : null,
                job.getAddress() != null ? job.getAddress().getCity() : null,
                job.getAddress() != null ? job.getAddress().getSubCity() : null,
                job.getAddress() != null ? job.getAddress().getStreet() : null,
                job.getTitle(),
                job.getDescription(),
                job.getJobType(),
                job.getWorkplaceType(),
                job.getCategory(),
                job.getEducationLevel(),
                job.getGenderRequirement(),
                job.getVacancyCount(),
                job.getExperienceLevel(),
                job.getExperienceDescription(),
                job.getSalaryMin(),
                job.getSalaryMax(),
                job.getCompensationType(),
                job.getCurrency(),
                job.getDeadline(),
                job.getIsActive(),
                job.getStatus(),
                job.getFilledCount(),
                (int) applicantCount,
                job.getIsFeatured(),
                job.getPaymentVerified(),
                companyVerified,
                job.getCreatedAt(),
                job.getUpdatedAt());
    }
}
