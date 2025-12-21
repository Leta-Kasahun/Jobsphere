package com.jobsphere.jobsite.controller.seeker.details.projects;

import com.jobsphere.jobsite.dto.seeker.ProjectDto;
import com.jobsphere.jobsite.service.seeker.SeekerProjectService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/seekers/profile/details/projects")
@RequiredArgsConstructor
public class ProjectController {
    private final SeekerProjectService seekerProjectService;

    @GetMapping
    public ResponseEntity<List<ProjectDto>> getAllProjects() {
        return ResponseEntity.ok(seekerProjectService.getAllProjects());
    }

    @GetMapping("/search")
    public ResponseEntity<List<ProjectDto>> searchProjectsByTitle(@RequestParam("title") String title) {
        return ResponseEntity.ok(seekerProjectService.getProjectsByTitle(title));
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ProjectDto> createProject(
            @Valid @RequestPart("project") ProjectDto projectDto,
            @RequestPart(value = "images", required = false) List<MultipartFile> imageFiles,
            @RequestPart(value = "video", required = false) MultipartFile videoFile) throws IOException {
        return ResponseEntity.ok(seekerProjectService.createProject(projectDto, imageFiles, videoFile));
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ProjectDto> updateProject(
            @PathVariable UUID id,
            @Valid @RequestPart("project") ProjectDto projectDto,
            @RequestPart(value = "images", required = false) List<MultipartFile> imageFiles,
            @RequestPart(value = "video", required = false) MultipartFile videoFile) throws IOException {
        return ResponseEntity.ok(seekerProjectService.updateProject(id, projectDto, imageFiles, videoFile));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProject(@PathVariable UUID id) {
        seekerProjectService.deleteProject(id);
        return ResponseEntity.noContent().build();
    }
}
