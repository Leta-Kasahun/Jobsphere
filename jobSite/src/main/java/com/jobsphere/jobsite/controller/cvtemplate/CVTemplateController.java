package com.jobsphere.jobsite.controller.cvtemplate;

import com.jobsphere.jobsite.dto.cvtemplate.CVTemplateCreateRequest;
import com.jobsphere.jobsite.dto.cvtemplate.CVTemplateResponse;
import com.jobsphere.jobsite.service.cvtemplate.CVTemplateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/cv-templates")
@RequiredArgsConstructor
@Tag(name = "CV Templates", description = "Admin API for managing CV templates")
public class CVTemplateController {
    private final CVTemplateService cvTemplateService;

    @GetMapping
    @Operation(summary = "Get all CV templates (Admin only)")
    public ResponseEntity<List<CVTemplateResponse>> getAllTemplates() {
        List<CVTemplateResponse> templates = cvTemplateService.getAllTemplates(Pageable.unpaged())
                .getContent();
        return ResponseEntity.ok(templates);
    }

    @PostMapping
    @Operation(summary = "Create a new CV template (Admin only)")
    public ResponseEntity<CVTemplateResponse> createTemplate(@Valid @RequestBody CVTemplateCreateRequest request) {
        CVTemplateResponse response = cvTemplateService.createTemplate(request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{templateId}")
    @Operation(summary = "Delete a CV template (Admin only)")
    public ResponseEntity<Void> deleteTemplate(@PathVariable UUID templateId) {
        cvTemplateService.deleteTemplate(templateId);
        return ResponseEntity.ok().build();
    }
}
