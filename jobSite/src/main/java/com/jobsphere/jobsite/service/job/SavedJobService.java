package com.jobsphere.jobsite.service.job;

import com.jobsphere.jobsite.exception.ResourceNotFoundException;
import com.jobsphere.jobsite.model.User;
import com.jobsphere.jobsite.model.job.Job;
import com.jobsphere.jobsite.model.job.SavedJob;
import com.jobsphere.jobsite.repository.UserRepository;
import com.jobsphere.jobsite.repository.job.JobRepository;
import com.jobsphere.jobsite.repository.job.SavedJobRepository;
import com.jobsphere.jobsite.service.shared.AuthenticationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class SavedJobService {
    private final SavedJobRepository savedJobRepository;
    private final JobRepository jobRepository;
    private final UserRepository userRepository;
    private final AuthenticationService authenticationService;
    private final JobService jobService;

    @Transactional
    public boolean toggleSave(UUID jobId) {
        UUID userId = authenticationService.getCurrentUserId();

        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return savedJobRepository.findByJobIdAndUserId(jobId, userId)
                .map(savedJob -> {
                    savedJobRepository.delete(savedJob);
                    log.info("Job {} unsaved by user {}", jobId, userId);
                    return false;
                })
                .orElseGet(() -> {
                    SavedJob savedJob = SavedJob.builder()
                            .job(job)
                            .user(user)
                            .build();
                    savedJobRepository.save(savedJob);
                    log.info("Job {} saved by user {}", jobId, userId);
                    return true;
                });
    }

    @Transactional(readOnly = true)
    public boolean isSaved(UUID jobId) {
        try {
            UUID userId = authenticationService.getCurrentUserId();
            return savedJobRepository.existsByJobIdAndUserId(jobId, userId);
        } catch (Exception e) {
            return false;
        }
    }

    @Transactional(readOnly = true)
    public Page<com.jobsphere.jobsite.dto.job.JobResponse> getSavedJobsAsResponses(Pageable pageable) {
        UUID userId = authenticationService.getCurrentUserId();
        return savedJobRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable)
                .map(savedJob -> jobService.mapToResponse(savedJob.getJob()));
    }
}
