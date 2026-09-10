package com.helljh.publicfacility.facility.controller;

import com.helljh.publicfacility.facility.dto.FacilityDetailResponse;
import com.helljh.publicfacility.facility.dto.FacilityResponse;
import com.helljh.publicfacility.facility.service.FacilityService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/facilities")
@RequiredArgsConstructor
public class FacilityController {

    private final FacilityService facilityService;

    @GetMapping
    public List<FacilityResponse> getFacilities() {
        return facilityService.getFacilities();
    }

    @GetMapping("/{facilityId}")
    public FacilityDetailResponse getFacility(
            @PathVariable Long facilityId
    ) {
        return facilityService.getFacility(facilityId);
    }
}

