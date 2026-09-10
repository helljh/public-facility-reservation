package com.helljh.publicfacility.reservation.dto;

import com.helljh.publicfacility.reservation.domain.Reservation;

import java.time.LocalDate;
import java.time.LocalTime;

public record ReservationSummaryResponse(
        Long reservationId,
        String reservationNumber,
        Long roomId,
        String roomName,
        Long facilityId,
        String facilityName,
        LocalDate reservationDate,
        LocalTime startTime,
        LocalTime endTime,
        Integer peopleCount,
        String status,
        boolean cancelable
) {

    public static ReservationSummaryResponse of(
            Reservation reservation,
            boolean cancelable
    ) {
        return new ReservationSummaryResponse(
                reservation.getId(),
                reservation.getReservationNumber(),
                reservation.getRoom().getId(),
                reservation.getRoom().getName(),
                reservation.getRoom().getFacility().getId(),
                reservation.getRoom().getFacility().getName(),
                reservation.getReservationDate(),
                reservation.getStartTime(),
                reservation.getEndTime(),
                reservation.getPeopleCount(),
                reservation.getStatus().name(),
                cancelable
        );
    }
}