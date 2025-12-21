package com.jobsphere.jobsite.dto.shared;

import com.jobsphere.jobsite.constant.JobConstants;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AddressCreateRequest(
    @NotBlank
    @Size(max = JobConstants.ADDRESS_COUNTRY_MAX_LENGTH)
    String country,

    @Size(max = JobConstants.ADDRESS_REGION_MAX_LENGTH)
    String region,

    @Size(max = JobConstants.ADDRESS_CITY_MAX_LENGTH)
    String city,

    @Size(max = JobConstants.ADDRESS_SUB_CITY_MAX_LENGTH)
    String subCity,

    @Size(max = JobConstants.ADDRESS_STREET_MAX_LENGTH)
    String street
) {}
