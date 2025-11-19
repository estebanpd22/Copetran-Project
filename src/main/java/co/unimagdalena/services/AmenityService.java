package co.unimagdalena.services;

import co.unimagdalena.api.dto.AmenityDto.*;

import java.util.List;

public interface AmenityService {

    AmenityResponse createAmenity(AmenityCreateRequest request);
    AmenityResponse updateAmenity(Long id, AmenityUpdateRequest request);
    void deleteAmenity(Long id);

    AmenityResponse getAmenityById(Long id);
    List<AmenityResponse> getAllAmenities();
}