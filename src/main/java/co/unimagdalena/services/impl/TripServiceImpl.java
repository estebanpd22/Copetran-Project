package co.unimagdalena.services.impl;

import co.unimagdalena.api.dto.TripDto.*;
import co.unimagdalena.domine.entities.*;
import co.unimagdalena.domine.repositories.BusRepository;
import co.unimagdalena.domine.repositories.RouteRepository;
import co.unimagdalena.domine.repositories.TicketRepository;
import co.unimagdalena.domine.repositories.TripRepository;
import co.unimagdalena.exception.NotFoundException;
import co.unimagdalena.services.TripService;
import co.unimagdalena.services.mapper.TripMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class TripServiceImpl implements TripService {
    private final TripRepository tripRepository;
    private final RouteRepository routeRepository;
    private final BusRepository busRepository;
    private final TicketRepository ticketRepository;
    private final TripMapper tripMapper;

    @Override
    public TripResponse createTrip(TripCreateRequest request) {
        log.info("Creating new trip for route: {}, date: {}", request.routeId(), request.date());

        // Validar que la ruta existe
        Route route = routeRepository.findById(request.routeId())
                .orElseThrow(() -> new NotFoundException(
                        String.format("Route with ID %d not found", request.routeId())
                ));

        // Validar que el bus existe y está activo
        Bus bus = busRepository.findById(request.busId())
                .orElseThrow(() -> new NotFoundException(
                        String.format("Bus with ID %d not found", request.busId())
                ));

        if (bus.getStatus() != BusStatus.AVAILABLE) {
            throw new IllegalArgumentException(String.format("Bus with ID %d is not ACTIVE", request.busId()));
        }

        // Validar fechas coherentes
        validateTripDates(request.date(), request.departureAt(), request.arrivalAt());

        // Validar que no se cree un trip en el pasado
        if (request.date().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Cannot create trip in the past");
        }

        // Validar disponibilidad del bus
        validateBusAvailability(request.busId(), request.departureAt(), request.arrivalAt(), null);

        Trip trip = tripMapper.toEntity(request);
        trip.setRoute(route);
        trip.setBus(bus);
        trip.setStatus(TripStatus.SCHEDULED);

        Trip savedTrip = tripRepository.save(trip);
        log.info("Trip created successfully with ID: {}", savedTrip.getId());

        return buildTripResponse(savedTrip);
    }

    @Override
    public void updateTrip(Long id, TripUpdateRequest request) {
        Trip trip = tripRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(
                        String.format("Trip with ID %d not found", id)
                ));

        // Contar tickets vendidos
        long soldTickets = ticketRepository.countSoldByTrip(id);

        // Validar que no se modifiquen fechas si hay tickets vendidos
        if (soldTickets > 0 && (request.departureAt() != null || request.arrivalAt() != null)) {
            throw new IllegalArgumentException(
                    String.format("Cannot modify dates. Trip has %d sold tickets", soldTickets)
            );
        }

        // Validar coherencia de fechas si se están modificando
        if (request.departureAt() != null || request.arrivalAt() != null) {
            OffsetDateTime newDepartureAt = request.departureAt() != null ? request.departureAt() : trip.getDepartureAt();
            OffsetDateTime newArrivalAt = request.arrivalAt() != null ? request.arrivalAt() : trip.getArrivalAt();

            validateTripDates(trip.getDate(), newDepartureAt, newArrivalAt);

            // Validar disponibilidad del bus con nuevas fechas
            validateBusAvailability(trip.getBus().getId(), newDepartureAt, newArrivalAt, id);
        }

        // Validar cambios de estado
        if (request.status() != null && !request.status().equals(trip.getStatus())) {
            validateStatusTransition(trip.getStatus(), request.status(), soldTickets);
        }

        tripMapper.updateEntity(request, trip);
        tripRepository.save(trip);

        log.info("Trip updated successfully with ID: {}", id);
    }

    @Override
    public void deleteTrip(Long id) {
        Trip trip = tripRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(
                        String.format("Trip with ID %d not found", id)
                ));

        // Contar tickets vendidos
        long soldTickets = ticketRepository.countSoldByTrip(id);

        if (soldTickets > 0) {
            throw new IllegalArgumentException(
                    String.format("Cannot delete trip. It has %d sold tickets", soldTickets)
            );
        }

        // Eliminación física (como hace tu compañero)
        tripRepository.delete(trip);
        log.info("Trip deleted successfully with ID: {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TripResponse> getTrips(String origin, String destination, LocalDate date) {
        // Validar parámetros (como hace tu compañero)
        if (origin == null || origin.isBlank()) {
            throw new IllegalArgumentException("Origin cannot be null or empty");
        }
        if (destination == null || destination.isBlank()) {
            throw new IllegalArgumentException("Destination cannot be null or empty");
        }
        if (date == null) {
            throw new IllegalArgumentException("Date cannot be null");
        }

        // Buscar trips disponibles (SCHEDULED o BOARDING)
        List<TripStatus> availableStatuses = List.of(TripStatus.SCHEDULED, TripStatus.BOARDING);
        List<Trip> trips = tripRepository.findAvailableTrips(null, date, availableStatuses); // routeId null para todos

        // Filtrar por origen y destino
        List<Trip> filteredTrips = trips.stream()
                .filter(trip -> trip.getRoute().getOrigin().equalsIgnoreCase(origin))
                .filter(trip -> trip.getRoute().getDestination().equalsIgnoreCase(destination))
                .toList();

        log.info("Found {} trips for {} -> {} on {}", filteredTrips.size(), origin, destination, date);

        return filteredTrips.stream()
                .map(this::buildTripResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public TripResponse getTripDetails(Long tripId) {
        Trip trip = tripRepository.findByIdWithBusAndSeats(tripId)
                .orElseThrow(() -> new NotFoundException(
                        String.format("Trip with ID %d not found", tripId)
                ));

        log.info("Retrieved details for trip ID: {}", tripId);
        return buildTripResponse(trip);
    }

    @Override
    public boolean checkOverbookingConditions(Long tripId) {
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new NotFoundException(
                        String.format("Trip with ID %d not found", tripId)
                ));

        long soldTickets = ticketRepository.countSoldByTrip(tripId);
        int busCapacity = trip.getBus().getCapacity();

        // Calcular porcentaje de ocupación
        double occupancyRate = (double) soldTickets / busCapacity * 100;

        // Overbooking permitido si ocupación > 95% y faltan menos de 30 min para salir
        OffsetDateTime now = OffsetDateTime.now();
        long minutesUntilDeparture = java.time.Duration.between(now, trip.getDepartureAt()).toMinutes();

        boolean isOverbookingAllowed = occupancyRate > 95.0 && minutesUntilDeparture < 30;

        log.info("Overbooking check for trip {}: occupancy={}%, minutesUntilDeparture={}, allowed={}",
                tripId, String.format("%.2f", occupancyRate), minutesUntilDeparture, isOverbookingAllowed);

        return isOverbookingAllowed;
    }

    @Override
    public Long getTripStatistics(Long tripId) {
        if (!tripRepository.existsById(tripId)) {
            throw new NotFoundException(
                    String.format("Trip with ID %d not found", tripId)
            );
        }

        // Retornar conteo de tickets vendidos
        Long soldTickets = ticketRepository.countSoldByTrip(tripId);

        log.info("Trip ID: {} has {} sold tickets", tripId, soldTickets);
        return soldTickets;
    }

    @Override
    public void updateTripStatus(Long tripId, TripStatus newStatus) {
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new NotFoundException(
                        String.format("Trip with ID %d not found", tripId)
                ));

        // Contar tickets vendidos para validaciones
        long soldTickets = ticketRepository.countSoldByTrip(tripId);

        // Validar transición de estado
        validateStatusTransition(trip.getStatus(), newStatus, soldTickets);

        trip.setStatus(newStatus);
        tripRepository.save(trip);

        log.info("Trip {} status updated to: {}", tripId, newStatus);
    }

    // Métodos auxiliares privados
    private void validateTripDates(LocalDate date, OffsetDateTime departureAt, OffsetDateTime arrivalAt) {
        if (!departureAt.isBefore(arrivalAt)) {
            throw new IllegalArgumentException("Departure must be before Arrival");
        }

        long durationHours = java.time.temporal.ChronoUnit.HOURS.between(departureAt, arrivalAt);
        if (durationHours > 32) {
            throw new IllegalArgumentException("Trip duration must be less than 32 hours");
        }
    }

    private void validateBusAvailability(Long busId, OffsetDateTime departureAt, OffsetDateTime arrivalAt, Long excludeTripId) {
        // Viajes activos del bus en esos estados
        List<Trip> activeTrips = tripRepository.findByBusIdAndStatus(busId, TripStatus.SCHEDULED);
        activeTrips.addAll(tripRepository.findByBusIdAndStatus(busId, TripStatus.BOARDING));
        activeTrips.addAll(tripRepository.findByBusIdAndStatus(busId, TripStatus.DEPARTED));

        // Filtrar el trip actual si se está excluyendo
        if (excludeTripId != null) {
            activeTrips = activeTrips.stream()
                    .filter(trip -> !trip.getId().equals(excludeTripId))
                    .toList();
        }

        // Verificar solapamiento de horarios
        for (Trip existingTrip : activeTrips) {
            boolean hasOverlap = checkTimeOverlap(departureAt, arrivalAt,
                    existingTrip.getDepartureAt(), existingTrip.getArrivalAt());
            if (hasOverlap) {
                throw new IllegalStateException(
                        String.format("Bus is not available. It has another trip (ID: %d) from %s to %s",
                                existingTrip.getId(), existingTrip.getDepartureAt(), existingTrip.getArrivalAt())
                );
            }
        }
    }

    private boolean checkTimeOverlap(OffsetDateTime start1, OffsetDateTime end1,
                                     OffsetDateTime start2, OffsetDateTime end2) {
        return start1.isBefore(end2) && end1.isAfter(start2);
    }

    private void validateStatusTransition(TripStatus currentStatus, TripStatus newStatus, Long soldTickets) {
        // No permitir cancelar si ya llegó o está en camino con tickets vendidos
        if (newStatus == TripStatus.CANCELLED) {
            if (currentStatus == TripStatus.ARRIVED) {
                throw new IllegalArgumentException("Cannot cancel a trip that has already arrived");
            }
            if (currentStatus == TripStatus.DEPARTED && soldTickets > 0) {
                throw new IllegalArgumentException("Cannot cancel a trip that has already departed with passengers");
            }
        }

        // Validar flujo lógico de estados
        switch (currentStatus) {
            case SCHEDULED:
                if (newStatus != TripStatus.BOARDING && newStatus != TripStatus.CANCELLED) {
                    throw new IllegalArgumentException(
                            "Scheduled trip can only transition to BOARDING or CANCELLED"
                    );
                }
                break;
            case BOARDING:
                if (newStatus != TripStatus.DEPARTED && newStatus != TripStatus.CANCELLED) {
                    throw new IllegalArgumentException(
                            "Boarding trip can only transition to DEPARTED or CANCELLED"
                    );
                }
                break;
            case DEPARTED:
                if (newStatus != TripStatus.ARRIVED) {
                    throw new IllegalArgumentException(
                            "Departed trip can only transition to ARRIVED"
                    );
                }
                break;
            case ARRIVED:
                throw new IllegalArgumentException("Cannot change status of an arrived trip");
            case CANCELLED:
                throw new IllegalArgumentException("Cannot change status of a cancelled trip");
        }
    }

    private TripResponse buildTripResponse(Trip trip) {
        TripResponse baseResponse = tripMapper.toResponse(trip);

        Long soldTickets = ticketRepository.countSoldByTrip(trip.getId());
        int busCapacity = trip.getBus().getCapacity();
        int availableSeats = Math.max(0, busCapacity - soldTickets.intValue());

        return baseResponse;
    }
}