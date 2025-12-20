package com.jobsphere.jobsite.controller.seeker.details;

import com.jobsphere.jobsite.dto.seeker.MediaDto;
import com.jobsphere.jobsite.service.seeker.SeekerDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/v1/seekers/profile/details")
@RequiredArgsConstructor
public class MediaController {
    private final SeekerDetailsService seekerDetailsService;

    @GetMapping("/media")
    public ResponseEntity<MediaDto> getMedia() {
        return ResponseEntity.ok(seekerDetailsService.getMedia());
    }

    @PostMapping(value = "/media/profile-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<MediaDto> uploadProfileImage(@RequestParam("file") MultipartFile file) throws IOException {
        return ResponseEntity.ok(seekerDetailsService.uploadProfileImage(file));
    }

    @DeleteMapping("/media/profile-image")
    public ResponseEntity<MediaDto> deleteProfileImage() {
        return ResponseEntity.ok(seekerDetailsService.deleteProfileImage());
    }

    @PostMapping(value = "/media/cv", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<MediaDto> uploadCv(@RequestParam("file") MultipartFile file) throws IOException {
        return ResponseEntity.ok(seekerDetailsService.uploadCv(file));
    }

    @DeleteMapping("/media/cv")
    public ResponseEntity<MediaDto> deleteCv() {
        return ResponseEntity.ok(seekerDetailsService.deleteCv());
    }
}

