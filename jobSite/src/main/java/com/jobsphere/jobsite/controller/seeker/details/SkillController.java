package com.jobsphere.jobsite.controller.seeker.details;

import com.jobsphere.jobsite.dto.seeker.SkillDto;
import com.jobsphere.jobsite.service.seeker.SeekerSkillService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/seekers/profile/details/skills")
@RequiredArgsConstructor
public class SkillController {
    private final SeekerSkillService seekerSkillService;

    @GetMapping
    public ResponseEntity<List<SkillDto>> getSkills() {
        return ResponseEntity.ok(seekerSkillService.getSkills());
    }

    @PostMapping
    public ResponseEntity<SkillDto> addSkill(@Valid @RequestBody SkillDto skillDto) {
        return ResponseEntity.ok(seekerSkillService.addSkill(skillDto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<SkillDto> updateSkill(
            @PathVariable UUID id,
            @Valid @RequestBody SkillDto skillDto) {
        return ResponseEntity.ok(seekerSkillService.updateSkill(id, skillDto));
    }
}

