package com.helljh.publicfacility.room.repository;

import com.helljh.publicfacility.room.domain.Room;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoomRepository extends JpaRepository<Room, Long> {
}