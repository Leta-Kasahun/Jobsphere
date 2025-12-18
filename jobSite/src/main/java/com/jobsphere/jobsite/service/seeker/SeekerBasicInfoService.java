package com.jobsphere.jobsite.service.seeker;

import com.jobsphere.jobsite.dto.seeker.SeekerBasicInfoDto;
import com.jobsphere.jobsite.model.seeker.Seeker;
import com.jobsphere.jobsite.model.shared.Address;
import com.jobsphere.jobsite.model.User;
import com.jobsphere.jobsite.repository.seeker.SeekerRepository;
import com.jobsphere.jobsite.repository.UserRepository;
import com.jobsphere.jobsite.repository.shared.AddressRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;
import java.time.Instant;

@Service
@RequiredArgsConstructor
public class SeekerBasicInfoService {
    private final SeekerRepository seekerRepository;
    private final UserRepository userRepository;
    private final AddressRepository addressRepository;

    public SeekerBasicInfoDto getBasicInfo(UUID userId) {
        Seeker s = seekerRepository.findByUserId(userId).orElseThrow();
        return SeekerBasicInfoDto.builder()
            .firstName(s.getFirstName())
            .middleName(s.getMiddleName())
            .lastName(s.getLastName())
            .phone(s.getPhone())
            .gender(s.getGender())
            .dateOfBirth(s.getDateOfBirth())
            .addressId(s.getAddress() == null ? null : s.getAddress().getId())
            .build();
    }

    @Transactional
    public SeekerBasicInfoDto create(UUID userId, SeekerBasicInfoDto dto) {
        User user = userRepository.findById(userId).orElseThrow();
        Address address = dto.getAddressId() == null ? null : addressRepository.findById(dto.getAddressId()).orElse(null);

        Seeker s = Seeker.builder()
            .id(userId)
            .user(user)
            .firstName(dto.getFirstName())
            .middleName(dto.getMiddleName())
            .lastName(dto.getLastName())
            .phone(dto.getPhone())
            .gender(dto.getGender())
            .dateOfBirth(dto.getDateOfBirth())
            .address(address)
            .createdAt(Instant.now())
            .build();

        seekerRepository.save(s);
        return getBasicInfo(userId);
    }

    @Transactional
    public SeekerBasicInfoDto update(UUID userId, SeekerBasicInfoDto dto) {
        Seeker s = seekerRepository.findByUserId(userId).orElseThrow();
        Address address = dto.getAddressId() == null ? null : addressRepository.findById(dto.getAddressId()).orElse(null);
        s.setFirstName(dto.getFirstName());
        s.setMiddleName(dto.getMiddleName());
        s.setLastName(dto.getLastName());
        s.setPhone(dto.getPhone());
        s.setGender(dto.getGender());
        s.setDateOfBirth(dto.getDateOfBirth());
        s.setAddress(address);
        s.setUpdatedAt(Instant.now());
        seekerRepository.save(s);
        return getBasicInfo(userId);
    }
}