package com.helljh.publicfacility.facility.repository;

import com.helljh.publicfacility.facility.domain.FacilityOperationHours;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FacilityOperationHoursRepository
        extends JpaRepository<FacilityOperationHours, Long> {
}