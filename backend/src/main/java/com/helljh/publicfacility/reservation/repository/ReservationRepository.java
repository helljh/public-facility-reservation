package com.helljh.publicfacility.reservation.repository;

import com.helljh.publicfacility.reservation.domain.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReservationRepository
        extends JpaRepository<Reservation, Long> {
}