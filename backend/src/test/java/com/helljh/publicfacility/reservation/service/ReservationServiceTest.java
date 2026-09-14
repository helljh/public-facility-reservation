package com.helljh.publicfacility.reservation.service;

import com.helljh.publicfacility.common.exception.BusinessException;
import com.helljh.publicfacility.common.exception.ErrorCode;
import com.helljh.publicfacility.facility.domain.DayOfWeekType;
import com.helljh.publicfacility.facility.domain.Facility;
import com.helljh.publicfacility.facility.domain.FacilityOperationHours;
import com.helljh.publicfacility.facility.domain.FacilityStatus;
import com.helljh.publicfacility.reservation.dto.ReservationCancelResponse;
import com.helljh.publicfacility.reservation.dto.ReservationCreateRequest;
import com.helljh.publicfacility.reservation.repository.ReservationRepository;
import com.helljh.publicfacility.room.domain.ReservationMethodType;
import com.helljh.publicfacility.room.domain.Room;
import com.helljh.publicfacility.room.domain.RoomReservationMethod;
import com.helljh.publicfacility.user.repository.UserRepository;
import com.helljh.publicfacility.room.repository.RoomRepository;
import com.helljh.publicfacility.facility.repository.FacilityClosedDateRepository;
import com.helljh.publicfacility.facility.repository.FacilityOperationHoursRepository;
import com.helljh.publicfacility.room.repository.RoomOperationHoursRepository;
import com.helljh.publicfacility.room.repository.RoomReservationMethodRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.*;

