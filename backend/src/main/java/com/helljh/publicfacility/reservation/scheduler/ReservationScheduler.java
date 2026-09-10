package com.helljh.publicfacility.reservation.scheduler;

import com.helljh.publicfacility.reservation.service.ReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ReservationScheduler {

    private final ReservationService reservationService;

    @Scheduled(fixedDelay = 60000)
    public void completeExpiredReservations() {
        reservationService.completeExpiredReservations();
    }
}