package com.helljh.publicfacility.reservation.dto;

import com.helljh.publicfacility.reservation.domain.Reservation;

import java.time.LocalDateTime;

public record ReservationCancelResponse(
        Long reservationId,
        String reservationNumber,
        String status,
        LocalDateTime canceledAt
) {

    public static ReservationCancelResponse from(
            Reservation reservation
    ) {
        return new ReservationCancelResponse(
                reservation.getId(),
                reservation.getReservationNumber(),
                reservation.getStatus().name(),
                reservation.getCanceledAt()
        );
    }
}