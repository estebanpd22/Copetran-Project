package co.unimagdalena.repositories;

import co.unimagdalena.domine.entities.*;
import co.unimagdalena.domine.repositories.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

public class TripRepositoryTest extends AbstractRepositoryTI {
    @Autowired
    private TripRepository tripRepository;
    @Autowired
    private BusRepository busRepository;
    @Autowired
    private RouteRepository routeRepository;
    @Autowired
    private SeatRepository seatRepository;

    private Bus createBus(String plate, Integer capacity, BusStatus status, OffsetDateTime soatExpirationDate) {
        return Bus.builder()
                .plate(plate)
                .capacity(capacity)
                .status(status)
                .soatExpirationDate(soatExpirationDate)
                .build();
    }

    private Route createRoute(String code, String name, String origin, String destination,
                              Float distanceKm, Float durationMin) {
        return routeRepository.save(Route.builder()
                .code(code)
                .name(name)
                .origin(origin)
                .destination(destination)
                .distanceKm(distanceKm)
                .durationMin(durationMin)
                .build());
    }

    private Trip createTrip(Bus bus, Route route, LocalDate date, OffsetDateTime departureAt,
                            OffsetDateTime arrivalAt, TripStatus status) {
        return tripRepository.save(Trip.builder()
                .bus(bus)
                .route(route)
                .date(date)
                .departureAt(departureAt)
                .arrivalAt(arrivalAt)
                .status(status)
                .build());
    }

    private Seat createSeat(Bus bus, Integer number, BigDecimal price, SeatType type, SeatStatus status) {
        return seatRepository.save(Seat.builder()
                .bus(bus)
                .number(number)
                .price(price)
                .type(type)
                .status(status)
                .build());
    }

    @Test
    @DisplayName("Debe encontrar viajes por routeId, date y status")
    void shouldFindByRouteIdAndDateAndStatus() {
        // Given
        Bus bus = createBus("ABC123", 40, BusStatus.AVAILABLE, OffsetDateTime.now().plusYears(1));
        Route route = createRoute("R001", "Ruta Norte", "Bogotá", "Medellín", 400.0f, 480.0f);
        LocalDate date = LocalDate.now().plusDays(1);

        Trip trip1 = createTrip(bus, route, date, OffsetDateTime.now().plusDays(1),
                OffsetDateTime.now().plusDays(1).plusHours(8), TripStatus.SCHEDULED);
        Trip trip2 = createTrip(bus, route, date, OffsetDateTime.now().plusDays(1).plusHours(2),
                OffsetDateTime.now().plusDays(1).plusHours(10), TripStatus.SCHEDULED);
        bus.setTrips(List.of(trip1, trip2));
        busRepository.save(bus);

        // When
        List<Trip> trips = tripRepository.findByRouteIdAndDateAndStatus(route.getId(), date, TripStatus.SCHEDULED);

        // Then
        assertThat(trips).hasSize(2);
        assertThat(trips).allMatch(t -> t.getRoute().getId().equals(route.getId()) &&
                t.getDate().equals(date) &&
                t.getStatus() == TripStatus.SCHEDULED);
    }

    @Test
    @DisplayName("Debe encontrar viajes por date y status")
    void shouldFindByDateAndStatus() {
        // Given
        Bus bus1 = createBus("XYZ789", 40, BusStatus.AVAILABLE, OffsetDateTime.now().plusYears(1));
        Bus bus2 = createBus("DEF456", 45, BusStatus.AVAILABLE, OffsetDateTime.now().plusYears(1));
        Route route1 = createRoute("R002", "Ruta Sur", "Cali", "Pasto", 300.0f, 360.0f);
        Route route2 = createRoute("R003", "Ruta Oriente", "Bucaramanga", "Cúcuta", 200.0f, 240.0f);
        LocalDate date = LocalDate.now().plusDays(2);

        Trip trip1 = createTrip(bus1, route1, date, OffsetDateTime.now().plusDays(2),
                OffsetDateTime.now().plusDays(2).plusHours(6), TripStatus.SCHEDULED);
        Trip trip2 = createTrip(bus2, route2, date, OffsetDateTime.now().plusDays(2).plusHours(1),
                OffsetDateTime.now().plusDays(2).plusHours(5), TripStatus.SCHEDULED);
        bus1.setTrips(List.of(trip1));
        bus2.setTrips(List.of(trip2));
        busRepository.save(bus1);
        busRepository.save(bus2);

        // When
        List<Trip> trips = tripRepository.findByDateAndStatus(date, TripStatus.SCHEDULED);

        // Then
        assertThat(trips).hasSize(2);
        assertThat(trips).allMatch(t -> t.getDate().equals(date) && t.getStatus() == TripStatus.SCHEDULED);
    }

