package com.helljh.publicfacility.facility.repository;

import com.helljh.publicfacility.facility.domain.Facility;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FacilityRepository extends JpaRepository<Facility, Long> {
}