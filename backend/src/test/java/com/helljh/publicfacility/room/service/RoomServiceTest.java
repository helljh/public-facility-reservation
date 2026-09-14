package com.helljh.publicfacility.room.service;

import com.helljh.publicfacility.common.exception.BusinessException;
import com.helljh.publicfacility.common.exception.ErrorCode;
import com.helljh.publicfacility.facility.domain.DayOfWeekType;
import com.helljh.publicfacility.facility.domain.Facility;
import com.helljh.publicfacility.facility.domain.FacilityOperationHours;
import com.helljh.publicfacility.facility.domain.FacilityStatus;
import com.helljh.publicfacility.facility.repository.FacilityClosedDateRepository;
import com.helljh.publicfacility.facility.repository.FacilityOperationHoursRepository;
import com.helljh.publicfacility.reservation.domain.Reservation;
import com.helljh.publicfacility.reservation.domain.ReservationStatus;
import com.helljh.publicfacility.reservation.repository.ReservationRepository;
import com.helljh.publicfacility.room.domain.Room;
import com.helljh.publicfacility.room.domain.RoomOperationHours;
import com.helljh.publicfacility.room.repository.RoomEquipmentRepository;
import com.helljh.publicfacility.room.repository.RoomOperationHoursRepository;
import com.helljh.publicfacility.room.repository.RoomRepository;
import com.helljh.publicfacility.room.repository.RoomReservationMethodRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.*;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoomServiceTest {

    @Mock
    private RoomRepository roomRepository;

    @Mock
    private RoomEquipmentRepository roomEquipmentRepository;

    @Mock
    private RoomReservationMethodRepository roomReservationMethodRepository;

    @Mock
    private RoomOperationHoursRepository roomOperationHoursRepository;

    @Mock
    private FacilityOperationHoursRepository facilityOperationHoursRepository;

    @Mock
    private FacilityClosedDateRepository facilityClosedDateRepository;

    @Mock
    private ReservationRepository reservationRepository;

    private RoomService roomService;

    private Clock fixedClock;

    @BeforeEach
    void setUp() {

        fixedClock = Clock.fixed(
                Instant.parse("2026-09-10T06:00:00Z"),
                ZoneId.of("Asia/Seoul")
        );

        roomService = new RoomService(
                roomRepository,
                roomEquipmentRepository,
                roomReservationMethodRepository,
                facilityClosedDateRepository,
                facilityOperationHoursRepository,
                roomOperationHoursRepository,
                reservationRepository,
                fixedClock
        );
    }

    @Test
    void 당일_현재시간_30분_이전_슬롯은_예약할_수_없다() {

        Long roomId = 10L;
        Long facilityId = 20L;

        Room room = mock(Room.class);
        Facility facility = mock(Facility.class);
        FacilityOperationHours facilityHours =
                mock(FacilityOperationHours.class);

        when(roomRepository.findByIdAndIsActiveTrueAndFacilityStatus(
                roomId,
                FacilityStatus.ACTIVE
        )).thenReturn(Optional.of(room));

        when(room.getId()).thenReturn(roomId);
        when(room.getFacility()).thenReturn(facility);

        when(facility.getId()).thenReturn(facilityId);

        when(facilityClosedDateRepository
                .existsByFacilityIdAndClosedDate(
                        facilityId,
                        LocalDate.of(2026, 9, 10)
                ))
                .thenReturn(false);

        when(roomOperationHoursRepository
                .findByRoomIdAndDayOfWeek(
                        roomId,
                        DayOfWeekType.THURSDAY
                ))
                .thenReturn(Optional.empty());

        when(facilityOperationHoursRepository
                .findByFacilityIdAndDayOfWeek(
                        facilityId,
                        DayOfWeekType.THURSDAY
                ))
                .thenReturn(Optional.of(facilityHours));

        when(facilityHours.getIsClosed())
                .thenReturn(false);

        when(facilityHours.getOpenTime())
                .thenReturn(LocalTime.of(14, 0));

        when(facilityHours.getCloseTime())
                .thenReturn(LocalTime.of(18, 0));

        when(reservationRepository.findAllByRoomIdAndReservationDateAndStatus(
                roomId,
                LocalDate.of(2026, 9, 10),
                ReservationStatus.RESERVED
        )).thenReturn(List.of());

        var response =
                roomService.getAvailability(
                        roomId,
                        LocalDate.of(2026, 9, 10)
                );

        assertThat(response.slots()).hasSize(4);

        assertThat(response.slots().get(0).available())
                .isFalse();

        assertThat(response.slots().get(1).available())
                .isFalse();

        assertThat(response.slots().get(2).available())
                .isTrue();

        assertThat(response.slots().get(3).available())
                .isTrue();
    }

    @Test
    void 시설_휴무일이면_예약가능_슬롯이_없다() {

        Long roomId = 10L;
        Long facilityId = 20L;

        Room room = mock(Room.class);
        Facility facility = mock(Facility.class);

        when(roomRepository.findByIdAndIsActiveTrueAndFacilityStatus(
                roomId,
                FacilityStatus.ACTIVE
        )).thenReturn(Optional.of(room));

        when(room.getFacility())
                .thenReturn(facility);

        when(facility.getId())
                .thenReturn(facilityId);

        when(facilityClosedDateRepository
                .existsByFacilityIdAndClosedDate(
                        facilityId,
                        LocalDate.of(2026, 9, 11)
                ))
                .thenReturn(true);

        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> roomService.getAvailability(
                                roomId,
                                LocalDate.of(2026, 9, 11)
                        )
                );

        assertThat(exception.getErrorCode())
                .isEqualTo(ErrorCode.FACILITY_CLOSED);

        verifyNoInteractions(
                roomOperationHoursRepository,
                facilityOperationHoursRepository,
                reservationRepository
        );
    }

    @Test
    void 기존_예약과_겹치는_슬롯은_예약할_수_없다() {

        Long roomId = 10L;
        Long facilityId = 20L;

        Room room = mock(Room.class);
        Facility facility = mock(Facility.class);
        FacilityOperationHours facilityHours =
                mock(FacilityOperationHours.class);

        Reservation existingReservation =
                mock(Reservation.class);

        LocalDate date =
                LocalDate.of(2026, 9, 11);

        when(roomRepository.findByIdAndIsActiveTrueAndFacilityStatus(
                roomId,
                FacilityStatus.ACTIVE
        )).thenReturn(Optional.of(room));

        when(room.getId())
                .thenReturn(roomId);

        when(room.getFacility())
                .thenReturn(facility);

        when(facility.getId())
                .thenReturn(facilityId);

        when(facilityClosedDateRepository
                .existsByFacilityIdAndClosedDate(
                        facilityId,
                        date
                ))
                .thenReturn(false);

        when(roomOperationHoursRepository
                .findByRoomIdAndDayOfWeek(
                        roomId,
                        DayOfWeekType.FRIDAY
                ))
                .thenReturn(Optional.empty());

        when(facilityOperationHoursRepository
                .findByFacilityIdAndDayOfWeek(
                        facilityId,
                        DayOfWeekType.FRIDAY
                ))
                .thenReturn(Optional.of(facilityHours));

        when(facilityHours.getIsClosed())
                .thenReturn(false);

        when(facilityHours.getOpenTime())
                .thenReturn(LocalTime.of(9, 0));

        when(facilityHours.getCloseTime())
                .thenReturn(LocalTime.of(13, 0));

        when(existingReservation.getStartTime())
                .thenReturn(LocalTime.of(10, 0));

        when(existingReservation.getEndTime())
                .thenReturn(LocalTime.of(11, 0));

        when(reservationRepository
                .findAllByRoomIdAndReservationDateAndStatus(
                        roomId,
                        date,
                        ReservationStatus.RESERVED
                ))
                .thenReturn(List.of(existingReservation));

        var response =
                roomService.getAvailability(
                        roomId,
                        date
                );

        assertThat(response.slots())
                .hasSize(4);

        assertThat(response.slots().get(0).available())
                .isTrue();

        assertThat(response.slots().get(1).available())
                .isFalse();

        assertThat(response.slots().get(2).available())
                .isTrue();

        assertThat(response.slots().get(3).available())
                .isTrue();
    }

    @Test
    void 회의실_운영시간이_있으면_시설_운영시간보다_우선한다() {

        Long roomId = 10L;
        Long facilityId = 20L;

        Room room = mock(Room.class);
        Facility facility = mock(Facility.class);

        RoomOperationHours roomHours =
                mock(RoomOperationHours.class);

        LocalDate date =
                LocalDate.of(2026, 9, 11);

        when(roomRepository.findByIdAndIsActiveTrueAndFacilityStatus(
                roomId,
                FacilityStatus.ACTIVE
        )).thenReturn(Optional.of(room));

        when(room.getId())
                .thenReturn(roomId);

        when(room.getFacility())
                .thenReturn(facility);

        when(facility.getId())
                .thenReturn(facilityId);

        when(facilityClosedDateRepository
                .existsByFacilityIdAndClosedDate(
                        facilityId,
                        date
                ))
                .thenReturn(false);

        when(roomOperationHoursRepository
                .findByRoomIdAndDayOfWeek(
                        roomId,
                        DayOfWeekType.FRIDAY
                ))
                .thenReturn(Optional.of(roomHours));

        when(roomHours.getIsClosed())
                .thenReturn(false);

        when(roomHours.getOpenTime())
                .thenReturn(LocalTime.of(13, 0));

        when(roomHours.getCloseTime())
                .thenReturn(LocalTime.of(17, 0));

        when(reservationRepository
                .findAllByRoomIdAndReservationDateAndStatus(
                        roomId,
                        date,
                        ReservationStatus.RESERVED
                ))
                .thenReturn(List.of());

        var response =
                roomService.getAvailability(
                        roomId,
                        date
                );

        assertThat(response.slots())
                .hasSize(4);

        assertThat(response.slots().get(0).startTime())
                .isEqualTo(LocalTime.of(13, 0));

        assertThat(response.slots().get(3).endTime())
                .isEqualTo(LocalTime.of(17, 0));

        verifyNoInteractions(
                facilityOperationHoursRepository
        );
    }

    @Test
    void 회의실이_해당요일_휴무이면_예약가능_슬롯이_없다() {

        Long roomId = 10L;
        Long facilityId = 20L;

        Room room = mock(Room.class);
        Facility facility = mock(Facility.class);

        RoomOperationHours roomHours =
                mock(RoomOperationHours.class);

        LocalDate date =
                LocalDate.of(2026, 9, 11);

        when(roomRepository.findByIdAndIsActiveTrueAndFacilityStatus(
                roomId,
                FacilityStatus.ACTIVE
        )).thenReturn(Optional.of(room));

        when(room.getId())
                .thenReturn(roomId);

        when(room.getFacility())
                .thenReturn(facility);

        when(facility.getId())
                .thenReturn(facilityId);

        when(facilityClosedDateRepository
                .existsByFacilityIdAndClosedDate(
                        facilityId,
                        date
                ))
                .thenReturn(false);

        when(roomOperationHoursRepository
                .findByRoomIdAndDayOfWeek(
                        roomId,
                        DayOfWeekType.FRIDAY
                ))
                .thenReturn(Optional.of(roomHours));

        when(roomHours.getIsClosed())
                .thenReturn(true);

        var response =
                roomService.getAvailability(
                        roomId,
                        date
                );

        assertThat(response.slots())
                .isEmpty();
    }

    @Test
    void 과거_날짜의_예약가능시간은_조회할_수_없다() {

        Long roomId = 10L;

        Room room = mock(Room.class);

        when(roomRepository.findByIdAndIsActiveTrueAndFacilityStatus(
                roomId,
                FacilityStatus.ACTIVE
        )).thenReturn(Optional.of(room));

        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> roomService.getAvailability(
                                roomId,
                                LocalDate.of(2026, 9, 9)
                        )
                );

        assertThat(exception.getErrorCode())
                .as(
                        "실제 ErrorCode: %s",
                        exception.getErrorCode()
                )
                .isEqualTo(ErrorCode.INVALID_RESERVATION_DATE);
    }

    @Test
    void 예약가능기간_30일을_초과해서_조회할_수_없다() {

        Long roomId = 10L;

        Room room = mock(Room.class);

        when(roomRepository.findByIdAndIsActiveTrueAndFacilityStatus(
                roomId,
                FacilityStatus.ACTIVE
        )).thenReturn(Optional.of(room));

        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> roomService.getAvailability(
                                roomId,
                                LocalDate.of(2026, 10, 11)
                        )
                );

        assertThat(exception.getErrorCode())
                .as(
                        "실제 ErrorCode: %s",
                        exception.getErrorCode()
                )
                .isEqualTo(ErrorCode.INVALID_RESERVATION_DATE);
    }
}