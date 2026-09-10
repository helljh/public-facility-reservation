package com.helljh.publicfacility.reservation.repository;

import com.helljh.publicfacility.reservation.domain.Reservation;
import com.helljh.publicfacility.reservation.domain.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface ReservationRepository
        extends JpaRepository<Reservation, Long> {

    List<Reservation> findAllByRoomIdAndReservationDateAndStatus(
            Long roomId,
            LocalDate reservationDate,
            ReservationStatus status
    );

    @Query("""
        select count(r) > 0
        from Reservation r
        where r.room.id = :roomId
          and r.reservationDate = :reservationDate
          and r.status = com.helljh.publicfacility.reservation.domain.ReservationStatus.RESERVED
          and r.startTime < :endTime
          and r.endTime > :startTime
        """)
    boolean existsOverlappingReservation(
            @Param("roomId") Long roomId,
            @Param("reservationDate") LocalDate reservationDate,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime
    );
}