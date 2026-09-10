package com.helljh.publicfacility.facility.repository;

import com.helljh.publicfacility.facility.domain.Facility;
import com.helljh.publicfacility.facility.domain.FacilityStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FacilityRepository
        extends JpaRepository<Facility, Long> {

    List<Facility> findAllByStatusOrderByNameAsc(
            FacilityStatus status
    );

    Optional<Facility> findByIdAndStatus(
            Long id,
            FacilityStatus status
    );
}