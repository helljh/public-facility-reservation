package com.helljh.publicfacility.facility.repository;

import com.helljh.publicfacility.facility.domain.FacilityClosedDate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;

public interface FacilityClosedDateRepository
        extends JpaRepository<FacilityClosedDate, Long> {

    boolean existsByFacilityIdAndClosedDate(
            Long facilityId,
            LocalDate closedDate
    );
}