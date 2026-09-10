package com.helljh.publicfacility.reservation.service;

import com.helljh.publicfacility.common.exception.BusinessException;
import com.helljh.publicfacility.common.exception.ErrorCode;
import com.helljh.publicfacility.facility.domain.DayOfWeekType;
import com.helljh.publicfacility.facility.domain.FacilityOperationHours;
import com.helljh.publicfacility.facility.domain.FacilityStatus;
import com.helljh.publicfacility.facility.repository.FacilityClosedDateRepository;
import com.helljh.publicfacility.facility.repository.FacilityOperationHoursRepository;
import com.helljh.publicfacility.reservation.domain.Reservation;
import com.helljh.publicfacility.reservation.domain.ReservationStatus;
import com.helljh.publicfacility.reservation.dto.*;
import com.helljh.publicfacility.reservation.repository.ReservationRepository;
import com.helljh.publicfacility.room.domain.ReservationMethodType;
import com.helljh.publicfacility.room.domain.Room;
import com.helljh.publicfacility.room.domain.RoomOperationHours;
import com.helljh.publicfacility.room.domain.RoomReservationMethod;
import com.helljh.publicfacility.room.repository.RoomOperationHoursRepository;
import com.helljh.publicfacility.room.repository.RoomRepository;
import com.helljh.publicfacility.room.repository.RoomReservationMethodRepository;
import com.helljh.publicfacility.user.domain.User;
import com.helljh.publicfacility.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.data.domain.Pageable;

