package com.helljh.publicfacility.room.repository;

import com.helljh.publicfacility.room.domain.Equipment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EquipmentRepository extends JpaRepository<Equipment, Long> {

    boolean existsByName(String name);
}