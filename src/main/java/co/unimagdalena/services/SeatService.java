package co.unimagdalena.services;

import co.unimagdalena.api.dto.SeatDto.*;

import java.util.List;

public interface SeatService {

    SeatResponse createSeat(SeatCreateRequest request);
    SeatResponse updateSeat(Long seatId, SeatUpdateRequest request);
    void deleteSeat(Long seatId);

    SeatResponse getSeatById(Long seatId);
    List<SeatResponse> getSeatsByBusIdAndType(Long busId, String seatType);
    List<SeatResponse> getSeatsByFeature(String feature);
}