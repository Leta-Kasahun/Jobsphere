package com.jobsphere.jobsite.service.cvtemplate;

import com.jobsphere.jobsite.dto.cvtemplate.CVBuilderRequest;
import com.jobsphere.jobsite.dto.cvtemplate.CVTemplateResponse;
import com.jobsphere.jobsite.exception.ResourceNotFoundException;
import com.jobsphere.jobsite.model.User;
import com.jobsphere.jobsite.model.cvtemplate.CVTemplate;
import com.jobsphere.jobsite.model.seeker.*;
import com.jobsphere.jobsite.repository.UserRepository;
import com.jobsphere.jobsite.repository.cvtemplate.CVTemplateRepository;
import com.jobsphere.jobsite.repository.seeker.*;
import com.jobsphere.jobsite.service.shared.AuthenticationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CVBuilderService {
    private final CVTemplateRepository cvTemplateRepository;
    private final SeekerRepository seekerRepository;
    private final SeekerSkillRepository seekerSkillRepository;
    private final SeekerSectorRepository seekerSectorRepository;
    private final SeekerSocialLinkRepository seekerSocialLinkRepository;
    private final SeekerTagRepository seekerTagRepository;
    private final SeekerProjectRepository seekerProjectRepository;
    private final UserRepository userRepository;
    private final AuthenticationService authenticationService;

    public List<CVTemplateResponse> getActiveTemplates() {
        List<CVTemplate> templates = cvTemplateRepository.findByStatus("ACTIVE");
        return templates.stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }

    @Transactional
    public Map<String, Object> getTemplateWithAutoFill(UUID templateId) {
        UUID seekerId = authenticationService.getCurrentUserId();
        
        CVTemplate template = cvTemplateRepository.findById(templateId)
            .orElseThrow(() -> new ResourceNotFoundException("CV template not found"));

        if (!"ACTIVE".equals(template.getStatus())) {
            throw new IllegalStateException("Template is not active");
        }

        Map<String, Object> filledData = autoFillFromProfile(seekerId, template.getSections());
        
        Map<String, Object> result = new HashMap<>();
        result.put("template", mapToResponse(template));
        result.put("filledData", filledData);
        
        return result;
    }

    public Map<String, Object> previewCV(CVBuilderRequest request) {
        CVTemplate template = cvTemplateRepository.findById(request.templateId())
            .orElseThrow(() -> new ResourceNotFoundException("CV template not found"));

        Map<String, Object> preview = new HashMap<>();
        preview.put("template", mapToResponse(template));
        preview.put("data", request.filledData());
        
        return preview;
    }

    public Map<String, Object> prepareDownload(CVBuilderRequest request) {
        CVTemplate template = cvTemplateRepository.findById(request.templateId())
            .orElseThrow(() -> new ResourceNotFoundException("CV template not found"));

        Map<String, Object> download = new HashMap<>();
        download.put("template", mapToResponse(template));
        download.put("data", request.filledData());
        
        return download;
    }

    private Map<String, Object> autoFillFromProfile(UUID seekerId, Map<String, Object> templateSections) {
        Map<String, Object> filledData = new HashMap<>();
        
        Seeker seeker = seekerRepository.findById(seekerId)
            .orElseThrow(() -> new ResourceNotFoundException("Seeker profile not found"));
        
        User user = userRepository.findById(seekerId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (templateSections.containsKey("header")) {
            Map<String, Object> header = new HashMap<>();
            String fullName = buildFullName(seeker);
            header.put("title", fullName);
            if (seeker.getCvUrl() != null) {
                header.put("cv_url", seeker.getCvUrl());
            }
            filledData.put("header", header);
        }

        if (templateSections.containsKey("personal_information")) {
            Map<String, Object> personalInfo = new HashMap<>();
            personalInfo.put("full_name", buildFullName(seeker));
            personalInfo.put("email", user.getEmail());
            personalInfo.put("phone", seeker.getPhone());
            filledData.put("personal_information", personalInfo);
        }

        if (templateSections.containsKey("skills")) {
            List<SeekerSkill> skills = seekerSkillRepository.findBySeekerId(seekerId);
            Map<String, Object> skillsData = new HashMap<>();
            skillsData.put("technical_skills", skills.stream()
                .map(SeekerSkill::getSkill)
                .collect(Collectors.toList()));
            filledData.put("skills", skillsData);
        }

        if (templateSections.containsKey("projects")) {
            List<SeekerProject> projects = seekerProjectRepository.findBySeekerId(seekerId);
            List<Map<String, Object>> projectsData = projects.stream()
                .map(p -> {
                    Map<String, Object> project = new HashMap<>();
                    project.put("project_name", p.getTitle());
                    project.put("description", p.getDescription());
                    if (p.getProjectUrl() != null) {
                        project.put("project_url", p.getProjectUrl());
                    }
                    return project;
                })
                .collect(Collectors.toList());
            filledData.put("projects", projectsData);
        }

        if (templateSections.containsKey("certifications")) {
            filledData.put("certifications", new ArrayList<>());
        }

        if (templateSections.containsKey("education")) {
            filledData.put("education", new ArrayList<>());
        }

        if (templateSections.containsKey("experience")) {
            filledData.put("experience", new ArrayList<>());
        }

        if (templateSections.containsKey("languages")) {
            filledData.put("languages", new ArrayList<>());
        }

        List<SeekerSector> sectors = seekerSectorRepository.findBySeekerId(seekerId);
        if (!sectors.isEmpty() && templateSections.containsKey("sectors")) {
            filledData.put("sectors", sectors.stream()
                .map(SeekerSector::getSector)
                .collect(Collectors.toList()));
        }

        List<SeekerTag> tags = seekerTagRepository.findBySeekerId(seekerId);
        if (!tags.isEmpty() && templateSections.containsKey("tags")) {
            filledData.put("tags", tags.stream()
                .map(SeekerTag::getTag)
                .collect(Collectors.toList()));
        }

        List<SeekerSocialLink> socialLinks = seekerSocialLinkRepository.findBySeekerId(seekerId);
        if (!socialLinks.isEmpty() && templateSections.containsKey("social_links")) {
            List<Map<String, Object>> linksData = socialLinks.stream()
                .map(link -> {
                    Map<String, Object> linkMap = new HashMap<>();
                    linkMap.put("platform", link.getPlatform());
                    linkMap.put("url", link.getUrl());
                    return linkMap;
                })
                .collect(Collectors.toList());
            filledData.put("social_links", linksData);
        }

        return filledData;
    }

    private String buildFullName(Seeker seeker) {
        StringBuilder name = new StringBuilder();
        if (seeker.getFirstName() != null) {
            name.append(seeker.getFirstName());
        }
        if (seeker.getMiddleName() != null) {
            if (name.length() > 0) name.append(" ");
            name.append(seeker.getMiddleName());
        }
        if (seeker.getLastName() != null) {
            if (name.length() > 0) name.append(" ");
            name.append(seeker.getLastName());
        }
        return name.toString().trim();
    }

    private CVTemplateResponse mapToResponse(CVTemplate template) {
        return new CVTemplateResponse(
            template.getId(),
            template.getName(),
            template.getCategory(),
            template.getStatus(),
            template.getDescription(),
            template.getSections(),
            template.getCreatedAt(),
            template.getUpdatedAt()
        );
    }
}