import java.time.*;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final RoomRepository roomRepository;
    private final UserRepository userRepository;
    private final FacilityClosedDateRepository facilityClosedDateRepository;
    private final FacilityOperationHoursRepository facilityOperationHoursRepository;
    private final RoomOperationHoursRepository roomOperationHoursRepository;
    private final RoomReservationMethodRepository roomReservationMethodRepository;

    @Transactional
    public ReservationCreateResponse createReservation(
            Long userId,
            ReservationCreateRequest request
    ) {

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new BusinessException(
                                ErrorCode.USER_NOT_FOUND
                        )
                );

        Room room = roomRepository
                .findByIdAndIsActiveTrueAndFacilityStatus(
                        request.roomId(),
                        FacilityStatus.ACTIVE
                )
                .orElseThrow(() ->
                        new BusinessException(
                                ErrorCode.ROOM_NOT_FOUND
                        )
                );

        validateReservationDate(
                request.reservationDate()
        );

        validateFacilityClosedDate(room, request.reservationDate());

        validateReservationMethod(room);

        validateOperationHours(
                room,
                request.reservationDate(),
                request.startTime(),
                request.endTime()
        );

        validateReservationTime(
                request
        );

        validatePeopleCount(
                room,
                request.peopleCount()
        );

        boolean conflict =
                reservationRepository
                        .existsOverlappingReservation(
                                room.getId(),
                                request.reservationDate(),
                                request.startTime(),
                                request.endTime()
                        );

        if (conflict) {
            throw new BusinessException(
                    ErrorCode.RESERVATION_TIME_CONFLICT
            );
        }

        String reservationNumber =
                generateReservationNumber();

        Reservation reservation =
                Reservation.create(
                        reservationNumber,
                        user,
                        room,
                        request.reservationDate(),
                        request.startTime(),
                        request.endTime(),
                        request.peopleCount()
                );

        try {
            Reservation saved =
                    reservationRepository.saveAndFlush(reservation);

            return ReservationCreateResponse.from(saved);

        } catch (DataIntegrityViolationException e) {

            throw new BusinessException(
                    ErrorCode.RESERVATION_TIME_CONFLICT
            );
        }
    }

    @Transactional(readOnly = true)
    public Page<ReservationSummaryResponse> getReservations(
            Long userId,
            ReservationStatus status,
            Pageable pageable
    ) {

        Page<Reservation> reservations;

        if (status == null) {
            reservations =
                    reservationRepository
                            .findAllByUserId(
                                    userId,
                                    pageable
                            );
        } else {
            reservations =
                    reservationRepository
                            .findAllByUserIdAndStatus(
                                    userId,
                                    status,
                                    pageable
                            );
        }

        return reservations.map(
                reservation ->
                        ReservationSummaryResponse.of(
                                reservation,
                                isCancelable(reservation)
                        )
        );
    }

    @Transactional(readOnly = true)
    public ReservationDetailResponse getReservation(
            Long userId,
            Long reservationId
    ) {

        Reservation reservation =
                reservationRepository
                        .findById(reservationId)
                        .orElseThrow(() ->
                                new BusinessException(
                                        ErrorCode.RESERVATION_NOT_FOUND
                                )
                        );

        if (!reservation.getUser()
                .getId()
                .equals(userId)) {

            throw new BusinessException(
                    ErrorCode.RESERVATION_ACCESS_DENIED
            );
        }

        return ReservationDetailResponse.of(
                reservation,
                isCancelable(reservation)
        );
    }

    @Transactional
    public ReservationCancelResponse cancelReservation(
            Long userId,
            Long reservationId
    ) {

        Reservation reservation =
                reservationRepository
                        .findById(reservationId)
                        .orElseThrow(() ->
                                new BusinessException(
                                        ErrorCode.RESERVATION_NOT_FOUND
                                )
                        );

        if (!reservation.getUser()
                .getId()
                .equals(userId)) {

            throw new BusinessException(
                    ErrorCode.RESERVATION_ACCESS_DENIED
            );
        }

        if (reservation.getStatus()
                == ReservationStatus.CANCELED) {

            throw new BusinessException(
                    ErrorCode.RESERVATION_ALREADY_CANCELED
            );
        }

        if (reservation.getStatus()
                != ReservationStatus.RESERVED) {

            throw new BusinessException(
                    ErrorCode.RESERVATION_CANNOT_CANCEL
            );
        }

        LocalDateTime reservationStart =
                LocalDateTime.of(
                        reservation.getReservationDate(),
                        reservation.getStartTime()
                );

        LocalDateTime cancelDeadline =
                reservationStart.minusHours(1);

        if (LocalDateTime.now()
                .isAfter(cancelDeadline)) {

            throw new BusinessException(
                    ErrorCode.RESERVATION_CANNOT_CANCEL
            );
        }

        reservation.cancel();

        return ReservationCancelResponse.from(
                reservation
        );
    }

    @Transactional
    public void completeExpiredReservations() {

        LocalDateTime now = LocalDateTime.now();

        List<Reservation> reservations =
                reservationRepository.findExpiredReservations(
                        ReservationStatus.RESERVED,
                        now.toLocalDate(),
                        now.toLocalTime()
                );

        for (Reservation reservation : reservations) {
            reservation.complete();
        }
    }

    private void validateReservationDate(LocalDate date) {

        LocalDate today = LocalDate.now();

        if (date.isBefore(today)
                || date.isAfter(today.plusDays(30))) {

            throw new BusinessException(
                    ErrorCode.INVALID_RESERVATION_DATE
            );
        }
    }

    private void validateReservationTime(
            ReservationCreateRequest request
    ) {

        if (!request.startTime()
                .isBefore(request.endTime())) {

            throw new BusinessException(
                    ErrorCode.INVALID_RESERVATION_TIME
            );
        }

        Duration duration =
                Duration.between(
                        request.startTime(),
                        request.endTime()
                );

        long minutes = duration.toMinutes();

        if (minutes < 60
                || minutes > 240
                || minutes % 60 != 0) {

            throw new BusinessException(
                    ErrorCode.INVALID_RESERVATION_TIME
            );
        }

        if (request.reservationDate()
                .equals(LocalDate.now())) {

            LocalDateTime reservationStart =
                    LocalDateTime.of(
                            request.reservationDate(),
                            request.startTime()
                    );

            if (reservationStart.isBefore(
                    LocalDateTime.now().plusMinutes(30)
            )) {
                throw new BusinessException(
                        ErrorCode.INVALID_RESERVATION_TIME
                );
            }
        }
    }

    private void validatePeopleCount(
            Room room,
            Integer peopleCount
    ) {

        if (peopleCount < 1
                || peopleCount > room.getCapacity()) {

            throw new BusinessException(
                    ErrorCode.EXCEED_ROOM_CAPACITY
            );
        }
    }

    private String generateReservationNumber() {

        return "RSV-"
                + UUID.randomUUID()
                .toString()
                .substring(0, 8)
                .toUpperCase();
    }

    private boolean isCancelable(
            Reservation reservation
    ) {

        if (reservation.getStatus()
                != ReservationStatus.RESERVED) {

            return false;
        }

        LocalDateTime reservationStart =
                LocalDateTime.of(
                        reservation.getReservationDate(),
                        reservation.getStartTime()
                );

        LocalDateTime cancelDeadline =
                reservationStart.minusHours(1);

        return !LocalDateTime.now()
                .isAfter(cancelDeadline);
    }

    private void validateFacilityClosedDate(
            Room room,
            LocalDate reservationDate
    ) {

        boolean closed =
                facilityClosedDateRepository
                        .existsByFacilityIdAndClosedDate(
                                room.getFacility().getId(),
                                reservationDate
                        );

        if (closed) {
            throw new BusinessException(
                    ErrorCode.FACILITY_CLOSED
            );
        }
    }

    private void validateReservationMethod(Room room) {

        RoomReservationMethod reservationMethod =
                roomReservationMethodRepository
                        .findByRoomId(room.getId())
                        .orElseThrow(() ->
                                new BusinessException(
                                        ErrorCode.RESERVATION_METHOD_NOT_FOUND
                                )
                        );

        if (reservationMethod.getMethodType()
                != ReservationMethodType.INTERNAL) {

            throw new BusinessException(
                    ErrorCode.INTERNAL_RESERVATION_NOT_ALLOWED
            );
        }
    }

    private void validateOperationHours(
            Room room,
            LocalDate reservationDate,
            LocalTime startTime,
            LocalTime endTime
    ) {

        DayOfWeekType dayOfWeek =
                DayOfWeekType.valueOf(
                        reservationDate.getDayOfWeek().name()
                );

        Optional<RoomOperationHours> roomHours =
                roomOperationHoursRepository
                        .findByRoomIdAndDayOfWeek(
                                room.getId(),
                                dayOfWeek
                        );

        LocalTime openTime;
        LocalTime closeTime;
        boolean closed;

        if (roomHours.isPresent()) {

            RoomOperationHours hours =
                    roomHours.get();

            openTime = hours.getOpenTime();
            closeTime = hours.getCloseTime();
            closed = hours.getIsClosed();

        } else {

            FacilityOperationHours hours =
                    facilityOperationHoursRepository
                            .findByFacilityIdAndDayOfWeek(
                                    room.getFacility().getId(),
                                    dayOfWeek
                            )
                            .orElseThrow(() ->
                                    new BusinessException(
                                            ErrorCode.FACILITY_CLOSED
                                    )
                            );

            openTime = hours.getOpenTime();
            closeTime = hours.getCloseTime();
            closed = hours.getIsClosed();
        }

        if (closed) {
            throw new BusinessException(
                    ErrorCode.FACILITY_CLOSED
            );
        }

        if (startTime.isBefore(openTime)
                || endTime.isAfter(closeTime)) {

            throw new BusinessException(
                    ErrorCode.OUTSIDE_OPERATION_HOURS
            );
        }
    }
}