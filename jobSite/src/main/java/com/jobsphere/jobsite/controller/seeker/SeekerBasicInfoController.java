package com.jobsphere.jobsite.controller.seeker;

import com.jobsphere.jobsite.dto.seeker.SeekerBasicInfoDto;
import com.jobsphere.jobsite.service.seeker.SeekerBasicInfoService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/seekers/basic-info")
@RequiredArgsConstructor
public class SeekerBasicInfoController {
    private final SeekerBasicInfoService service;

    private UUID getCurrentUserId() {
        return UUID.fromString("00000000-0000-0000-0000-000000000000");
    }

    @GetMapping
    public SeekerBasicInfoDto get() {
        return service.getBasicInfo(getCurrentUserId());
    }

    @PostMapping
    public SeekerBasicInfoDto create(@RequestBody SeekerBasicInfoDto dto) {
        return service.create(getCurrentUserId(), dto);
    }

    @PutMapping
    public SeekerBasicInfoDto update(@RequestBody SeekerBasicInfoDto dto) {
        return service.update(getCurrentUserId(), dto);
    }
}