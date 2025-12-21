package com.jobsphere.jobsite.service.seeker;

import com.jobsphere.jobsite.constant.UserType;
import com.jobsphere.jobsite.dto.seeker.ProjectDto;
import com.jobsphere.jobsite.exception.AuthException;
import com.jobsphere.jobsite.exception.ResourceNotFoundException;
import com.jobsphere.jobsite.model.User;
import com.jobsphere.jobsite.model.seeker.SeekerProject;
import com.jobsphere.jobsite.repository.UserRepository;
import com.jobsphere.jobsite.repository.seeker.SeekerProjectRepository;
import com.jobsphere.jobsite.repository.seeker.SeekerProjectImageRepository;
import com.jobsphere.jobsite.model.seeker.SeekerProjectImage;
import com.jobsphere.jobsite.service.shared.CloudinaryFileService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SeekerProjectService {
    private final SeekerProjectRepository seekerProjectRepository;
    private final SeekerProjectImageRepository seekerProjectImageRepository;
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
    public ProjectDto createProject(ProjectDto projectDto, List<MultipartFile> imageFiles, MultipartFile videoFile)
            throws IOException {
        User user = getAuthenticatedUser();
        validateSeekerUser(user);
        UUID seekerId = user.getId();

        log.info("Creating project: title={}, hasImages={}, hasVideo={}",
                projectDto.getTitle(),
                imageFiles != null ? imageFiles.size() : 0,
                videoFile != null && !videoFile.isEmpty());

        SeekerProject project = SeekerProject.builder()
                .seekerId(seekerId)
                .title(projectDto.getTitle())
                .description(projectDto.getDescription())
                .projectUrl(projectDto.getProjectUrl())
                .videoUrl(projectDto.getVideoUrl())
                .videoType(projectDto.getVideoType() != null ? projectDto.getVideoType() : "UPLOAD")
                .build();

        // Handle video upload if type is UPLOAD and file is provided
        if ("UPLOAD".equals(project.getVideoType()) && videoFile != null && !videoFile.isEmpty()) {
            log.info("Uploading video file: {} ({} bytes)", videoFile.getOriginalFilename(), videoFile.getSize());
            String videoUrl = cloudinaryFileService.uploadVideo(videoFile, "seekers/projects/videos");
            log.info("Video uploaded successfully: {}", videoUrl);
            project.setVideoUrl(videoUrl);
        }

        SeekerProject savedProject = seekerProjectRepository.save(project);

        // Handle multiple image uploads
        if (imageFiles != null && !imageFiles.isEmpty()) {
            log.info("Processing {} images...", imageFiles.size());
            List<SeekerProjectImage> projectImages = new ArrayList<>();
            for (MultipartFile file : imageFiles) {
                if (!file.isEmpty()) {
                    String imageUrl = cloudinaryFileService.uploadImage(file, "seekers/projects/images");
                    log.info("Image uploaded: {}", imageUrl);
                    projectImages.add(SeekerProjectImage.builder()
                            .projectId(savedProject.getId())
                            .imageUrl(imageUrl)
                            .build());
                }
            }
            if (!projectImages.isEmpty()) {
                seekerProjectImageRepository.saveAll(projectImages);
                savedProject.setImages(projectImages);
                log.info("Saved {} images to database", projectImages.size());
            }
        }

        return mapToDto(savedProject);
    }

    @Transactional
    public ProjectDto updateProject(UUID projectId, ProjectDto projectDto, List<MultipartFile> imageFiles,
            MultipartFile videoFile) throws IOException {
        User user = getAuthenticatedUser();
        validateSeekerUser(user);
        UUID seekerId = user.getId();

        SeekerProject project = seekerProjectRepository.findByIdAndSeekerId(projectId, seekerId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));

        project.setTitle(projectDto.getTitle());
        project.setDescription(projectDto.getDescription());
        project.setProjectUrl(projectDto.getProjectUrl());
        project.setVideoType(projectDto.getVideoType() != null ? projectDto.getVideoType() : project.getVideoType());

        // Handle video
        if ("YOUTUBE".equals(project.getVideoType())) {
            project.setVideoUrl(projectDto.getVideoUrl());
        } else if (videoFile != null && !videoFile.isEmpty()) {
            if (project.getVideoUrl() != null && !project.getVideoUrl().contains("youtube.com")) {
                try {
                    cloudinaryFileService.deleteFile(project.getVideoUrl());
                } catch (IOException ignored) {
                }
            }
            String videoUrl = cloudinaryFileService.uploadVideo(videoFile, "seekers/projects/videos");
            project.setVideoUrl(videoUrl);
        }

        // Handle new image uploads (append to existing ones)
        if (imageFiles != null && !imageFiles.isEmpty()) {
            List<SeekerProjectImage> newImages = new ArrayList<>();
            for (MultipartFile file : imageFiles) {
                if (!file.isEmpty()) {
                    String imageUrl = cloudinaryFileService.uploadImage(file, "seekers/projects/images");
                    newImages.add(SeekerProjectImage.builder()
                            .projectId(project.getId())
                            .imageUrl(imageUrl)
                            .build());
                }
            }
            if (!newImages.isEmpty()) {
                seekerProjectImageRepository.saveAll(newImages);
                if (project.getImages() == null)
                    project.setImages(new ArrayList<>());
                project.getImages().addAll(newImages);
            }
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

        List<SeekerProject> projects = seekerProjectRepository.findBySeekerIdAndTitleContainingIgnoreCase(seekerId,
                title);
        return projects.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    private ProjectDto mapToDto(SeekerProject project) {
        List<String> imageUrls = project.getImages() != null
                ? project.getImages().stream().map(SeekerProjectImage::getImageUrl).collect(Collectors.toList())
                : new ArrayList<>();

        return ProjectDto.builder()
                .id(project.getId())
                .title(project.getTitle())
                .description(project.getDescription())
                .projectUrl(project.getProjectUrl())
                .imageUrl(project.getImageUrl())
                .imageUrls(imageUrls)
                .videoUrl(project.getVideoUrl())
                .videoType(project.getVideoType())
                .build();
    }
}
