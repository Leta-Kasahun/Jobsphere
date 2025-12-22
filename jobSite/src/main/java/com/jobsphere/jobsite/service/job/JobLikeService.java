package com.jobsphere.jobsite.service.job;

import com.jobsphere.jobsite.exception.ResourceNotFoundException;
import com.jobsphere.jobsite.model.User;
import com.jobsphere.jobsite.model.job.Job;
import com.jobsphere.jobsite.model.job.JobLike;
import com.jobsphere.jobsite.repository.UserRepository;
import com.jobsphere.jobsite.repository.job.JobLikeRepository;
import com.jobsphere.jobsite.repository.job.JobRepository;
import com.jobsphere.jobsite.repository.seeker.SeekerRepository;
import com.jobsphere.jobsite.service.notification.NotificationService;
import com.jobsphere.jobsite.service.shared.AuthenticationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class JobLikeService {
    private final JobLikeRepository jobLikeRepository;
    private final JobRepository jobRepository;
    private final UserRepository userRepository;
    private final SeekerRepository seekerRepository;
    private final AuthenticationService authenticationService;
    private final NotificationService notificationService;

    @Transactional
    public boolean toggleLike(UUID jobId) {
        UUID userId = authenticationService.getCurrentUserId();

        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return jobLikeRepository.findByJobIdAndUserId(jobId, userId)
                .map(like -> {
                    jobLikeRepository.delete(like);
                    log.info("Job {} unliked by user {}", jobId, userId);
                    return false;
                })
                .orElseGet(() -> {
                    JobLike like = JobLike.builder()
                            .job(job)
                            .user(user)
                            .build();
                    jobLikeRepository.save(like);
                    log.info("Job {} liked by user {}", jobId, userId);

                    // Notify employer
                    String likerName = user.getEmail();
                    try {
                        com.jobsphere.jobsite.model.seeker.Seeker seeker = seekerRepository.findById(userId)
                                .orElse(null);
                        if (seeker != null && seeker.getFirstName() != null) {
                            likerName = seeker.getFirstName() + " "
                                    + (seeker.getLastName() != null ? seeker.getLastName() : "");
                        }
                    } catch (Exception e) {
                        log.warn("Could not fetch seeker name for like notification", e);
                    }

                    notificationService.notifyJobLike(
                            job.getCompanyProfile().getUserId(),
                            job.getTitle(),
                            likerName);

                    return true;
                });
    }

    @Transactional(readOnly = true)
    public boolean hasLiked(UUID jobId) {
        UUID userId = authenticationService.getCurrentUserId();
        return jobLikeRepository.existsByJobIdAndUserId(jobId, userId);
    }

    @Transactional(readOnly = true)
    public long getLikeCount(UUID jobId) {
        return jobLikeRepository.countByJobId(jobId);
    }
}
