package com.helljh.publicfacility.room.repository;

import com.helljh.publicfacility.facility.domain.DayOfWeekType;
import com.helljh.publicfacility.room.domain.RoomOperationHours;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.DayOfWeek;
import java.util.Optional;

public interface RoomOperationHoursRepository
        extends JpaRepository<RoomOperationHours, Long> {

    Optional<RoomOperationHours>
    findByRoomIdAndDayOfWeek(
            Long roomId,
            DayOfWeekType dayOfWeek
    );
}