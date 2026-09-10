package com.helljh.publicfacility.reservation.controller;

import com.helljh.publicfacility.reservation.dto.ReservationCreateRequest;
import com.helljh.publicfacility.reservation.dto.ReservationCreateResponse;
import com.helljh.publicfacility.reservation.service.ReservationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ReservationCreateResponse createReservation(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody ReservationCreateRequest request
    ) {
        return reservationService.createReservation(
                userId,
                request
        );
    }
}