import com.helljh.publicfacility.reservation.domain.Reservation;
import com.helljh.publicfacility.reservation.domain.ReservationStatus;
import com.helljh.publicfacility.reservation.dto.ReservationDetailResponse;
import com.helljh.publicfacility.user.domain.User;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoomRepository roomRepository;

    @Mock
    private FacilityClosedDateRepository facilityClosedDateRepository;

    @Mock
    private FacilityOperationHoursRepository facilityOperationHoursRepository;

    @Mock
    private RoomOperationHoursRepository roomOperationHoursRepository;

    @Mock
    private RoomReservationMethodRepository roomReservationMethodRepository;

    private ReservationService reservationService;

    private Clock fixedClock;

    @BeforeEach
    void setUp() {

        fixedClock = Clock.fixed(
                Instant.parse("2026-09-10T06:00:00Z"),
                ZoneId.of("Asia/Seoul")
        );

        reservationService = new ReservationService(
                reservationRepository,
                roomRepository,
                userRepository,
                facilityClosedDateRepository,
                facilityOperationHoursRepository,
                roomOperationHoursRepository,
                roomReservationMethodRepository,
                fixedClock
        );
    }

    @Test
    void 예약_시작_1시간_전까지는_취소_가능하다() {

        User user = mock(User.class);
        Facility facility = mock(Facility.class);
        Room room = mock(Room.class);
        Reservation reservation = mock(Reservation.class);

        when(user.getId()).thenReturn(1L);

        when(facility.getId()).thenReturn(1L);
        when(facility.getName()).thenReturn("테스트 시설");
        when(facility.getAddress()).thenReturn("서울시");

        when(room.getId()).thenReturn(1L);
        when(room.getName()).thenReturn("회의실 A");
        when(room.getFacility()).thenReturn(facility);

        when(reservation.getId()).thenReturn(1L);
        when(reservation.getReservationNumber())
                .thenReturn("RSV-TEST1234");
        when(reservation.getUser()).thenReturn(user);
        when(reservation.getRoom()).thenReturn(room);

        when(reservation.getReservationDate())
                .thenReturn(LocalDate.of(2026, 9, 10));

        when(reservation.getStartTime())
                .thenReturn(LocalTime.of(17, 0));

        when(reservation.getEndTime())
                .thenReturn(LocalTime.of(18, 0));

        when(reservation.getPeopleCount())
                .thenReturn(4);

        when(reservation.getStatus())
                .thenReturn(ReservationStatus.RESERVED);

        when(reservationRepository.findById(1L))
                .thenReturn(Optional.of(reservation));

        ReservationDetailResponse response =
                reservationService.getReservation(
                        1L,
                        1L
                );

        assertThat(response.cancelable())
                .isTrue();
    }

    @Test
    void 예약_시작_1시간_이내에는_취소할_수_없다() {

        User user = mock(User.class);
        Facility facility = mock(Facility.class);
        Room room = mock(Room.class);
        Reservation reservation = mock(Reservation.class);

        when(user.getId()).thenReturn(1L);

        when(facility.getId()).thenReturn(1L);
        when(facility.getName()).thenReturn("테스트 시설");
        when(facility.getAddress()).thenReturn("서울시");

        when(room.getId()).thenReturn(1L);
        when(room.getName()).thenReturn("회의실 A");
        when(room.getFacility()).thenReturn(facility);

        when(reservation.getId()).thenReturn(1L);
        when(reservation.getReservationNumber())
                .thenReturn("RSV-TEST1234");
        when(reservation.getUser()).thenReturn(user);
        when(reservation.getRoom()).thenReturn(room);

        when(reservation.getReservationDate())
                .thenReturn(LocalDate.of(2026, 9, 10));

        // 현재 15:00 / 예약 시작 15:30
        when(reservation.getStartTime())
                .thenReturn(LocalTime.of(15, 30));

        when(reservation.getEndTime())
                .thenReturn(LocalTime.of(16, 30));

        when(reservation.getPeopleCount())
                .thenReturn(4);

        when(reservation.getStatus())
                .thenReturn(ReservationStatus.RESERVED);

        when(reservationRepository.findById(1L))
                .thenReturn(Optional.of(reservation));

        ReservationDetailResponse response =
                reservationService.getReservation(
                        1L,
                        1L
                );

        assertThat(response.cancelable())
                .isFalse();
    }

    @Test
    void 예약을_정상적으로_취소할_수_있다() {

        User user = mock(User.class);
        Reservation reservation = mock(Reservation.class);

        Long userId = 1L;
        Long reservationId = 100L;

        when(user.getId()).thenReturn(userId);

        when(reservation.getId()).thenReturn(reservationId);
        when(reservation.getReservationNumber())
                .thenReturn("RSV-TEST1234");

        when(reservation.getUser())
                .thenReturn(user);

        when(reservation.getReservationDate())
                .thenReturn(LocalDate.of(2026, 9, 10));

        when(reservation.getStartTime())
                .thenReturn(LocalTime.of(17, 0));

        when(reservation.getStatus())
                .thenReturn(
                        ReservationStatus.RESERVED,
                        ReservationStatus.RESERVED,
                        ReservationStatus.CANCELED
                );

        when(reservation.getCanceledAt())
                .thenReturn(
                        LocalDateTime.of(
                                2026, 9, 10, 15, 0
                        )
                );

        when(reservationRepository.findById(reservationId))
                .thenReturn(Optional.of(reservation));

        ReservationCancelResponse response =
                reservationService.cancelReservation(
                        userId,
                        reservationId
                );

        assertThat(response.status())
                .isEqualTo("CANCELED");

        verify(reservation).cancel(
                LocalDateTime.of(
                        2026, 9, 10, 15, 0
                )
        );
    }

    @Test
    void 이미_취소된_예약은_다시_취소할_수_없다() {

        User user = mock(User.class);
        Reservation reservation = mock(Reservation.class);

        Long userId = 1L;
        Long reservationId = 100L;

        when(user.getId()).thenReturn(userId);
        when(reservation.getUser()).thenReturn(user);

        when(reservation.getStatus())
                .thenReturn(ReservationStatus.CANCELED);

        when(reservationRepository.findById(reservationId))
                .thenReturn(Optional.of(reservation));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> reservationService.cancelReservation(
                        userId,
                        reservationId
                )
        );

        assertThat(exception.getErrorCode())
                .isEqualTo(ErrorCode.RESERVATION_ALREADY_CANCELED);
    }

    @Test
    void 취소_마감시간이_지난_예약은_취소할_수_없다() {

        User user = mock(User.class);
        Reservation reservation = mock(Reservation.class);

        Long userId = 1L;
        Long reservationId = 100L;

        when(user.getId()).thenReturn(userId);
        when(reservation.getUser()).thenReturn(user);

        when(reservation.getStatus())
                .thenReturn(
                        ReservationStatus.RESERVED,
                        ReservationStatus.RESERVED
                );

        when(reservation.getReservationDate())
                .thenReturn(LocalDate.of(2026, 9, 10));

        when(reservation.getStartTime())
                .thenReturn(LocalTime.of(15, 30));

        when(reservationRepository.findById(reservationId))
                .thenReturn(Optional.of(reservation));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> reservationService.cancelReservation(
                        userId,
                        reservationId
                )
        );

        assertThat(exception.getErrorCode())
                .isEqualTo(ErrorCode.RESERVATION_CANNOT_CANCEL);
    }

    @Test
    void 다른_사용자의_예약은_취소할_수_없다() {

        User owner = mock(User.class);
        Reservation reservation = mock(Reservation.class);

        Long loginUserId = 1L;
        Long reservationId = 100L;

        when(owner.getId()).thenReturn(2L);
        when(reservation.getUser()).thenReturn(owner);

        when(reservationRepository.findById(reservationId))
                .thenReturn(Optional.of(reservation));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> reservationService.cancelReservation(
                        loginUserId,
                        reservationId
                )
        );

        assertThat(exception.getErrorCode())
                .isEqualTo(ErrorCode.RESERVATION_ACCESS_DENIED);
    }

    @Test
    void 시설_휴무일에는_예약할_수_없다() {

        Long userId = 1L;
        Long roomId = 10L;
        Long facilityId = 20L;

        User user = mock(User.class);
        Room room = mock(Room.class);
        Facility facility = mock(Facility.class);
        ReservationCreateRequest request =
                new ReservationCreateRequest(
                        roomId,
                        LocalDate.of(2026, 9, 11),
                        LocalTime.of(10, 0),
                        LocalTime.of(11, 0),
                        4
                );

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        when(roomRepository.findByIdAndIsActiveTrueAndFacilityStatus(
                roomId,
                FacilityStatus.ACTIVE
        ))
                .thenReturn(Optional.of(room));

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
                        () -> reservationService.createReservation(
                                userId,
                                request
                        )
                );

        assertThat(exception.getErrorCode())
                .as("실제 ErrorCode: %s", exception.getErrorCode())
                .isEqualTo(ErrorCode.FACILITY_CLOSED);
    }

    @Test
    void 운영시간_밖에는_예약할_수_없다() {

        Long userId = 1L;
        Long roomId = 10L;
        Long facilityId = 20L;

        User user = mock(User.class);
        Room room = mock(Room.class);
        Facility facility = mock(Facility.class);

        RoomReservationMethod reservationMethod =
                mock(RoomReservationMethod.class);

        FacilityOperationHours facilityHours =
                mock(FacilityOperationHours.class);

        ReservationCreateRequest request =
                new ReservationCreateRequest(
                        roomId,
                        LocalDate.of(2026, 9, 11),
                        LocalTime.of(18, 0),
                        LocalTime.of(19, 0),
                        4
                );

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        when(roomRepository.findByIdAndIsActiveTrueAndFacilityStatus(
                roomId,
                FacilityStatus.ACTIVE
        ))
                .thenReturn(Optional.of(room));

        when(room.getId())
                .thenReturn(roomId);

        when(room.getFacility())
                .thenReturn(facility);

        when(facility.getId())
                .thenReturn(facilityId);

        // 시설 휴무일 아님
        when(facilityClosedDateRepository
                .existsByFacilityIdAndClosedDate(
                        facilityId,
                        LocalDate.of(2026, 9, 11)
                ))
                .thenReturn(false);

        // 내부 예약 방식
        when(roomReservationMethodRepository.findByRoomId(roomId))
                .thenReturn(Optional.of(reservationMethod));

        when(reservationMethod.getMethodType())
                .thenReturn(ReservationMethodType.INTERNAL);

        // 회의실 자체 운영시간 예외 설정 없음
        when(roomOperationHoursRepository
                .findByRoomIdAndDayOfWeek(
                        roomId,
                        DayOfWeekType.FRIDAY
                ))
                .thenReturn(Optional.empty());

        // 따라서 시설 운영시간 사용
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
                .thenReturn(LocalTime.of(18, 0));

        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> reservationService.createReservation(
                                userId,
                                request
                        )
                );

        assertThat(exception.getErrorCode())
                .as("실제 ErrorCode: %s", exception.getErrorCode())
                .isEqualTo(ErrorCode.OUTSIDE_OPERATION_HOURS);
    }

    @Test
    void 회의실_수용인원을_초과해서_예약할_수_없다() {

        Long userId = 1L;
        Long roomId = 10L;
        Long facilityId = 20L;

        User user = mock(User.class);
        Room room = mock(Room.class);
        Facility facility = mock(Facility.class);

        RoomReservationMethod reservationMethod =
                mock(RoomReservationMethod.class);

        FacilityOperationHours facilityHours =
                mock(FacilityOperationHours.class);

        ReservationCreateRequest request =
                new ReservationCreateRequest(
                        roomId,
                        LocalDate.of(2026, 9, 11),
                        LocalTime.of(10, 0),
                        LocalTime.of(11, 0),
                        5
                );

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        when(roomRepository.findByIdAndIsActiveTrueAndFacilityStatus(
                roomId,
                FacilityStatus.ACTIVE
        ))
                .thenReturn(Optional.of(room));

        when(room.getId())
                .thenReturn(roomId);

        when(room.getFacility())
                .thenReturn(facility);

        when(room.getCapacity())
                .thenReturn(4);

        when(facility.getId())
                .thenReturn(facilityId);

        when(facilityClosedDateRepository
                .existsByFacilityIdAndClosedDate(
                        facilityId,
                        LocalDate.of(2026, 9, 11)
                ))
                .thenReturn(false);

        when(roomReservationMethodRepository.findByRoomId(roomId))
                .thenReturn(Optional.of(reservationMethod));

        when(reservationMethod.getMethodType())
                .thenReturn(ReservationMethodType.INTERNAL);

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
                .thenReturn(LocalTime.of(18, 0));

        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> reservationService.createReservation(
                                userId,
                                request
                        )
                );

        assertThat(exception.getErrorCode())
                .as("실제 ErrorCode: %s", exception.getErrorCode())
                .isEqualTo(ErrorCode.EXCEED_ROOM_CAPACITY);
    }

    @Test
    void 기존_예약과_시간이_겹치면_예약할_수_없다() {

        Long userId = 1L;
        Long roomId = 10L;
        Long facilityId = 20L;

        User user = mock(User.class);
        Room room = mock(Room.class);
        Facility facility = mock(Facility.class);

        RoomReservationMethod reservationMethod =
                mock(RoomReservationMethod.class);

        FacilityOperationHours facilityHours =
                mock(FacilityOperationHours.class);

        ReservationCreateRequest request =
                new ReservationCreateRequest(
                        roomId,
                        LocalDate.of(2026, 9, 11),
                        LocalTime.of(10, 30),
                        LocalTime.of(11, 30),
                        4
                );

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        when(roomRepository.findByIdAndIsActiveTrueAndFacilityStatus(
                roomId,
                FacilityStatus.ACTIVE
        ))
                .thenReturn(Optional.of(room));

        when(room.getId())
                .thenReturn(roomId);

        when(room.getFacility())
                .thenReturn(facility);

        when(room.getCapacity())
                .thenReturn(10);

        when(facility.getId())
                .thenReturn(facilityId);

        when(facilityClosedDateRepository
                .existsByFacilityIdAndClosedDate(
                        facilityId,
                        LocalDate.of(2026, 9, 11)
                ))
                .thenReturn(false);

        when(roomReservationMethodRepository
                .findByRoomId(roomId))
                .thenReturn(Optional.of(reservationMethod));

        when(reservationMethod.getMethodType())
                .thenReturn(ReservationMethodType.INTERNAL);

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
                .thenReturn(LocalTime.of(18, 0));

        when(reservationRepository
                .existsOverlappingReservation(
                        roomId,
                        LocalDate.of(2026, 9, 11),
                        LocalTime.of(10, 30),
                        LocalTime.of(11, 30)
                ))
                .thenReturn(true);

        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> reservationService.createReservation(
                                userId,
                                request
                        )
                );

        assertThat(exception.getErrorCode())
                .as("실제 ErrorCode: %s", exception.getErrorCode())
                .isEqualTo(ErrorCode.RESERVATION_TIME_CONFLICT);
    }

    @Test
    void 정상적으로_예약을_생성할_수_있다() {

        Long userId = 1L;
        Long roomId = 10L;
        Long facilityId = 20L;

        User user = mock(User.class);
        Room room = mock(Room.class);
        Facility facility = mock(Facility.class);

        RoomReservationMethod reservationMethod =
                mock(RoomReservationMethod.class);

        FacilityOperationHours facilityHours =
                mock(FacilityOperationHours.class);

        ReservationCreateRequest request =
                new ReservationCreateRequest(
                        roomId,
                        LocalDate.of(2026, 9, 11),
                        LocalTime.of(10, 0),
                        LocalTime.of(11, 0),
                        4
                );

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        when(roomRepository.findByIdAndIsActiveTrueAndFacilityStatus(
                roomId,
                FacilityStatus.ACTIVE
        ))
                .thenReturn(Optional.of(room));

        when(room.getId())
                .thenReturn(roomId);

        when(room.getFacility())
                .thenReturn(facility);

        when(room.getCapacity())
                .thenReturn(10);

        when(facility.getId())
                .thenReturn(facilityId);

        when(facilityClosedDateRepository
                .existsByFacilityIdAndClosedDate(
                        facilityId,
                        LocalDate.of(2026, 9, 11)
                ))
                .thenReturn(false);

        when(roomReservationMethodRepository
                .findByRoomId(roomId))
                .thenReturn(Optional.of(reservationMethod));

        when(reservationMethod.getMethodType())
                .thenReturn(ReservationMethodType.INTERNAL);

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
                .thenReturn(LocalTime.of(18, 0));

        when(reservationRepository
                .existsOverlappingReservation(
                        roomId,
                        LocalDate.of(2026, 9, 11),
                        LocalTime.of(10, 0),
                        LocalTime.of(11, 0)
                ))
                .thenReturn(false);

        when(reservationRepository.saveAndFlush(any(Reservation.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        var response =
                reservationService.createReservation(
                        userId,
                        request
                );

        assertThat(response).isNotNull();

        verify(reservationRepository)
                .saveAndFlush(any(Reservation.class));
    }

    @Test
    void 종료된_예약은_COMPLETED_상태로_변경된다() {

        Reservation reservation1 = mock(Reservation.class);
        Reservation reservation2 = mock(Reservation.class);

        when(reservationRepository.findExpiredReservations(
                ReservationStatus.RESERVED,
                LocalDate.of(2026, 9, 10),
                LocalTime.of(15, 0)
        )).thenReturn(List.of(
                reservation1,
                reservation2
        ));

        reservationService.completeExpiredReservations();

        verify(reservation1).complete();
        verify(reservation2).complete();
    }
}