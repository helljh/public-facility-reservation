package com.helljh.publicfacility.room.repository;

import com.helljh.publicfacility.facility.domain.FacilityStatus;
import com.helljh.publicfacility.room.domain.Room;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RoomRepository extends JpaRepository<Room, Long> {

    List<Room> findAllByIsActiveTrueAndFacilityStatusOrderByNameAsc(
            FacilityStatus status
    );

    Optional<Room> findByIdAndIsActiveTrueAndFacilityStatus(
            Long id,
            FacilityStatus status
    );
}