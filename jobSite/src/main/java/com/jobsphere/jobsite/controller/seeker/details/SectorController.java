package com.jobsphere.jobsite.controller.seeker.details;

import com.jobsphere.jobsite.dto.seeker.SectorDto;
import com.jobsphere.jobsite.service.seeker.SeekerSectorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/seekers/profile/details")
@RequiredArgsConstructor
public class SectorController {
    private final SeekerSectorService seekerSectorService;

    @GetMapping("/sector")
    public ResponseEntity<SectorDto> getSector() {
        return ResponseEntity.ok(seekerSectorService.getSector());
    }

    @PostMapping("/sector")
    public ResponseEntity<SectorDto> createSector(@Valid @RequestBody SectorDto sectorDto) {
        return ResponseEntity.ok(seekerSectorService.createSector(sectorDto));
    }

    @PutMapping("/sector")
    public ResponseEntity<SectorDto> updateSector(@Valid @RequestBody SectorDto sectorDto) {
        return ResponseEntity.ok(seekerSectorService.updateSector(sectorDto));
    }
}

