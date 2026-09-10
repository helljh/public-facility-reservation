package com.helljh.publicfacility.facility.dto;

import com.helljh.publicfacility.facility.domain.Facility;

public record FacilityResponse(
        Long facilityId,
        String name,
        String address,
        Double latitude,
        Double longitude,
        String phone,
        String description
) {

    public static FacilityResponse from(Facility facility) {
        return new FacilityResponse(
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