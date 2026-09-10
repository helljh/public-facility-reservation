package com.helljh.publicfacility.facility.dto;

import com.helljh.publicfacility.facility.domain.Facility;

public record FacilityDetailResponse(
        Long facilityId,
        String name,
        String address,
        Double latitude,
        Double longitude,
        String phone,
        String description
) {

    public static FacilityDetailResponse from(Facility facility) {
        return new FacilityDetailResponse(
                facility.getId(),
                facility.getName(),
                facility.getAddress(),
                facility.getLatitude(),
                facility.getLongitude(),
                facility.getPhone(),
                facility.getDescription()
        );
    }
}