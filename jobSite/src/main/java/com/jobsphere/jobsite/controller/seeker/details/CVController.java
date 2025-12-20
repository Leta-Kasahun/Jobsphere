package com.jobsphere.jobsite.controller.seeker.details;

import com.jobsphere.jobsite.dto.seeker.CVDto;
import com.jobsphere.jobsite.service.seeker.SeekerCVService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/seekers/profile/details/cv")
@RequiredArgsConstructor
public class CVController {
    private final SeekerCVService seekerCVService;

    @GetMapping
    public ResponseEntity<CVDto> getCV() {
        return ResponseEntity.ok(seekerCVService.getCV());
    }

    @PostMapping
    public ResponseEntity<CVDto> createOrUpdateCV(@Valid @RequestBody CVDto cvDto) {
        return ResponseEntity.ok(seekerCVService.createOrUpdateCV(cvDto));
    }

    @PutMapping
    public ResponseEntity<CVDto> updateCV(@Valid @RequestBody CVDto cvDto) {
        return ResponseEntity.ok(seekerCVService.updateCV(cvDto));
    }
}

