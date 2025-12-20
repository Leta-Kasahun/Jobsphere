package com.jobsphere.jobsite.service.seeker;

import com.jobsphere.jobsite.constant.UserType;
import com.jobsphere.jobsite.dto.seeker.BasicInfoRequest;
import com.jobsphere.jobsite.dto.seeker.BasicInfoResponse;
import com.jobsphere.jobsite.exception.AuthException;
import com.jobsphere.jobsite.exception.ResourceNotFoundException;
import com.jobsphere.jobsite.model.seeker.Seeker;
import com.jobsphere.jobsite.model.User;
import com.jobsphere.jobsite.dto.shared.AddressDto;
import com.jobsphere.jobsite.model.shared.Address;
import com.jobsphere.jobsite.repository.seeker.SeekerRepository;
import com.jobsphere.jobsite.repository.UserRepository;
import com.jobsphere.jobsite.repository.shared.AddressRepository;
import com.jobsphere.jobsite.service.shared.CloudinaryImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SeekerService {
    private final SeekerRepository seekerRepository;
    private final UserRepository userRepository;
    private final AddressRepository addressRepository;
    private final CloudinaryImageService cloudinaryImageService;

    private User getAuthenticatedUser() {
        org.springframework.security.core.Authentication authentication = SecurityContextHolder.getContext()
                .getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AuthException("User not authenticated");
        }

        String email;
        Object principal = authentication.getPrincipal();
        if (principal instanceof org.springframework.security.core.userdetails.UserDetails) {
            email = ((org.springframework.security.core.userdetails.UserDetails) principal).getUsername();
        } else if (principal instanceof org.springframework.security.oauth2.core.user.OAuth2User) {
            email = ((org.springframework.security.oauth2.core.user.OAuth2User) principal).getAttribute("email");
        } else if (principal instanceof String) {
            email = (String) principal;
        } else {
            email = authentication.getName();
        }

        if (email == null || "anonymousUser".equals(email)) {
            throw new AuthException("User not found: Principal is anonymous");
        }

        return userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new AuthException("User not found: " + email));

    }

    private void validateSeekerUser(User user) {
        if (user.getUserType() != UserType.SEEKER) {
            throw new AuthException("Only seekers can perform this action");
        }
    }

    @Transactional
    public BasicInfoResponse create(BasicInfoRequest request) {
        User user = getAuthenticatedUser();
        validateSeekerUser(user);
        UUID userId = user.getId();
        if (seekerRepository.existsById(userId)) {
            throw new AuthException("Seeker profile already exists");
        }
        Seeker seeker = buildSeeker(userId, request);
        return mapToResponse(seekerRepository.save(seeker), user);
    }

    @Transactional
    public BasicInfoResponse update(BasicInfoRequest request) {
        User user = getAuthenticatedUser();
        validateSeekerUser(user);
        UUID userId = user.getId();
        Seeker seeker = seekerRepository.findById(userId).orElse(null);

        if (seeker == null) {
            seeker = buildSeeker(userId, request);
        } else {
            updateSeekerFields(seeker, request);
        }

        return mapToResponse(seekerRepository.save(seeker), user);
    }

    @Transactional
    public BasicInfoResponse saveOrUpdateBasicInfo(BasicInfoRequest request) {
        User user = getAuthenticatedUser();
        validateSeekerUser(user);
        UUID userId = user.getId();
        Seeker seeker = seekerRepository.findById(userId).orElse(null);
        if (seeker == null) {
            seeker = buildSeeker(userId, request);
            seekerRepository.save(seeker);
        } else {
            updateSeekerFields(seeker, request);
        }
        return mapToResponse(seeker, user);
    }

    @Transactional(readOnly = true)
    public BasicInfoResponse getBasicInfo() {
        User user = getAuthenticatedUser();
        Seeker seeker = seekerRepository.findById(user.getId()).orElse(null);

        if (seeker == null) {
            // Return a default response for new users who haven't set up their profile yet
            return BasicInfoResponse.builder()
                    .id(user.getId())
                    .email(user.getEmail())
                    .profileCompletion("0%")
                    .build();
        }

        return mapToResponse(seeker, user);
    }

    @Transactional
    public void delete() {
        User user = getAuthenticatedUser();
        validateSeekerUser(user);
        Seeker seeker = seekerRepository.findById(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Seeker profile not found"));
        if (seeker.getProfileImageUrl() != null) {
            try {
                cloudinaryImageService.deleteImage(seeker.getProfileImageUrl());
            } catch (IOException ignored) {
            }
        }
        seekerRepository.delete(seeker);
    }

    @Transactional
    public BasicInfoResponse uploadProfileImage(MultipartFile file) throws IOException {
        User user = getAuthenticatedUser();
        validateSeekerUser(user);
        UUID userId = user.getId();
        Seeker seeker = seekerRepository.findById(userId).orElse(null);
        if (seeker == null) {
            seeker = Seeker.builder().id(userId).build();
        }
        if (seeker.getProfileImageUrl() != null) {
            cloudinaryImageService.deleteImage(seeker.getProfileImageUrl());
        }
        String imageUrl = cloudinaryImageService.uploadImage(file, "seekers/profile");
        seeker.setProfileImageUrl(imageUrl);
        seekerRepository.save(seeker);
        return mapToResponse(seeker, user);
    }

    @Transactional
    public BasicInfoResponse deleteProfileImage() {
        User user = getAuthenticatedUser();
        validateSeekerUser(user);
        Seeker seeker = seekerRepository.findById(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Seeker profile not found"));
        if (seeker.getProfileImageUrl() != null) {
            try {
                cloudinaryImageService.deleteImage(seeker.getProfileImageUrl());
            } catch (IOException ignored) {
            }
            seeker.setProfileImageUrl(null);
            seekerRepository.save(seeker);
        }
        return mapToResponse(seeker, user);
    }

    @Transactional
    public BasicInfoResponse setAddress(AddressDto addressDto) {
        User user = getAuthenticatedUser();
        validateSeekerUser(user);
        UUID userId = user.getId();
        Seeker seeker = seekerRepository.findById(userId).orElse(null);

        if (seeker == null) {
            seeker = Seeker.builder().id(userId).build();
        }

        Address address;
        if (seeker.getAddressId() != null) {
            address = addressRepository.findById(seeker.getAddressId())
                    .orElseGet(() -> buildAddress(addressDto));
            updateAddressFields(address, addressDto);
        } else {
            address = buildAddress(addressDto);
        }

        Address savedAddress = addressRepository.save(address);
        seeker.setAddressId(savedAddress.getId());
        seekerRepository.save(seeker);
        return mapToResponse(seeker, user);
    }

    @Transactional
    public BasicInfoResponse deleteAddress() {
        User user = getAuthenticatedUser();
        validateSeekerUser(user);
        Seeker seeker = seekerRepository.findById(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Seeker profile not found"));

        if (seeker.getAddressId() != null) {
            seeker.setAddressId(null);
            seekerRepository.save(seeker);
        }
        return mapToResponse(seeker, user);
    }

    private Seeker buildSeeker(UUID userId, BasicInfoRequest request) {
        return Seeker.builder()
                .id(userId)
                .firstName(request.getFirstName())
                .middleName(request.getMiddleName())
                .lastName(request.getLastName())
                .phone(request.getPhone())
                .gender(request.getGender())
                .dateOfBirth(request.getDateOfBirth())
                .build();
    }

    private void updateSeekerFields(Seeker seeker, BasicInfoRequest request) {
        seeker.setFirstName(request.getFirstName());
        seeker.setMiddleName(request.getMiddleName());
        seeker.setLastName(request.getLastName());
        seeker.setPhone(request.getPhone());
        seeker.setGender(request.getGender());
        seeker.setDateOfBirth(request.getDateOfBirth());
    }

    private BasicInfoResponse mapToResponse(Seeker seeker, User user) {
        AddressDto addressDto = seeker.getAddressId() != null
                ? addressRepository.findById(seeker.getAddressId())
                        .map(this::mapToAddressDto)
                        .orElse(null)
                : null;

        return BasicInfoResponse.builder()
                .id(seeker.getId())
                .firstName(seeker.getFirstName())
                .middleName(seeker.getMiddleName())
                .lastName(seeker.getLastName())
                .phone(seeker.getPhone())
                .gender(seeker.getGender())
                .dateOfBirth(seeker.getDateOfBirth())
                .email(user.getEmail())
                .profileCompletion(calculateCompletion(seeker) + "%")
                .profileImageUrl(seeker.getProfileImageUrl())
                .address(addressDto)
                .build();
    }

    private int calculateCompletion(Seeker seeker) {
        int total = 7, completed = 0;
        if (isNotEmpty(seeker.getFirstName()))
            completed++;
        if (isNotEmpty(seeker.getMiddleName()))
            completed++;
        if (isNotEmpty(seeker.getLastName()))
            completed++;
        if (isNotEmpty(seeker.getPhone()))
            completed++;
        if (seeker.getGender() != null)
            completed++;
        if (seeker.getDateOfBirth() != null)
            completed++;
        if (seeker.getAddressId() != null)
            completed++;
        return (completed * 100) / total;
    }

    private Address buildAddress(AddressDto dto) {
        return Address.builder()
                .country(dto.getCountry())
                .region(dto.getRegion())
                .city(dto.getCity())
                .subCity(dto.getSubCity())
                .street(dto.getStreet())
                .createdAt(Instant.now())
                .build();
    }

    private void updateAddressFields(Address address, AddressDto dto) {
        address.setCountry(dto.getCountry());
        address.setRegion(dto.getRegion());
        address.setCity(dto.getCity());
        address.setSubCity(dto.getSubCity());
        address.setStreet(dto.getStreet());
    }

    private AddressDto mapToAddressDto(Address address) {
        return AddressDto.builder()
                .id(address.getId())
                .country(address.getCountry())
                .region(address.getRegion())
                .city(address.getCity())
                .subCity(address.getSubCity())
                .street(address.getStreet())
                .build();
    }

    private boolean isNotEmpty(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
