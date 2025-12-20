package com.jobsphere.jobsite.controller.seeker.details;

import com.jobsphere.jobsite.dto.seeker.SocialLinkDto;
import com.jobsphere.jobsite.service.seeker.SeekerSocialService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/seekers/profile/details")
@RequiredArgsConstructor
public class SocialController {
    private final SeekerSocialService seekerSocialService;

    @GetMapping("/social-links")
    public ResponseEntity<List<SocialLinkDto>> getSocialLinks() {
        return ResponseEntity.ok(seekerSocialService.getSocialLinks());
    }

    @PostMapping("/social-links")
    public ResponseEntity<SocialLinkDto> addSocialLink(@Valid @RequestBody SocialLinkDto socialLinkDto) {
        return ResponseEntity.ok(seekerSocialService.addSocialLink(socialLinkDto));
    }

    @DeleteMapping("/social-links")
    public ResponseEntity<Void> deleteSocialLink(@Valid @RequestBody SocialLinkDto socialLinkDto) {
        seekerSocialService.deleteSocialLink(socialLinkDto);
        return ResponseEntity.noContent().build();
    }
}

