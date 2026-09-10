package com.helljh.publicfacility.room.dto;

import com.helljh.publicfacility.room.domain.Room;
import com.helljh.publicfacility.room.domain.RoomReservationMethod;

import java.util.List;

public record RoomDetailResponse(
        Long roomId,
        Long facilityId,
        String facilityName,
        String facilityAddress,
        String name,
        Integer capacity,
        String description,
        String imageUrl,
        List<EquipmentInfo> equipments,
        ReservationMethodInfo reservationMethod
) {

    public record EquipmentInfo(
            Long equipmentId,
            String name
    ) {
    }

    public record ReservationMethodInfo(
            String methodType,
            String externalUrl,
            String phone,
            String description
    ) {
        public static ReservationMethodInfo from(
                RoomReservationMethod method
        ) {
            if (method == null) {
                return null;
            }

            return new ReservationMethodInfo(
                    method.getMethodType().name(),
                    method.getExternalUrl(),
                    method.getPhone(),
                    method.getDescription()
            );
        }
    }

    public static RoomDetailResponse of(
            Room room,
            List<EquipmentInfo> equipments,
            RoomReservationMethod reservationMethod
    ) {
        return new RoomDetailResponse(
                room.getId(),
                room.getFacility().getId(),
                room.getFacility().getName(),
                room.getFacility().getAddress(),
                room.getName(),
                room.getCapacity(),
                room.getDescription(),
                room.getImageUrl(),
                equipments,
                ReservationMethodInfo.from(reservationMethod)
        );
    }
}