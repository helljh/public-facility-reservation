package com.helljh.publicfacility.facility.repository;

import com.helljh.publicfacility.facility.domain.DayOfWeekType;
import com.helljh.publicfacility.facility.domain.FacilityOperationHours;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FacilityOperationHoursRepository
        extends JpaRepository<FacilityOperationHours, Long> {

    Optional<FacilityOperationHours>
    findByFacilityIdAndDayOfWeek(
            Long facilityId,
            DayOfWeekType dayOfWeek
    );
}