package com.helljh.publicfacility.room.repository;

import com.helljh.publicfacility.room.domain.RoomReservationMethod;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoomReservationMethodRepository
        extends JpaRepository<RoomReservationMethod, Long> {

    Optional<RoomReservationMethod> findByRoomId(Long roomId);
}