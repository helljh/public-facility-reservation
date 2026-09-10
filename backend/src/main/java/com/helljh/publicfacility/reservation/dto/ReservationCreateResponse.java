package com.helljh.publicfacility.reservation.dto;

import com.helljh.publicfacility.reservation.domain.Reservation;

public record ReservationCreateResponse(
        Long reservationId,
        String reservationNumber,
        Long roomId,
        String roomName,
        String facilityName,
        String reservationDate,
        String startTime,
        String endTime,
        Integer peopleCount,
        String status
) {

    public static ReservationCreateResponse from(
            Reservation reservation
    ) {
        return new ReservationCreateResponse(
                reservation.getId(),
                reservation.getReservationNumber(),
                reservation.getRoom().getId(),
                reservation.getRoom().getName(),
                reservation.getRoom().getFacility().getName(),
                reservation.getReservationDate().toString(),
                reservation.getStartTime().toString(),
                reservation.getEndTime().toString(),
                reservation.getPeopleCount(),
                reservation.getStatus().name()
        );
    }
}