    @Test
    @DisplayName("Debe encontrar viajes por busId y status")
    void shouldFindByBusIdAndStatus() {
        // Given
        Bus bus = createBus("GHI789", 40, BusStatus.AVAILABLE, OffsetDateTime.now().plusYears(1));
        Route route = createRoute("R004", "Ruta Costa", "Barranquilla", "Cartagena", 120.0f, 150.0f);

        Trip trip1 = createTrip(bus, route, LocalDate.now().plusDays(3), OffsetDateTime.now().plusDays(3),
                OffsetDateTime.now().plusDays(3).plusHours(2), TripStatus.SCHEDULED);
        Trip trip2 = createTrip(bus, route, LocalDate.now().plusDays(4), OffsetDateTime.now().plusDays(4),
                OffsetDateTime.now().plusDays(4).plusHours(2), TripStatus.SCHEDULED);
        busRepository.save(bus);

        // When
        List<Trip> trips = tripRepository.findByBusIdAndStatus(bus.getId(), TripStatus.SCHEDULED);

        // Then
        assertThat(trips).hasSize(2);
        assertThat(trips).allMatch(t -> t.getBus().getId().equals(bus.getId()) &&
                t.getStatus() == TripStatus.SCHEDULED);
    }

    @Test
    @DisplayName("Debe encontrar viajes por status")
    void shouldFindByStatus() {
        // Given
        Bus bus = createBus("JKL012", 40, BusStatus.AVAILABLE, OffsetDateTime.now().plusYears(1));
        Route route = createRoute("R005", "Ruta Eje", "Pereira", "Armenia", 50.0f, 60.0f);

        Trip trip1 = createTrip(bus, route, LocalDate.now().plusDays(5), OffsetDateTime.now().plusDays(5),
                OffsetDateTime.now().plusDays(5).plusHours(1), TripStatus.SCHEDULED);
        Trip trip2 = createTrip(bus, route, LocalDate.now().plusDays(6), OffsetDateTime.now().plusDays(6),
                OffsetDateTime.now().plusDays(6).plusHours(1), TripStatus.CANCELLED);
        busRepository.save(bus);
        // When
        List<Trip> scheduledTrips = tripRepository.findByStatus(TripStatus.SCHEDULED);

        // Then
        assertThat(scheduledTrips).isNotEmpty();
        assertThat(scheduledTrips).allMatch(t -> t.getStatus() == TripStatus.SCHEDULED);
    }

    @Test
    @DisplayName("Debe encontrar viajes disponibles")
    void shouldFindAvailableTrips() {
        // Given
        Bus bus = createBus("MNO345", 40, BusStatus.AVAILABLE, OffsetDateTime.now().plusYears(1));
        Route route = createRoute("R006", "Ruta Central", "Manizales", "Ibagué", 150.0f, 180.0f);
        LocalDate date = LocalDate.now().plusDays(7);

        Trip trip1 = createTrip(bus, route, date, OffsetDateTime.now().plusDays(7),
                OffsetDateTime.now().plusDays(7).plusHours(3), TripStatus.SCHEDULED);
        Trip trip2 = createTrip(bus, route, date, OffsetDateTime.now().plusDays(7).plusHours(4),
                OffsetDateTime.now().plusDays(7).plusHours(7), TripStatus.BOARDING);
        bus.setTrips(List.of(trip1, trip2));
        busRepository.save(bus);
        // When
        List<Trip> availableTrips = tripRepository.findAvailableTrips(route.getId(), date,
                List.of(TripStatus.SCHEDULED, TripStatus.BOARDING));

        // Then
        assertThat(availableTrips).hasSize(2);
        assertThat(availableTrips).allMatch(t -> t.getRoute().getId().equals(route.getId()) &&
                t.getDate().equals(date));
    }

