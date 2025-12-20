package com.jobsphere.jobsite.controller.seeker.details;

import com.jobsphere.jobsite.dto.seeker.TagDto;
import com.jobsphere.jobsite.service.seeker.SeekerTagService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/seekers/profile/details")
@RequiredArgsConstructor
public class TagController {
    private final SeekerTagService seekerTagService;

    @GetMapping("/tags")
    public ResponseEntity<List<TagDto>> getTags() {
        return ResponseEntity.ok(seekerTagService.getTags());
    }

    @PostMapping("/tags")
    public ResponseEntity<TagDto> addTag(@Valid @RequestBody TagDto tagDto) {
        return ResponseEntity.ok(seekerTagService.addTag(tagDto));
    }

    @DeleteMapping("/tags")
    public ResponseEntity<Void> deleteTag(@Valid @RequestBody TagDto tagDto) {
        seekerTagService.deleteTag(tagDto);
        return ResponseEntity.noContent().build();
    }
}

