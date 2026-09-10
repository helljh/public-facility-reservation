package com.helljh.publicfacility.room.dto;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public record RoomAvailabilityResponse(
        Long roomId,
        String roomName,
        LocalDate date,
        List<SlotResponse> slots
) {

    public record SlotResponse(
            LocalTime startTime,
            LocalTime endTime,
            boolean available
    ) {
    }
}