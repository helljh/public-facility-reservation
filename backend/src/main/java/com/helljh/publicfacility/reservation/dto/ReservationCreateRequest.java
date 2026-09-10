package com.helljh.publicfacility.reservation.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;

public record ReservationCreateRequest(

        @NotNull
        Long roomId,

        @NotNull
        LocalDate reservationDate,

        @NotNull
        LocalTime startTime,

        @NotNull
        LocalTime endTime,

        @NotNull
        @Min(1)
        Integer peopleCount
) {
}