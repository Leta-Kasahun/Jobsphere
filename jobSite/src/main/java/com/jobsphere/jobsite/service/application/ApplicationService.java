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
import com.jobsphere.jobsite.repository.seeker.SeekerSectorRepository;
import com.jobsphere.jobsite.repository.seeker.SeekerSkillRepository;
import com.jobsphere.jobsite.repository.seeker.SeekerTagRepository;
import com.jobsphere.jobsite.model.seeker.SeekerSkill;
import com.jobsphere.jobsite.model.seeker.SeekerSector;
import com.jobsphere.jobsite.model.seeker.SeekerTag;
import com.jobsphere.jobsite.service.shared.AuthenticationService;
import com.jobsphere.jobsite.service.notification.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.UUID;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ApplicationService {
    private final ApplicationRepository applicationRepository;
    private final JobRepository jobRepository;
    private final SeekerRepository seekerRepository;
    private final SeekerSkillRepository skillRepository;
    private final SeekerSectorRepository sectorRepository;
    private final SeekerTagRepository tagRepository;
    private final AuthenticationService authenticationService;
    private final NotificationService notificationService;

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

        notificationService.notifyNewApplication(job.getCompanyProfile().getUserId(), job.getTitle());

        return mapToResponse(application);
    }

    @Transactional(readOnly = true)
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

    @Transactional(readOnly = true)
    public Page<ApplicationResponse> getMyApplications(Pageable pageable) {
        UUID seekerId = authenticationService.getCurrentUserId();
        log.info("Fetching applications for seekerId: {}", seekerId);
        Page<Application> applications = applicationRepository.findBySeekerId(seekerId, pageable);
        log.info("Found {} applications for seekerId: {}", applications.getTotalElements(), seekerId);
        return applications.map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
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

    @Transactional(readOnly = true)
    public boolean hasApplied(UUID jobId) {
        try {
            UUID seekerId = authenticationService.getCurrentUserId();
            return applicationRepository.existsByJobIdAndSeekerId(jobId, seekerId);
        } catch (Exception e) {
            return false;
        }
    }

    @Transactional
    public ApplicationResponse updateApplicationStatus(UUID applicationId, ApplicationUpdateRequest request) {
        UUID userId = authenticationService.getCurrentUserId();
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found"));

        if (!application.getJob().getCompanyProfile().getUserId().equals(userId)) {
            throw new IllegalStateException("You can only update applications for your own jobs");
        }

        String oldStatus = application.getStatus();
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

        if (!oldStatus.equals(application.getStatus())) {
            notificationService.notifyApplicationStatusUpdate(
                    application.getSeeker().getId(),
                    application.getJob().getTitle(),
                    application.getStatus());
        }

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
        Seeker seeker = application.getSeeker();
        List<String> skills = skillRepository.findBySeekerId(seeker.getId())
                .stream().map(SeekerSkill::getSkill).toList();
        List<String> sectors = sectorRepository.findBySeekerId(seeker.getId())
                .stream().map(SeekerSector::getSector).toList();
        List<String> tags = tagRepository.findBySeekerId(seeker.getId())
                .stream().map(SeekerTag::getTag).toList();

        return new ApplicationResponse(
                application.getId(),
                application.getJob().getId(),
                application.getJob().getTitle(),
                seeker.getId(),
                seeker.getFirstName() + " " + seeker.getLastName(),
                seeker.getCvUrl(),
                skills,
                sectors,
                tags,
                application.getCoverLetter(),
                application.getExpectedSalary(),
                application.getStatus(),
                application.getAppliedAt(),
                application.getReviewedAt(),
                application.getHiredFlag(),
                application.getNotes(),
                application.getUpdatedAt());
    }
}
