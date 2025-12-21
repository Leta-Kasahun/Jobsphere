package com.jobsphere.jobsite.controller.seeker.details;

import com.jobsphere.jobsite.dto.seeker.BioDto;
import com.jobsphere.jobsite.service.seeker.SeekerBioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/seekers/profile/details")
@RequiredArgsConstructor
public class BioController {
    private final SeekerBioService seekerBioService;

    @GetMapping("/bio")
    public ResponseEntity<BioDto> getBio() {
        return ResponseEntity.ok(seekerBioService.getBio());
    }

    @PostMapping("/bio")
    public ResponseEntity<BioDto> createBio(@Valid @RequestBody BioDto bioDto) {
        return ResponseEntity.ok(seekerBioService.createBio(bioDto));
    }

    @PutMapping("/bio")
    public ResponseEntity<BioDto> updateBio(@Valid @RequestBody BioDto bioDto) {
        return ResponseEntity.ok(seekerBioService.updateBio(bioDto));
    }

    @DeleteMapping("/bio")
    public ResponseEntity<Void> deleteBio() {
        seekerBioService.deleteBio();
        return ResponseEntity.noContent().build();
    }
}

