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
import com.jobsphere.jobsite.service.shared.AuthenticationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class JobService {
    private final JobRepository jobRepository;
    private final CompanyProfileRepository companyProfileRepository;
    private final AddressRepository addressRepository;
    private final AuthenticationService authenticationService;

    @Transactional
    public JobResponse createJob(JobCreateRequest request) {
        UUID userId = authenticationService.getCurrentUserId();
        CompanyProfile companyProfile = companyProfileRepository.findByUserId(userId)
            .orElseThrow(() -> new IllegalStateException("Company profile not found"));

        Address address = null;
        if (request.addressId() != null) {
            address = addressRepository.findById(request.addressId())
                .orElseThrow(() -> new ResourceNotFoundException("Address not found"));
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
            .deadline(request.deadline())
            .isActive(true)
            .build();

        job = jobRepository.save(job);
        log.info("Job created: {} for company {}", job.getId(), companyProfile.getCompanyName());

        return mapToResponse(job);
    }

    public Page<JobResponse> listJobs(String category, String jobType, String workplaceType, String city, Pageable pageable) {
        Page<Job> jobs = jobRepository.findActiveJobsWithFilters(category, jobType, workplaceType, city, pageable);
        return jobs.map(this::mapToResponse);
    }

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
        jobRepository.save(job);
        log.info("Job deactivated: {}", job.getId());
    }

    private JobResponse mapToResponse(Job job) {
        return new JobResponse(
            job.getId(),
            job.getCompanyProfile().getId(),
            job.getCompanyProfile().getCompanyName(),
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
            job.getDeadline(),
            job.getIsActive(),
            job.getCreatedAt(),
            job.getUpdatedAt()
        );
    }
}
