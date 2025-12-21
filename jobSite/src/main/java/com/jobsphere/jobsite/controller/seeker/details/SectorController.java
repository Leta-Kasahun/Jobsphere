package com.jobsphere.jobsite.controller.seeker.details;

import com.jobsphere.jobsite.dto.seeker.SectorDto;
import com.jobsphere.jobsite.service.seeker.SeekerSectorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/seekers/profile/details/sector")
@RequiredArgsConstructor
public class SectorController {
    private final SeekerSectorService seekerSectorService;

    @GetMapping
    public ResponseEntity<List<SectorDto>> getSectors() {
        return ResponseEntity.ok(seekerSectorService.getSectors());
    }

    @PostMapping
    public ResponseEntity<SectorDto> addSector(@Valid @RequestBody SectorDto sectorDto) {
        return ResponseEntity.ok(seekerSectorService.addSector(sectorDto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSector(@PathVariable UUID id) {
        seekerSectorService.deleteSector(id);
        return ResponseEntity.noContent().build();
    }
}
