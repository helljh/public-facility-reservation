package com.helljh.publicfacility.reservation.service;

import com.helljh.publicfacility.common.exception.BusinessException;
import com.helljh.publicfacility.common.exception.ErrorCode;
import com.helljh.publicfacility.facility.domain.FacilityStatus;
import com.helljh.publicfacility.reservation.domain.Reservation;
import com.helljh.publicfacility.reservation.dto.ReservationCreateRequest;
import com.helljh.publicfacility.reservation.dto.ReservationCreateResponse;
import com.helljh.publicfacility.reservation.repository.ReservationRepository;
import com.helljh.publicfacility.room.domain.Room;
import com.helljh.publicfacility.room.repository.RoomRepository;
import com.helljh.publicfacility.user.domain.User;
import com.helljh.publicfacility.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final RoomRepository roomRepository;
    private final UserRepository userRepository;

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
}