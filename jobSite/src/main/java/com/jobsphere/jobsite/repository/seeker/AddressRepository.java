package com.jobsphere.jobsite.repository.shared;

import com.jobsphere.jobsite.model.shared.Address;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface AddressRepository extends JpaRepository<Address, UUID> {
}