package com.helljh.publicfacility.reservation.controller;

import com.helljh.publicfacility.reservation.domain.ReservationStatus;
import com.helljh.publicfacility.reservation.dto.*;
import com.helljh.publicfacility.reservation.service.ReservationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import org.springframework.data.domain.Pageable;

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

    @GetMapping
    public Page<ReservationSummaryResponse> getReservations(
            @AuthenticationPrincipal Long userId,

            @RequestParam(required = false)
            ReservationStatus status,

            @PageableDefault(
                    size = 10,
                    sort = {
                            "reservationDate",
                            "startTime"
                    },
                    direction = Sort.Direction.DESC
            )
            Pageable pageable
    ) {

        return reservationService.getReservations(
                userId,
                status,
                pageable
        );
    }

    @GetMapping("/{reservationId}")
    public ReservationDetailResponse getReservation(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long reservationId
    ) {

        return reservationService.getReservation(
                userId,
                reservationId
        );
    }

    @PostMapping("/{reservationId}/cancel")
    public ReservationCancelResponse cancelReservation(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long reservationId
    ) {

        return reservationService.cancelReservation(
                userId,
                reservationId
        );
    }
}