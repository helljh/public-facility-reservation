package com.helljh.publicfacility.reservation.dto;

import com.helljh.publicfacility.reservation.domain.Reservation;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public record ReservationDetailResponse(
        Long reservationId,
        String reservationNumber,

        Long facilityId,
        String facilityName,
        String facilityAddress,

        Long roomId,
        String roomName,

        LocalDate reservationDate,
        LocalTime startTime,
        LocalTime endTime,

        Integer peopleCount,
        String status,

        boolean cancelable,
        LocalDateTime createdAt
) {

    public static ReservationDetailResponse of(
            Reservation reservation,
            boolean cancelable
    ) {

        return new ReservationDetailResponse(
                reservation.getId(),
                reservation.getReservationNumber(),

                reservation.getRoom().getFacility().getId(),
                reservation.getRoom().getFacility().getName(),
                reservation.getRoom().getFacility().getAddress(),

                reservation.getRoom().getId(),
                reservation.getRoom().getName(),

                reservation.getReservationDate(),
                reservation.getStartTime(),
                reservation.getEndTime(),

                reservation.getPeopleCount(),
                reservation.getStatus().name(),

                cancelable,
                reservation.getCreatedAt()
        );
    }
}