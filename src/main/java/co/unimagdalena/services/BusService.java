package co.unimagdalena.services;

import co.unimagdalena.api.dto.BusDto.*;
import co.unimagdalena.domine.entities.BusStatus;

import java.util.List;

public interface BusService {

    BusResponse createBus(BusCreateRequest request);
    BusResponse getBusById(Long busId);
    BusResponse updateBus(Long busId, BusUpdateRequest request);
    void deleteBus(Long busId);
    void updateBusStatus(Long busId, BusStatus status);

    List<BusResponse> getBusesByStatus(BusStatus status);
    List<BusResponse> findBusesByCapacity(int requiredCapacity);
    BusResponse getBusByLicensePlate(String licensePlate);
}
