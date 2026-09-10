package com.helljh.publicfacility.room.service;

import com.helljh.publicfacility.common.exception.BusinessException;
import com.helljh.publicfacility.common.exception.ErrorCode;
import com.helljh.publicfacility.facility.domain.FacilityStatus;
import com.helljh.publicfacility.room.domain.Room;
import com.helljh.publicfacility.room.domain.RoomReservationMethod;
import com.helljh.publicfacility.room.dto.RoomDetailResponse;
import com.helljh.publicfacility.room.dto.RoomResponse;
import com.helljh.publicfacility.room.repository.RoomEquipmentRepository;
import com.helljh.publicfacility.room.repository.RoomRepository;
import com.helljh.publicfacility.room.repository.RoomReservationMethodRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RoomService {

    private final RoomRepository roomRepository;
    private final RoomEquipmentRepository roomEquipmentRepository;
    private final RoomReservationMethodRepository roomReservationMethodRepository;


    public List<RoomResponse> getRooms() {

        return roomRepository
                .findAllByIsActiveTrueAndFacilityStatusOrderByNameAsc(
                        FacilityStatus.ACTIVE
                )
                .stream()
                .map(RoomResponse::from)
                .toList();
    }

    public RoomDetailResponse getRoom(Long roomId) {

        Room room = roomRepository
                .findByIdAndIsActiveTrueAndFacilityStatus(
                        roomId,
                        FacilityStatus.ACTIVE
                )
                .orElseThrow(() ->
                        new BusinessException(
                                ErrorCode.ROOM_NOT_FOUND
                        )
                );

        List<RoomDetailResponse.EquipmentInfo> equipments =
                roomEquipmentRepository
                        .findAllByRoomId(roomId)
                        .stream()
                        .map(roomEquipment ->
                                new RoomDetailResponse.EquipmentInfo(
                                        roomEquipment
                                                .getEquipment()
                                                .getId(),
                                        roomEquipment
                                                .getEquipment()
                                                .getName()
                                )
                        )
                        .toList();

        RoomReservationMethod reservationMethod =
                roomReservationMethodRepository
                        .findByRoomId(roomId)
                        .orElse(null);

        return RoomDetailResponse.of(
                room,
                equipments,
                reservationMethod
        );
    }
}