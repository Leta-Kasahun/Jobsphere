package com.jobsphere.jobsite.controller.seeker;

import com.jobsphere.jobsite.dto.seeker.BasicInfoRequest;
import com.jobsphere.jobsite.dto.seeker.BasicInfoResponse;
import com.jobsphere.jobsite.dto.shared.AddressDto;
import com.jobsphere.jobsite.service.seeker.SeekerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/v1/seekers/profile")
@RequiredArgsConstructor
public class SeekerProfileController {
    private final SeekerService seekerService;

    @PostMapping("/basic-info")
    public ResponseEntity<BasicInfoResponse> create(@Valid @RequestBody BasicInfoRequest request) {
        return ResponseEntity.ok(seekerService.create(request));
    }

    @PutMapping("/basic-info")
    public ResponseEntity<BasicInfoResponse> update(@Valid @RequestBody BasicInfoRequest request) {
        return ResponseEntity.ok(seekerService.update(request));
    }

    @GetMapping("/basic-info")
    public ResponseEntity<BasicInfoResponse> get() {
        return ResponseEntity.ok(seekerService.getBasicInfo());
    }

    @DeleteMapping("/basic-info")
    public ResponseEntity<Void> delete() {
        seekerService.delete();
        return ResponseEntity.noContent().build();
    }

    @PostMapping(value = "/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<BasicInfoResponse> uploadImage(@RequestParam("file") MultipartFile file) throws IOException {
        return ResponseEntity.ok(seekerService.uploadProfileImage(file));
    }

    @DeleteMapping("/image")
    public ResponseEntity<BasicInfoResponse> deleteImage() {
        return ResponseEntity.ok(seekerService.deleteProfileImage());
    }

    @PostMapping("/address")
    public ResponseEntity<BasicInfoResponse> setAddress(@Valid @RequestBody AddressDto addressDto) {
        return ResponseEntity.ok(seekerService.setAddress(addressDto));
    }

    @DeleteMapping("/address")
    public ResponseEntity<BasicInfoResponse> deleteAddress() {
        return ResponseEntity.ok(seekerService.deleteAddress());
    }
}