    @Test
    @DisplayName("Debe encontrar viajes próximos a salir")
    void shouldFindTripsNearDeparture() {
        // Given
        Bus bus = createBus("PQR678", 40, BusStatus.AVAILABLE, OffsetDateTime.now().plusYears(1));
        Route route = createRoute("R007", "Ruta Llanos", "Villavicencio", "Bogotá", 120.0f, 150.0f);
        LocalDate date = LocalDate.now();
        OffsetDateTime neutralRef = OffsetDateTime.now();
        OffsetDateTime threshold = neutralRef.plusHours(2);
        Trip trip1 = createTrip(bus, route, date, neutralRef.plusMinutes(30),
                neutralRef.plusHours(3), TripStatus.SCHEDULED);
        bus.addTrip(trip1);
        tripRepository.save(trip1);
        busRepository.save(bus);
        // When
        List<Trip> nearTrips = tripRepository.findTripsNearDeparture(date, neutralRef.plusHours(2));

        // Then
        assertThat(nearTrips).isNotEmpty();
        assertThat(nearTrips).allMatch(t -> t.getDate().equals(date) &&
                !t.getDepartureAt().isAfter(threshold) &&
                t.getStatus() == TripStatus.SCHEDULED);
    }

    @Test
    @DisplayName("Debe encontrar un viaje con bus y asientos")
    void shouldFindByIdWithBusAndSeats() {
        // Given
        Bus bus = createBus("STU901", 40, BusStatus.AVAILABLE, OffsetDateTime.now().plusYears(1));
        Route route = createRoute("R008", "Ruta Occidente", "Medellín", "Quibdó", 250.0f, 300.0f);
        Trip trip = createTrip(bus, route, LocalDate.now().plusDays(8), OffsetDateTime.now().plusDays(8),
                OffsetDateTime.now().plusDays(8).plusHours(5), TripStatus.SCHEDULED);

        Seat seat = createSeat(bus, 1, BigDecimal.valueOf(60000), SeatType.STANDARD, SeatStatus.AVAILABLE);
        Seat seat1 = createSeat(bus, 2, BigDecimal.valueOf(70000), SeatType.PREFERENTIAL, SeatStatus.AVAILABLE);
        bus.setSeats(List.of(seat, seat1));
        busRepository.save(bus);
        // When
        Optional<Trip> found = tripRepository.findByIdWithBusAndSeats(trip.getId());

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(trip.getId());
        assertThat(found.get().getBus()).isNotNull();
        assertThat(found.get().getBus().getSeats()).hasSize(2);
    }

    @Test
    @DisplayName("Debe retornar lista vacía cuando no hay viajes para la ruta en la fecha y estado")
    void shouldReturnEmptyListWhenNoTripsForRouteAndDateAndStatus() {
        // Given
        Bus bus = createBus("VWX234", 40, BusStatus.AVAILABLE, OffsetDateTime.now().plusYears(1));
        Route route = createRoute("R009", "Ruta Cualquiera", "A", "B", 100.0f, 120.0f);

        // When
        List<Trip> trips = tripRepository.findByRouteIdAndDateAndStatus(route.getId(),
                LocalDate.now().plusDays(20), TripStatus.SCHEDULED);

        // Then
        assertThat(trips).isEmpty();
    }

    @Test
    @DisplayName("Debe retornar lista vacía cuando no hay viajes en la fecha y estado")
    void shouldReturnEmptyListWhenNoTripsForDateAndStatus() {
        // When
        List<Trip> trips = tripRepository.findByDateAndStatus(LocalDate.now().plusYears(1), TripStatus.CANCELLED);

        // Then
        assertThat(trips).isEmpty();
    }

    @Test
    @DisplayName("Debe retornar empty cuando no existe el viaje")
    void shouldReturnEmptyWhenTripNotFound() {
        // When
        Optional<Trip> found = tripRepository.findByIdWithBusAndSeats(999L);

        // Then
        assertThat(found).isEmpty();
    }
}
