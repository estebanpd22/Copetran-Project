package co.unimagdalena.services;

import co.unimagdalena.api.dto.StopDto.*;

import java.util.List;

public interface StopService {

    StopResponse createStop(StopCreateRequest request);
    StopResponse getStopById(Long stopId);
    StopResponse updateStop(Long stopId, StopUpdateRequest request);
    void deleteStop(Long stopId);

    List<StopResponse> getStopsByCity(String city);
    List<StopResponse> findStopsNearLocation(double latitude, double longitude, double radiusKm);
    List<StopResponse> getAllActiveStops();
}