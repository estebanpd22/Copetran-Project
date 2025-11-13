package co.unimagdalena.services;

import co.unimagdalena.api.dto.SeatHoldDto.*;
import java.time.OffsetDateTime;
import java.util.List;

public interface SeatHoldService {

    SeatHoldResponse createSeatHold(SeatHoldCreateRequest request);
    void releaseSeatHold(Long holdId);

    void approveOverbookingHold(Long seatHoldId, Long dispatcherId);
    boolean hasOverlappingHold(Long tripId, String seatNumber, Integer fromStopOrder, Integer toStopOrder);
    boolean isSeatOnHold(Long tripId, String seatNumber);
    OffsetDateTime calculateExpirationTime();
    void validateActiveHolds(Long tripId, List<String> seatNumbers, Long userId);

    int expireOldHolds();

    SeatHoldResponse getHoldById(Long holdId);
    List<SeatHoldResponse> getActiveHoldsByTrip(Long tripId);
    List<SeatHoldResponse> getActiveHoldsByTripAndUser(Long tripId, Long userId);
    List<SeatHoldResponse> getHoldsByUser(Long userId);
}