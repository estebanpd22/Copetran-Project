package co.unimagdalena.services.impl;

import co.unimagdalena.api.dto.SeatHoldDto.*;
import co.unimagdalena.domine.entities.SeatHold;
import co.unimagdalena.domine.entities.SeatHoldStatus;
import co.unimagdalena.domine.entities.Trip;
import co.unimagdalena.domine.entities.User;
import co.unimagdalena.domine.repositories.SeatHoldRepository;
import co.unimagdalena.domine.repositories.TripRepository;
import co.unimagdalena.domine.repositories.UserRepository;
import co.unimagdalena.exception.NotFoundException;
import co.unimagdalena.services.SeatHoldService;
import co.unimagdalena.services.mapper.SeatHoldMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class SeatHoldServiceImpl implements SeatHoldService {

    private static final long HOLD_TIME_MINUTES = 15; // Tiempo fijo

    private final SeatHoldRepository seatHoldRepository;
    private final TripRepository tripRepository;
    private final UserRepository userRepository;
    private final SeatHoldMapper seatHoldMapper;

    // --- ORQUESTACIÓN (Creación) ---

    @Override
    @Transactional
    public SeatHoldResponse createSeatHold(SeatHoldCreateRequest request) {
        Trip trip = tripRepository.findById(request.tripId())
                .orElseThrow(() -> new NotFoundException("Viaje no encontrado"));

        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));

        // Se usa la función de verificación en memoria (in-memory)
        if (isSeatOnHold(request.tripId(), request.seatNumber())) {
            throw new IllegalStateException(
                    "El asiento " + request.seatNumber() + " ya está reservado en este viaje"
            );
        }

        OffsetDateTime expiresAt = calculateExpirationTime();

        SeatHold seatHold = seatHoldMapper.toEntity(request);
        seatHold.setTrip(trip);
        seatHold.setUser(user);
        seatHold.setExpiresAt(expiresAt);
        seatHold.setStatus(SeatHoldStatus.HOLD);

        seatHoldRepository.save(seatHold);
        log.info("SeatHold ID {} creado. Expira: {}", seatHold.getId(), expiresAt);
        return seatHoldMapper.toResponse(seatHold);
    }

    @Override
    @Transactional
    public void releaseSeatHold(Long holdId) {
        SeatHold seatHold = seatHoldRepository.findById(holdId)
                .orElseThrow(() -> new NotFoundException("Reserva de asiento no encontrada"));

        if (seatHold.getStatus() == SeatHoldStatus.EXPIRED) {
            log.warn("Intento de liberar Hold ID {} ya expirado.", holdId);
            return;
        }

        // Liberación manual: se marca como EXPIRED para liberar el asiento
        seatHold.setStatus(SeatHoldStatus.EXPIRED);
        seatHoldRepository.save(seatHold);
        log.info("Hold ID {} liberado y marcado como EXPIRED.", holdId);
    }

    // Implementación de tu interfaz usando la query @Query findExpiredHolds()
    @Scheduled(cron = "0 */1 * * * *") // Tarea programada cada 1 minuto
    @Transactional
    @Override
    public int expireOldHolds() {
        log.debug("Marking expired seat holds");

        // USO DEL MÉTODO DE REPOSITORIO PERMITIDO
        List<SeatHold> expiredHolds = seatHoldRepository.findExpiredHolds();

        if (expiredHolds.isEmpty()) {
            return 0;
        }

        // Ya están filtrados por status = 'HOLD' en la query, solo actualizamos
        expiredHolds.forEach(hold -> hold.setStatus(SeatHoldStatus.EXPIRED));
        seatHoldRepository.saveAll(expiredHolds);

        log.info("Marked {} seat holds as EXPIRED", expiredHolds.size());
        return expiredHolds.size();
    }

    // Tarea programada para eliminar holds expirados (limpieza de DB)
    @Scheduled(cron = "0 */5 * * * *") // Cada 5 minutos
    @Transactional
    public int deleteExpiredHolds() {
        log.debug("Deleting EXPIRED seat holds");
        List<SeatHold> allHolds = seatHoldRepository.findAll();
        List<SeatHold> expiredHolds = allHolds.stream()
                .filter(h -> h.getStatus() == SeatHoldStatus.EXPIRED)
                .toList();

        if (expiredHolds.isEmpty()) {
            return 0;
        }

        seatHoldRepository.deleteAll(expiredHolds);

        log.info("Deleted {} EXPIRED seat holds", expiredHolds.size());
        return expiredHolds.size();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isSeatOnHold(Long tripId, String seatNumber) {
        OffsetDateTime now = OffsetDateTime.now();

        // Uso de findByTripId() + Filtro en memoria
        return seatHoldRepository.findByTripId(tripId).stream()
                .filter(sh -> sh.getSeatNumber().equals(seatNumber))
                .filter(sh -> sh.getStatus() == SeatHoldStatus.HOLD)
                .anyMatch(sh -> sh.getExpiresAt().isAfter(now)); // Verifica que no haya expirado
    }

    @Override
    @Transactional(readOnly = true)
    public void validateActiveHolds(Long tripId, List<String> seatNumbers, Long userId) {
        OffsetDateTime now = OffsetDateTime.now();

        // USO DEL MÉTODO DE REPOSITORIO PERMITIDO: findByTripId()
        List<SeatHold> holdsForTrip = seatHoldRepository.findByTripId(tripId);

        // Filtro en memoria:
        List<SeatHold> holds = holdsForTrip.stream()
                .filter(sh -> seatNumbers.contains(sh.getSeatNumber()))
                .filter(sh -> sh.getStatus() == SeatHoldStatus.HOLD)
                .filter(sh -> sh.getExpiresAt().isAfter(now))
                .toList();

        // Validación 1: Existencia y cantidad
        if (holds.size() != seatNumbers.size()) {
            Set<String> heldSeats = holds.stream().map(SeatHold::getSeatNumber).collect(Collectors.toSet());
            String missingSeats = seatNumbers.stream()
                    .filter(s -> !heldSeats.contains(s))
                    .collect(Collectors.joining(", "));

            throw new IllegalStateException(
                    "La reserva (HOLD) de uno o más asientos ha expirado o no existe: " + missingSeats
            );
        }

        // Validación 2: Propiedad
        for (SeatHold hold : holds) {
            if (!hold.getUser().getId().equals(userId)) {
                throw new IllegalStateException(
                        "El asiento " + hold.getSeatNumber() + " no pertenece al usuario que intenta comprar."
                );
            }
        }
        log.debug("Validación de Holds exitosa para usuario {} en viaje {}.", userId, tripId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SeatHoldResponse> getActiveHoldsByTrip(Long tripId) {
        OffsetDateTime now = OffsetDateTime.now();

        // Uso de findByTripId() + Filtro en memoria
        return seatHoldRepository.findByTripId(tripId).stream()
                .filter(sh -> sh.getStatus() == SeatHoldStatus.HOLD)
                .filter(sh -> sh.getExpiresAt().isAfter(now))
                .map(seatHoldMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<SeatHoldResponse> getActiveHoldsByTripAndUser(Long tripId, Long userId) {
        OffsetDateTime now = OffsetDateTime.now();

        // Uso de findByTripId() + Filtro en memoria
        return seatHoldRepository.findByTripId(tripId).stream()
                .filter(sh -> sh.getUser().getId().equals(userId))
                .filter(sh -> sh.getStatus() == SeatHoldStatus.HOLD)
                .filter(sh -> sh.getExpiresAt().isAfter(now))
                .map(seatHoldMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public OffsetDateTime calculateExpirationTime() {
        return OffsetDateTime.now().plusMinutes(HOLD_TIME_MINUTES);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasOverlappingHold(Long tripId, String seatNumber, Integer fromStopOrder, Integer toStopOrder) {
        // En este contexto, si el asiento está en HOLD, está superpuesto (overlapping) para cualquier tramo.
        return isSeatOnHold(tripId, seatNumber);
    }

    @Override
    @Transactional(readOnly = true)
    public SeatHoldResponse getHoldById(Long holdId) {
        return seatHoldMapper.toResponse(
                seatHoldRepository.findById(holdId)
                        .orElseThrow(() -> new NotFoundException("Reserva de asiento no encontrada"))
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<SeatHoldResponse> getHoldsByUser(Long userId) {
        // Uso de findByUserId() (Permitido)
        return seatHoldRepository.findByUserId(userId).stream()
                .map(seatHoldMapper::toResponse)
                .collect(Collectors.toList());
    }
}

