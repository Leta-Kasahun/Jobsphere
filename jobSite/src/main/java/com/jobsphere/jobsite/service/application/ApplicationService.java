package com.jobsphere.jobsite.service.application;

import com.jobsphere.jobsite.dto.application.ApplicationCreateRequest;
import com.jobsphere.jobsite.dto.application.ApplicationResponse;
import com.jobsphere.jobsite.dto.application.ApplicationUpdateRequest;
import com.jobsphere.jobsite.exception.ResourceNotFoundException;
import com.jobsphere.jobsite.model.application.Application;
import com.jobsphere.jobsite.model.job.Job;
import com.jobsphere.jobsite.model.seeker.Seeker;
import com.jobsphere.jobsite.repository.application.ApplicationRepository;
import com.jobsphere.jobsite.repository.job.JobRepository;
import com.jobsphere.jobsite.repository.seeker.SeekerRepository;
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
public class ApplicationService {
    private final ApplicationRepository applicationRepository;
    private final JobRepository jobRepository;
    private final SeekerRepository seekerRepository;
    private final AuthenticationService authenticationService;

    @Transactional
    public ApplicationResponse applyForJob(ApplicationCreateRequest request) {
        UUID seekerId = authenticationService.getCurrentUserId();

        Job job = jobRepository.findActiveJobWithCompanyProfile(request.jobId());
        if (job == null) {
            throw new ResourceNotFoundException("Job not found or not active");
        }

        if ("CLOSED".equals(job.getStatus()) || "HIRED".equals(job.getStatus())) {
            throw new IllegalStateException("This job is no longer accepting applications");
        }

        Seeker seeker = seekerRepository.findById(seekerId)
            .orElseThrow(() -> new ResourceNotFoundException("Seeker profile not found"));

        if (applicationRepository.existsByJobIdAndSeekerId(request.jobId(), seekerId)) {
            throw new IllegalStateException("You have already applied for this job");
        }

        Application application = Application.builder()
            .job(job)
            .seeker(seeker)
            .coverLetter(request.coverLetter())
            .expectedSalary(request.expectedSalary())
            .build();

        application = applicationRepository.save(application);
        log.info("Application created: {} for job {} by seeker {}", application.getId(), job.getId(), seekerId);

        return mapToResponse(application);
    }

    public Page<ApplicationResponse> getApplicationsByJob(UUID jobId, Pageable pageable) {
        UUID userId = authenticationService.getCurrentUserId();
        Job job = jobRepository.findById(jobId)
            .orElseThrow(() -> new ResourceNotFoundException("Job not found"));

        if (!job.getCompanyProfile().getUserId().equals(userId)) {
            throw new IllegalStateException("You can only view applications for your own jobs");
        }

        Page<Application> applications = applicationRepository.findByJobId(jobId, pageable);
        return applications.map(this::mapToResponse);
    }

    public Page<ApplicationResponse> getMyApplications(Pageable pageable) {
        UUID seekerId = authenticationService.getCurrentUserId();
        Page<Application> applications = applicationRepository.findBySeekerId(seekerId, pageable);
        return applications.map(this::mapToResponse);
    }

    public ApplicationResponse getApplication(UUID applicationId) {
        Application application = applicationRepository.findById(applicationId)
            .orElseThrow(() -> new ResourceNotFoundException("Application not found"));

        UUID userId = authenticationService.getCurrentUserId();
        boolean isSeeker = application.getSeeker().getId().equals(userId);
        boolean isEmployer = application.getJob().getCompanyProfile().getUserId().equals(userId);

        if (!isSeeker && !isEmployer) {
            throw new IllegalStateException("You can only view your own applications or applications for your jobs");
        }

        return mapToResponse(application);
    }

    @Transactional
    public ApplicationResponse updateApplicationStatus(UUID applicationId, ApplicationUpdateRequest request) {
        UUID userId = authenticationService.getCurrentUserId();
        Application application = applicationRepository.findById(applicationId)
            .orElseThrow(() -> new ResourceNotFoundException("Application not found"));

        if (!application.getJob().getCompanyProfile().getUserId().equals(userId)) {
            throw new IllegalStateException("You can only update applications for your own jobs");
        }

        if (StringUtils.hasText(request.status())) {
            application.setStatus(request.status().toUpperCase());
            application.setReviewedAt(Instant.now());
        }

        if (request.hiredFlag() != null) {
            boolean wasHired = application.getHiredFlag() != null && application.getHiredFlag();
            boolean isNowHired = request.hiredFlag();

            application.setHiredFlag(isNowHired);

            if (!wasHired && isNowHired) {
                updateJobFilledCount(application.getJob().getId(), 1);
            } else if (wasHired && !isNowHired) {
                updateJobFilledCount(application.getJob().getId(), -1);
            }
        }

        if (request.notes() != null) {
            application.setNotes(request.notes());
        }

        application = applicationRepository.save(application);
        log.info("Application updated: {} status: {}", applicationId, application.getStatus());

        return mapToResponse(application);
    }

    private void updateJobFilledCount(UUID jobId, int delta) {
        Job job = jobRepository.findById(jobId)
            .orElseThrow(() -> new ResourceNotFoundException("Job not found"));

        int newFilledCount = (job.getFilledCount() != null ? job.getFilledCount() : 0) + delta;
        newFilledCount = Math.max(0, newFilledCount);

        job.setFilledCount(newFilledCount);

        if (newFilledCount > 0) {
            if (job.getVacancyCount() != null && newFilledCount >= job.getVacancyCount()) {
                job.setStatus("HIRED");
            } else {
                job.setStatus("OPEN");
            }
        } else {
            job.setStatus("OPEN");
        }

        jobRepository.save(job);
        log.info("Updated job {} filled count to {} and status to {}", jobId, newFilledCount, job.getStatus());
    }

    private ApplicationResponse mapToResponse(Application application) {
        return new ApplicationResponse(
            application.getId(),
            application.getJob().getId(),
            application.getJob().getTitle(),
            application.getSeeker().getId(),
            application.getSeeker().getFirstName() + " " + application.getSeeker().getLastName(),
            application.getCoverLetter(),
            application.getExpectedSalary(),
            application.getStatus(),
            application.getAppliedAt(),
            application.getReviewedAt(),
            application.getHiredFlag(),
            application.getNotes(),
            application.getUpdatedAt()
        );
    }
}
