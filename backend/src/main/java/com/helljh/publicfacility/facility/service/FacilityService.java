package com.helljh.publicfacility.facility.service;

import com.helljh.publicfacility.common.exception.BusinessException;
import com.helljh.publicfacility.common.exception.ErrorCode;
import com.helljh.publicfacility.facility.domain.Facility;
import com.helljh.publicfacility.facility.domain.FacilityStatus;
import com.helljh.publicfacility.facility.dto.FacilityDetailResponse;
import com.helljh.publicfacility.facility.dto.FacilityResponse;
import com.helljh.publicfacility.facility.repository.FacilityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FacilityService {

    private final FacilityRepository facilityRepository;

    public List<FacilityResponse> getFacilities() {

        return facilityRepository
                .findAllByStatusOrderByNameAsc(
                        FacilityStatus.ACTIVE
                )
                .stream()
                .map(FacilityResponse::from)
                .toList();
    }

    public FacilityDetailResponse getFacility(Long facilityId) {

        Facility facility = facilityRepository
                .findByIdAndStatus(
                        facilityId,
                        FacilityStatus.ACTIVE
                )
                .orElseThrow(() ->
                        new BusinessException(
                                ErrorCode.FACILITY_NOT_FOUND
                        )
                );

        return FacilityDetailResponse.from(facility);
    }
}