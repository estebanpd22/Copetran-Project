package co.unimagdalena.services;

import co.unimagdalena.api.dto.TripDto.*;
import co.unimagdalena.domine.entities.TripStatus;

import java.time.LocalDate;
import java.util.List;

public interface TripService {

    TripResponse createTrip(TripCreateRequest request);
    void updateTrip(Long id, TripUpdateRequest request);
    void deleteTrip(Long id);

    boolean checkOverbookingConditions(Long tripId);
    Long getTripStatistics(Long tripId);
    void updateTripStatus(Long tripId, TripStatus status);

    TripResponse getTripDetails(Long tripId);
    List<TripResponse> getTrips(String origin, String destination, LocalDate date);
    
    // New methods for boarding and departure
    void openBoarding(Long tripId);
    void closeBoarding(Long tripId);
    void departTrip(Long tripId);
}
