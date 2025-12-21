package com.jobsphere.jobsite.controller.shared;

import com.jobsphere.jobsite.dto.shared.AddressCreateRequest;
import com.jobsphere.jobsite.dto.shared.AddressDto;
import com.jobsphere.jobsite.service.shared.AddressService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/addresses")
@RequiredArgsConstructor
public class AddressController {
    private final AddressService service;

    @PostMapping
    public ResponseEntity<AddressDto> createAddress(@Valid @RequestBody AddressCreateRequest request) {
        AddressDto response = service.createAddress(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{addressId}")
    public ResponseEntity<AddressDto> getAddress(@PathVariable UUID addressId) {
        AddressDto response = service.getAddress(addressId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/legacy")
    public AddressDto create(@RequestBody AddressDto dto) {
        return service.create(dto);
    }
}