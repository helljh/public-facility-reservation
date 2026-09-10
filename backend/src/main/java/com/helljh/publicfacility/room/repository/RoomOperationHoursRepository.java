package com.helljh.publicfacility.room.repository;

import com.helljh.publicfacility.room.domain.RoomOperationHours;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoomOperationHoursRepository
        extends JpaRepository<RoomOperationHours, Long> {
}