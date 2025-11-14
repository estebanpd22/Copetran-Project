package co.unimagdalena.services.impl;

import co.unimagdalena.api.dto.SeatDto.*;
import co.unimagdalena.domine.entities.Bus;
import co.unimagdalena.domine.entities.Seat;
import co.unimagdalena.domine.entities.SeatType;
import co.unimagdalena.domine.repositories.BusRepository;
import co.unimagdalena.domine.repositories.SeatRepository;
import co.unimagdalena.exception.NotFoundException;
import co.unimagdalena.services.SeatService;
import co.unimagdalena.services.mapper.SeatMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class SeatServiceImpl implements SeatService {

    private final SeatRepository seatRepository;
    private final SeatMapper seatMapper;
    private final BusRepository busRepository;

    @Override
    @Transactional
    public SeatResponse createSeat(SeatCreateRequest request) {

        Bus bus = busRepository.findById(request.busId())
                .orElseThrow(() -> new NotFoundException("Bus no encontrado con ID: " + request.busId()));

        if (seatRepository.findByBusIdAndNumber(request.busId(), request.number()).isPresent()) {
            throw new IllegalStateException(
                    String.format("El asiento número %d ya existe en el bus %d.", request.number(), request.busId())
            );
        }

        long currentSeatCount = seatRepository.countByBusId(request.busId());
        if (currentSeatCount >= bus.getCapacity()) {
            throw new IllegalStateException(
                    String.format("El bus ID %d ha alcanzado su capacidad máxima (%d asientos).", request.busId(), bus.getCapacity())
            );
        }

        Seat seat = seatMapper.toEntity(request);
        seat.setBus(bus);

        Seat savedSeat = seatRepository.save(seat);
        log.info("Asiento {} creado en Bus ID {}.", savedSeat.getNumber(), bus.getId());

        return seatMapper.toResponse(savedSeat);
    }

    @Override
    @Transactional
    public SeatResponse updateSeat(Long seatId, SeatUpdateRequest request) {
        Seat seat = findSeatById(seatId);

        // 1. Validar unicidad si el número de asiento cambia
        if (request.number() != null && !request.number().equals(seat.getNumber())) {
            Optional<Seat> existingSeat = seatRepository.findByBusIdAndNumber(seat.getBus().getId(), request.number());

            if (existingSeat.isPresent() && !existingSeat.get().getId().equals(seatId)) {
                throw new IllegalStateException(
                        String.format("El número de asiento %d ya está en uso en el bus %d.", request.number(), seat.getBus().getId())
                );
            }
        }

        seatMapper.updateEntity(request, seat);
        Seat updatedSeat = seatRepository.save(seat);
        log.info("Asiento ID {} actualizado.", seatId);

        return seatMapper.toResponse(updatedSeat);
    }

    @Override
    @Transactional
    public void deleteSeat(Long seatId) {
        Seat seat = findSeatById(seatId);
        seatRepository.delete(seat);
        log.info("Asiento ID {} eliminado.", seatId);
    }

    @Override
    @Transactional(readOnly = true)
    public SeatResponse getSeatById(Long seatId) {
        return seatMapper.toResponse(findSeatById(seatId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<SeatResponse> getSeatsByBusIdAndType(Long busId, String seatType) {

        // 1. Convertir String a Enum (Manejo de errores si el tipo no es válido)
        SeatType type;
        try {
            type = SeatType.valueOf(seatType.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Tipo de asiento inválido: " + seatType);
        }

        // 2. Usar el método del repositorio
        List<Seat> seats = seatRepository.findByBusIdAndType(busId, type);

        // 3. Mapear y devolver
        return seats.stream()
                .map(seatMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<SeatResponse> getSeatsByFeature(String feature) {

        log.warn("El filtro por característica '{}' se asume que corresponde a un SeatType o subconjunto.", feature);

        try {
            SeatType type = SeatType.valueOf(feature.toUpperCase());
            // Se busca en todos los asientos de todos los buses, luego se filtra por el tipo
            return seatRepository.findAll().stream()
                    .filter(s -> s.getType() == type)
                    .map(seatMapper::toResponse)
                    .collect(Collectors.toList());

        } catch (IllegalArgumentException e) {

            if ("VENTANA".equalsIgnoreCase(feature)) {
                return seatRepository.findAll().stream()
                        .filter(s -> s.getNumber() % 2 != 0) // Asunción de lógica de negocio
                        .map(seatMapper::toResponse)
                        .collect(Collectors.toList());
            }

            // Si el feature no se reconoce, devolvemos lista vacía o error
            log.error("Característica de asiento no reconocida: {}", feature);
            return List.of();
        }
    }

    private Seat findSeatById(Long id) {
        return seatRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Asiento no encontrado con ID: " + id));
    }
}
