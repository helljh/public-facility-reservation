package com.helljh.publicfacility.room.service;

import com.helljh.publicfacility.common.exception.BusinessException;
import com.helljh.publicfacility.common.exception.ErrorCode;
import com.helljh.publicfacility.facility.domain.DayOfWeekType;
import com.helljh.publicfacility.facility.domain.FacilityOperationHours;
import com.helljh.publicfacility.facility.domain.FacilityStatus;
import com.helljh.publicfacility.facility.repository.FacilityClosedDateRepository;
import com.helljh.publicfacility.facility.repository.FacilityOperationHoursRepository;
import com.helljh.publicfacility.reservation.domain.Reservation;
import com.helljh.publicfacility.reservation.domain.ReservationStatus;
import com.helljh.publicfacility.reservation.repository.ReservationRepository;
import com.helljh.publicfacility.room.domain.Room;
import com.helljh.publicfacility.room.domain.RoomOperationHours;
import com.helljh.publicfacility.room.domain.RoomReservationMethod;
import com.helljh.publicfacility.room.dto.RoomAvailabilityResponse;
import com.helljh.publicfacility.room.dto.RoomDetailResponse;
import com.helljh.publicfacility.room.dto.RoomResponse;
import com.helljh.publicfacility.room.repository.RoomEquipmentRepository;
import com.helljh.publicfacility.room.repository.RoomOperationHoursRepository;
import com.helljh.publicfacility.room.repository.RoomRepository;
import com.helljh.publicfacility.room.repository.RoomReservationMethodRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RoomService {

    private final RoomRepository roomRepository;
    private final RoomEquipmentRepository roomEquipmentRepository;
    private final RoomReservationMethodRepository roomReservationMethodRepository;
    private final FacilityClosedDateRepository facilityClosedDateRepository;
    private final FacilityOperationHoursRepository facilityOperationHoursRepository;
    private final RoomOperationHoursRepository roomOperationHoursRepository;
    private final ReservationRepository reservationRepository;

    private final Clock clock;

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

    public RoomAvailabilityResponse getAvailability(
            Long roomId,
            LocalDate date
    ) {

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

        validateReservationDate(date);

        Long facilityId = room.getFacility().getId();

        if (facilityClosedDateRepository
                .existsByFacilityIdAndClosedDate(
                        facilityId,
                        date
                )) {

            throw new BusinessException(
                    ErrorCode.FACILITY_CLOSED
            );
        }

        DayOfWeekType dayOfWeek =
                DayOfWeekType.valueOf(
                        date.getDayOfWeek().name()
                );

        LocalTime openTime;
        LocalTime closeTime;

        Optional<RoomOperationHours> roomHours =
                roomOperationHoursRepository
                        .findByRoomIdAndDayOfWeek(
                                roomId,
                                dayOfWeek
                        );

        if (roomHours.isPresent()) {

            RoomOperationHours hours = roomHours.get();

            if (Boolean.TRUE.equals(hours.getIsClosed())) {
                return emptyAvailability(room, date);
            }

            openTime = hours.getOpenTime();
            closeTime = hours.getCloseTime();

        } else {

            FacilityOperationHours facilityHours =
                    facilityOperationHoursRepository
                            .findByFacilityIdAndDayOfWeek(
                                    facilityId,
                                    dayOfWeek
                            )
                            .orElse(null);

            if (facilityHours == null
                    || Boolean.TRUE.equals(
                    facilityHours.getIsClosed()
            )) {

                return emptyAvailability(room, date);
            }

            openTime = facilityHours.getOpenTime();
            closeTime = facilityHours.getCloseTime();
        }

        List<Reservation> reservations =
                reservationRepository
                        .findAllByRoomIdAndReservationDateAndStatus(
                                roomId,
                                date,
                                ReservationStatus.RESERVED
                        );

        List<RoomAvailabilityResponse.SlotResponse> slots =
                createSlots(
                        date,
                        openTime,
                        closeTime,
                        reservations
                );

        return new RoomAvailabilityResponse(
                room.getId(),
                room.getName(),
                date,
                slots
        );
    }

    private void validateReservationDate(LocalDate date) {

        LocalDate today = LocalDate.now(clock);
        LocalDate maxDate = today.plusDays(30);

        if (date.isBefore(today)
                || date.isAfter(maxDate)) {

            throw new BusinessException(
                    ErrorCode.INVALID_RESERVATION_DATE
            );
        }
    }

    private RoomAvailabilityResponse emptyAvailability(
            Room room,
            LocalDate date
    ) {

        return new RoomAvailabilityResponse(
                room.getId(),
                room.getName(),
                date,
                List.of()
        );
    }

    private List<RoomAvailabilityResponse.SlotResponse>
    createSlots(
            LocalDate date,
            LocalTime openTime,
            LocalTime closeTime,
            List<Reservation> reservations
    ) {

        List<RoomAvailabilityResponse.SlotResponse> slots =
                new ArrayList<>();

        LocalTime current = openTime;

        while (!current.plusHours(1).isAfter(closeTime)) {

            LocalTime startTime = current;
            LocalTime endTime = current.plusHours(1);

            boolean available = true;

            // 당일 예약은 시작시간 기준 30분 전에 마감
            if (date.equals(LocalDate.now(clock))) {

                LocalDateTime reservationStart =
                        LocalDateTime.of(
                                date,
                                startTime
                        );

                LocalDateTime minimumStart =
                        LocalDateTime.now(clock)
                                .plusMinutes(30);

                if (reservationStart.isBefore(minimumStart)) {
                    available = false;
                }
            }

            // 기존 예약과 시간이 겹치는지 확인
            boolean overlaps =
                    reservations.stream()
                            .anyMatch(reservation ->
                                    startTime.isBefore(
                                            reservation.getEndTime()
                                    )
                                            &&
                                            endTime.isAfter(
                                                    reservation.getStartTime()
                                            )
                            );

            if (overlaps) {
                available = false;
            }

            slots.add(
                    new RoomAvailabilityResponse.SlotResponse(
                            startTime,
                            endTime,
                            available
                    )
            );

            current = endTime;
        }

        return slots;
    }
}