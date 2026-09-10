package com.helljh.publicfacility.facility.repository;

import com.helljh.publicfacility.facility.domain.FacilityManager;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FacilityManagerRepository extends JpaRepository<FacilityManager, Long> {

    boolean existsByUserIdAndFacilityId(Long userId, Long facilityId);
}