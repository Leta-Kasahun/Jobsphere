package com.jobsphere.jobsite.service.cvtemplate;

import com.jobsphere.jobsite.dto.cvtemplate.CVTemplateCreateRequest;
import com.jobsphere.jobsite.dto.cvtemplate.CVTemplateResponse;
import com.jobsphere.jobsite.dto.cvtemplate.CVTemplateUpdateRequest;
import com.jobsphere.jobsite.exception.ResourceNotFoundException;
import com.jobsphere.jobsite.model.cvtemplate.CVTemplate;
import com.jobsphere.jobsite.repository.cvtemplate.CVTemplateRepository;
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
public class CVTemplateService {
    private final CVTemplateRepository cvTemplateRepository;

    @Transactional
    public CVTemplateResponse createTemplate(CVTemplateCreateRequest request) {
        CVTemplate template = CVTemplate.builder()
            .name(request.name())
            .category(request.category())
            .description(request.description())
            .sections(request.sections())
            .status("ACTIVE")
            .build();

        template = cvTemplateRepository.save(template);
        log.info("CV template created: {} - {}", template.getId(), template.getName());

        return mapToResponse(template);
    }

    public Page<CVTemplateResponse> getAllTemplates(Pageable pageable) {
        return cvTemplateRepository.findAll(pageable)
            .map(this::mapToResponse);
    }

    public Page<CVTemplateResponse> getTemplatesByStatus(String status, Pageable pageable) {
        return cvTemplateRepository.findByStatus(status, pageable)
            .map(this::mapToResponse);
    }

    public Page<CVTemplateResponse> getTemplatesByCategory(String category, Pageable pageable) {
        return cvTemplateRepository.findByCategory(category, pageable)
            .map(this::mapToResponse);
    }

    public Page<CVTemplateResponse> getActiveTemplatesByCategory(String category, Pageable pageable) {
        return cvTemplateRepository.findByCategoryAndStatus(category, "ACTIVE", pageable)
            .map(this::mapToResponse);
    }

    public CVTemplateResponse getTemplate(UUID templateId) {
        CVTemplate template = cvTemplateRepository.findById(templateId)
            .orElseThrow(() -> new ResourceNotFoundException("CV template not found"));
        return mapToResponse(template);
    }

    @Transactional
    public CVTemplateResponse updateTemplate(UUID templateId, CVTemplateUpdateRequest request) {
        CVTemplate template = cvTemplateRepository.findById(templateId)
            .orElseThrow(() -> new ResourceNotFoundException("CV template not found"));

        if (StringUtils.hasText(request.name())) {
            template.setName(request.name());
        }
        if (StringUtils.hasText(request.category())) {
            template.setCategory(request.category());
        }
        if (StringUtils.hasText(request.status())) {
            template.setStatus(request.status());
        }
        if (request.description() != null) {
            template.setDescription(request.description());
        }
        if (request.sections() != null) {
            template.setSections(request.sections());
        }

        template = cvTemplateRepository.save(template);
        log.info("CV template updated: {}", templateId);

        return mapToResponse(template);
    }

    @Transactional
    public void deleteTemplate(UUID templateId) {
        if (!cvTemplateRepository.existsById(templateId)) {
            throw new ResourceNotFoundException("CV template not found");
        }
        cvTemplateRepository.deleteById(templateId);
        log.info("CV template deleted: {}", templateId);
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
