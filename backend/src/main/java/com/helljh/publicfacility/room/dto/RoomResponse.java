package com.helljh.publicfacility.room.dto;

import com.helljh.publicfacility.room.domain.Room;

public record RoomResponse(
        Long roomId,
        Long facilityId,
        String facilityName,
        String name,
        Integer capacity,
        String description,
        String imageUrl
) {

    public static RoomResponse from(Room room) {
        return new RoomResponse(
                room.getId(),
                room.getFacility().getId(),
                room.getFacility().getName(),
                room.getName(),
                room.getCapacity(),
                room.getDescription(),
                room.getImageUrl()
        );
    }
}