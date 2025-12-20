package com.jobsphere.jobsite.service.seeker;

import com.jobsphere.jobsite.constant.UserType;
import com.jobsphere.jobsite.dto.seeker.ProjectDto;
import com.jobsphere.jobsite.exception.AuthException;
import com.jobsphere.jobsite.exception.ResourceNotFoundException;
import com.jobsphere.jobsite.model.User;
import com.jobsphere.jobsite.model.seeker.SeekerProject;
import com.jobsphere.jobsite.repository.UserRepository;
import com.jobsphere.jobsite.repository.seeker.SeekerProjectRepository;
import com.jobsphere.jobsite.service.shared.CloudinaryFileService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SeekerProjectService {
    private final SeekerProjectRepository seekerProjectRepository;
    private final UserRepository userRepository;
    private final CloudinaryFileService cloudinaryFileService;

    private User getAuthenticatedUser() {
        return userRepository.findByEmail(
                SecurityContextHolder.getContext().getAuthentication().getName())
                .orElseThrow(() -> new AuthException("User not found"));
    }

    private void validateSeekerUser(User user) {
        if (user.getUserType() != UserType.SEEKER) {
            throw new AuthException("Only seekers can perform this action");
        }
    }

    @Transactional
    public ProjectDto createProject(ProjectDto projectDto, MultipartFile imageFile, MultipartFile videoFile) throws IOException {
        User user = getAuthenticatedUser();
        validateSeekerUser(user);
        UUID seekerId = user.getId();

        SeekerProject project = SeekerProject.builder()
                .seekerId(seekerId)
                .title(projectDto.getTitle())
                .description(projectDto.getDescription())
                .projectUrl(projectDto.getProjectUrl())
                .build();

        // Handle optional image upload
        if (imageFile != null && !imageFile.isEmpty()) {
            String imageUrl = cloudinaryFileService.uploadImage(imageFile, "seekers/projects/images");
            project.setImageUrl(imageUrl);
        }

        // Handle optional video upload
        if (videoFile != null && !videoFile.isEmpty()) {
            String videoUrl = cloudinaryFileService.uploadVideo(videoFile, "seekers/projects/videos");
            project.setVideoUrl(videoUrl);
        }

        SeekerProject savedProject = seekerProjectRepository.save(project);
        return mapToDto(savedProject);
    }

    @Transactional
    public ProjectDto updateProject(UUID projectId, ProjectDto projectDto, MultipartFile imageFile, MultipartFile videoFile) throws IOException {
        User user = getAuthenticatedUser();
        validateSeekerUser(user);
        UUID seekerId = user.getId();

        SeekerProject project = seekerProjectRepository.findByIdAndSeekerId(projectId, seekerId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));

        project.setTitle(projectDto.getTitle());
        project.setDescription(projectDto.getDescription());
        project.setProjectUrl(projectDto.getProjectUrl());

        // Handle optional image upload/replacement
        if (imageFile != null && !imageFile.isEmpty()) {
            // Delete old image if exists
            if (project.getImageUrl() != null) {
                try {
                    cloudinaryFileService.deleteFile(project.getImageUrl());
                } catch (IOException ignored) {
                }
            }
            String imageUrl = cloudinaryFileService.uploadImage(imageFile, "seekers/projects/images");
            project.setImageUrl(imageUrl);
        }

        // Handle optional video upload/replacement
        if (videoFile != null && !videoFile.isEmpty()) {
            // Delete old video if exists
            if (project.getVideoUrl() != null) {
                try {
                    cloudinaryFileService.deleteFile(project.getVideoUrl());
                } catch (IOException ignored) {
                }
            }
            String videoUrl = cloudinaryFileService.uploadVideo(videoFile, "seekers/projects/videos");
            project.setVideoUrl(videoUrl);
        }

        SeekerProject savedProject = seekerProjectRepository.save(project);
        return mapToDto(savedProject);
    }

    @Transactional
    public void deleteProject(UUID projectId) {
        User user = getAuthenticatedUser();
        validateSeekerUser(user);
        UUID seekerId = user.getId();

        SeekerProject project = seekerProjectRepository.findByIdAndSeekerId(projectId, seekerId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));

        // Delete media files from Cloudinary
        if (project.getImageUrl() != null) {
            try {
                cloudinaryFileService.deleteFile(project.getImageUrl());
            } catch (IOException ignored) {
            }
        }
        if (project.getVideoUrl() != null) {
            try {
                cloudinaryFileService.deleteFile(project.getVideoUrl());
            } catch (IOException ignored) {
            }
        }

        seekerProjectRepository.delete(project);
    }

    @Transactional(readOnly = true)
    public List<ProjectDto> getAllProjects() {
        User user = getAuthenticatedUser();
        validateSeekerUser(user);
        UUID seekerId = user.getId();

        List<SeekerProject> projects = seekerProjectRepository.findBySeekerId(seekerId);
        return projects.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ProjectDto> getProjectsByTitle(String title) {
        User user = getAuthenticatedUser();
        validateSeekerUser(user);
        UUID seekerId = user.getId();

        List<SeekerProject> projects = seekerProjectRepository.findBySeekerIdAndTitleContainingIgnoreCase(seekerId, title);
        return projects.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    private ProjectDto mapToDto(SeekerProject project) {
        return ProjectDto.builder()
                .id(project.getId())
                .title(project.getTitle())
                .description(project.getDescription())
                .projectUrl(project.getProjectUrl())
                .imageUrl(project.getImageUrl())
                .videoUrl(project.getVideoUrl())
                .build();
    }
}

