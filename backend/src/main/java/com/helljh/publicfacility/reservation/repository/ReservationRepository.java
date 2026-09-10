package com.helljh.publicfacility.reservation.repository;

import com.helljh.publicfacility.reservation.domain.Reservation;
import com.helljh.publicfacility.reservation.domain.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface ReservationRepository
        extends JpaRepository<Reservation, Long> {

    List<Reservation> findAllByRoomIdAndReservationDateAndStatus(
            Long roomId,
            LocalDate reservationDate,
            ReservationStatus status
    );
}