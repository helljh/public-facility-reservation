package com.helljh.publicfacility.room.repository;

import com.helljh.publicfacility.room.domain.RoomEquipment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoomEquipmentRepository extends JpaRepository<RoomEquipment, Long> {

    boolean existsByRoomIdAndEquipmentId(Long roomId, Long equipmentId);
}