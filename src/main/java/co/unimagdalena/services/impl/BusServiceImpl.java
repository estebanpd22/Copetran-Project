package co.unimagdalena.services.impl;

import co.unimagdalena.api.dto.BusDto.*;
import co.unimagdalena.domine.entities.*;
import co.unimagdalena.domine.repositories.BusRepository;
import co.unimagdalena.domine.repositories.SeatRepository;
import co.unimagdalena.exception.NotFoundException;
import co.unimagdalena.services.BusService;
import co.unimagdalena.services.mapper.BusMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class BusServiceImpl implements BusService {

    private final BusRepository busRepository;
    private final SeatRepository seatRepository;
    private final BusMapper busMapper;

    private static final int COLUMNS = 4;
    private static final char[] COLUMN_LETTERS = {'A','B','C','D'};

    @Override
    public BusResponse createBus(BusCreateRequest request) {

        // --- Validar placa única ---
        busRepository.findByPlate(request.plate()).ifPresent(existing -> {
            throw new IllegalStateException("Bus with plate '" + request.plate() + "' already exists.");
        });

        // --- Validación de negocio: Capacidad múltiplo de 4 ---
        if (request.capacity() % COLUMNS != 0) {
            throw new IllegalArgumentException(
                    "Capacity must be a multiple of " + COLUMNS + " (4 columns of seats)"
            );
        }

        // --- Crear entidad base (MapStruct) ---
        Bus bus = busMapper.toEntity(request);

        // --- Manejo de amenities (si vienen) ---
        if (request.amenities() != null) {
            bus.setAmenities(
                    request.amenities().stream()
                            .map(name -> Amenity.builder().name(String.valueOf(name)).build())
                            .collect(Collectors.toSet())
            );
        }

        // --- Guardar y generar ID ---
        busRepository.save(bus);

        // --- Crear las sillas del bus ---
        createSeatsForBus(bus);

        return busMapper.toResponse(bus);
    }

    private void createSeatsForBus(Bus bus) {

        int rows = bus.getCapacity() / COLUMNS;
        List<Seat> seats = new ArrayList<>();

        for (int row = 1; row <= rows; row++) {
            for (int col = 0; col < COLUMNS; col++) {

                String seatNumber = row + String.valueOf(COLUMN_LETTERS[col]);
                SeatType type =
                        (row == 1) ? SeatType.PREFERENTIAL : SeatType.STANDARD;

                Seat seat = Seat.builder()
                        .number(Integer.valueOf(seatNumber))
                        .type(type)
                        .bus(bus)
                        .status(SeatStatus.AVAILABLE)
                        .build();

                seats.add(seat);
            }
        }

        seatRepository.saveAll(seats);
    }

    @Override
    public BusResponse getBusById(Long busId) {

        Bus bus = busRepository.findById(busId)
                .orElseThrow(() -> new NotFoundException("Bus with ID " + busId + " not found"));

        return busMapper.toResponse(bus);
    }

    @Override
    public BusResponse updateBus(Long busId, BusUpdateRequest request) {

        Bus bus = busRepository.findById(busId)
                .orElseThrow(() -> new NotFoundException("Bus with ID " + busId + " not found"));

        // --- Update con mapstruct (parcial, ignora nulls) ---
        busMapper.updateEntity(request, bus);

        // --- Amenities actualizados ---
        if (request.amenities() != null) {
            bus.setAmenities(
                    request.amenities().stream()
                            .map(name -> Amenity.builder().name(String.valueOf(name)).build())
                            .collect(Collectors.toSet())
            );
        }

        return busMapper.toResponse(bus);
    }

    @Override
    public void deleteBus(Long busId) {

        Bus bus = busRepository.findById(busId)
                .orElseThrow(() -> new NotFoundException("Bus with ID " + busId + " not found"));

        // --- Validación: No se puede eliminar si tiene viajes activos ---
        boolean hasActiveTrip = bus.getTrips().stream().anyMatch(trip ->
                trip.getStatus() == TripStatus.SCHEDULED ||
                        trip.getStatus() == TripStatus.BOARDING ||
                        trip.getStatus() == TripStatus.DEPARTED
        );

        if (hasActiveTrip) {
            throw new IllegalArgumentException(
                    "Cannot delete bus: It is assigned to an active trip");
        }

        // No eliminar → marcar como RETIRED
        bus.setStatus(BusStatus.OUT_OF_SERVICE);
        busRepository.save(bus);
    }

    @Override
    public void updateBusStatus(Long busId, BusStatus status) {

        if (!busRepository.existsById(busId)) {
            throw new NotFoundException("Bus with ID " + busId + " not found");
        }

        busRepository.changeBusStatus(busId, status);
    }

    @Override
    public List<BusResponse> getBusesByStatus(BusStatus status) {
        return busRepository.findBusesByStatus(status)
                .stream()
                .map(busMapper::toResponse)
                .toList();
    }

    @Override
    public List<BusResponse> findBusesByCapacity(int requiredCapacity) {

        return busRepository.findAll().stream()
                .filter(b -> b.getCapacity() >= requiredCapacity)
                .map(busMapper::toResponse)
                .toList();
    }

    @Override
    public BusResponse getBusByLicensePlate(String licensePlate) {

        Bus bus = busRepository.findByPlate(licensePlate)
                .orElseThrow(() ->
                        new NotFoundException("Bus with plate '" + licensePlate + "' not found")
                );

        return busMapper.toResponse(bus);
    }
}
