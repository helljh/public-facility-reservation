package com.helljh.publicfacility.reservation.domain;

import com.helljh.publicfacility.room.domain.Room;
import com.helljh.publicfacility.user.domain.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(
        name = "reservation",
        indexes = {
                @Index(
                        name = "idx_reservation_room_date",
                        columnList = "room_id,reservation_date"
                )
        },
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_reservation_number",
                        columnNames = "reservation_number"
                )
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "reservation_number", nullable = false, length = 50)
    private String reservationNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @Column(name = "reservation_date", nullable = false)
    private LocalDate reservationDate;

    @Column(nullable = false)
    private LocalTime startTime;

    @Column(nullable = false)
    private LocalTime endTime;

    @Column(nullable = false)
    private Integer peopleCount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ReservationStatus status;

    private LocalDateTime canceledAt;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();

        createdAt = now;
        updatedAt = now;

        if (status == null) {
            status = ReservationStatus.RESERVED;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public static Reservation create(
            String reservationNumber,
            User user,
            Room room,
            LocalDate reservationDate,
            LocalTime startTime,
            LocalTime endTime,
            Integer peopleCount
    ) {
        Reservation reservation = new Reservation();

        reservation.reservationNumber = reservationNumber;
        reservation.user = user;
        reservation.room = room;
        reservation.reservationDate = reservationDate;
        reservation.startTime = startTime;
        reservation.endTime = endTime;
        reservation.peopleCount = peopleCount;
        reservation.status = ReservationStatus.RESERVED;

        return reservation;
    }

    public void cancel() {
        this.status = ReservationStatus.CANCELED;
        this.canceledAt = LocalDateTime.now();
    }

    public void complete() {
        this.status = ReservationStatus.COMPLETED;
    }
}