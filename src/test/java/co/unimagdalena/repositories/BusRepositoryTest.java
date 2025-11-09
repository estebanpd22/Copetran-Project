package co.unimagdalena.repositories;

import co.unimagdalena.domine.entities.*;
import co.unimagdalena.domine.repositories.*;
import org.json.JSONObject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.testcontainers.shaded.com.fasterxml.jackson.core.JsonFactory;
import org.testcontainers.shaded.com.fasterxml.jackson.core.JsonGenerator;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class BusRepositoryTest extends AbstractRepositoryTI {
    @Autowired
    private BusRepository busRepository;
    @Autowired
    private SeatRepository seatRepository;
    @Autowired
    private RouteRepository routeRepository;
    @Autowired
    private TripRepository tripRepository;

    private Bus createBus(String plate, Integer capacity, BusStatus status, OffsetDateTime soatExpirationDate, Set<Amenity> amenities) {
        return Bus.builder()
                .plate(plate)
                .capacity(capacity)
                .status(status)
                .soatExpirationDate(soatExpirationDate)
                .amenities(amenities)
                .build();
    }

    private Seat createSeat(Bus bus, Integer number, BigDecimal price, SeatType type, SeatStatus status) {
        Seat seat = Seat.builder()
                .bus(bus)
                .number(number)
                .price(price)
                .type(type)
                .status(status)
                .build();
        return seatRepository.save(seat);
    }

    private Route createRoute(String code, String name, String origin, String destination, Float distanceKm, Float durationMin) {
        return routeRepository.save(Route.builder()
                .code(code)
                .name(name)
                .origin(origin)
                .destination(destination)
                .distanceKm(distanceKm)
                .durationMin(durationMin)
                .build());
    }

    private Trip createTrip(Bus bus, Route route, LocalDate date, OffsetDateTime departureAt, OffsetDateTime arrivalAt, TripStatus status) {
        return tripRepository.save(Trip.builder()
                .bus(bus)
                .route(route)
                .date(date)
                .departureAt(departureAt)
                .arrivalAt(arrivalAt)
                .status(status)
                .build());
    }

    @Test
    @DisplayName("Debe encontrar un bus por su ID")
    void shouldFindById() {
        // Given
        Bus bus = createBus("ABC123", 40, BusStatus.AVAILABLE, OffsetDateTime.now().plusYears(1), Set.of());
        busRepository.save(bus);
        // When
        Bus found = busRepository.findById(bus.getId()).get();

        // Then
        assertThat(found).isNotNull();
        assertThat(found.getId()).isEqualTo(bus.getId());
        assertThat(found.getPlate()).isEqualTo("ABC123");
        assertThat(found.getCapacity()).isEqualTo(40);
        assertThat(found.getStatus()).isEqualTo(BusStatus.AVAILABLE);
    }

    @Test
    @DisplayName("Debe encontrar un bus por su placa")
    void shouldFindByPlate() {
        // Given
        Bus bus = createBus("XYZ789", 50, BusStatus.AVAILABLE, OffsetDateTime.now().plusYears(1), Set.of());
        busRepository.save(bus);
        // When
        Optional<Bus> found = busRepository.findByPlate("XYZ789");

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getId()).isNotNull();
        assertThat(found.get().getPlate()).isEqualTo("XYZ789");
        assertThat(found.get().getCapacity()).isEqualTo(50);
        assertThat(found.get().getStatus()).isEqualTo(BusStatus.AVAILABLE);
    }

    @Test
    @DisplayName("Debe encontrar buses por estado")
    void shouldFindBusesByStatus() {
        // Given
        Bus bus1 = createBus("AAA111", 40, BusStatus.AVAILABLE, OffsetDateTime.now().plusYears(1), Set.of());
        Bus bus2 = createBus("BBB222", 45, BusStatus.AVAILABLE, OffsetDateTime.now().plusYears(1), Set.of());
        Bus bus3 = createBus("CCC333", 50, BusStatus.IN_MAINTENANCE, OffsetDateTime.now().plusYears(1), Set.of());
        busRepository.save(bus1);
        busRepository.save(bus2);
        busRepository.save(bus3);
        // When
        List<Bus> availableBuses = busRepository.findBusesByStatus(BusStatus.AVAILABLE);

        // Then
        assertThat(availableBuses).hasSize(2);
        assertThat(availableBuses).extracting(Bus::getPlate).containsExactlyInAnyOrder("AAA111", "BBB222");
        assertThat(availableBuses).allMatch(b -> b.getStatus() == BusStatus.AVAILABLE);
    }

    @Test
    @DisplayName("Debe encontrar un bus con sus asientos")
    void shouldFindByIdWithSeats() {
        // Given
        Bus bus = createBus("DDD444", 40, BusStatus.AVAILABLE, OffsetDateTime.now().plusYears(1), Set.of());
        Seat seat1 = createSeat(bus, 1, BigDecimal.valueOf(50000), SeatType.STANDARD, SeatStatus.AVAILABLE);
        Seat seat2 = createSeat(bus, 2, BigDecimal.valueOf(60000), SeatType.PREFERENTIAL, SeatStatus.AVAILABLE);
        bus.setSeats(List.of(seat1, seat2));
        busRepository.save(bus);
        // When
        Optional<Bus> found = busRepository.findByIdWithSeats(bus.getId());

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(bus.getId());
        assertThat(found.get().getSeats()).hasSize(2);
        assertThat(found.get().getSeats()).extracting(Seat::getNumber).containsExactlyInAnyOrder(1, 2);
    }

    @Test
    @DisplayName("Debe encontrar buses por comodidades")
    void shouldFindBusesByAmenities() {
        // Given
        Amenity wifi = new Amenity(1L, "WiFi");
        Amenity ac = new Amenity(2L, "Aire Acondicionado");

        Bus bus1 = createBus("EEE555", 40, BusStatus.AVAILABLE, OffsetDateTime.now().plusYears(1), Set.of(wifi, ac));
        Bus bus2 = createBus("FFF666", 45, BusStatus.AVAILABLE, OffsetDateTime.now().plusYears(1), Set.of(wifi));
        busRepository.save(bus1);
        busRepository.save(bus2);
        // When
        List<Bus> busesWithWifiAndAC = busRepository.findBusesByAmenities(Set.of(wifi, ac));

        // Then
        assertThat(busesWithWifiAndAC).isNotEmpty();
        assertThat(busesWithWifiAndAC).anyMatch(b -> b.getPlate().equals("EEE555"));
    }

    @Test
    @DisplayName("Debe cambiar el estado de un bus")
    void shouldChangeBusStatus() {
        // Given
        Bus bus = createBus("GGG777", 40, BusStatus.AVAILABLE, OffsetDateTime.now().plusYears(1), Set.of());
        busRepository.save(bus);
        // When
        busRepository.changeBusStatus(bus.getId(), BusStatus.IN_MAINTENANCE);
        busRepository.flush();

        // Then
        Bus updated = busRepository.findById(bus.getId()).get();
        assertThat(updated.getStatus()).isEqualTo(BusStatus.IN_MAINTENANCE);
    }

    @Test
    @DisplayName("Debe calcular el porcentaje de overbooking del bus")
    void shouldCalculateOccupancyRate() {
        // Given
        Bus bus = createBus("HHH888", 4, BusStatus.AVAILABLE, OffsetDateTime.now().plusYears(1), Set.of());
        Seat seat1 = createSeat(bus, 1, BigDecimal.valueOf(50000), SeatType.STANDARD, SeatStatus.TAKEN);
        Seat seat2 = createSeat(bus, 2, BigDecimal.valueOf(50000), SeatType.STANDARD, SeatStatus.TAKEN);
        Seat seat3 = createSeat(bus, 3, BigDecimal.valueOf(50000), SeatType.STANDARD, SeatStatus.AVAILABLE);
        Seat seat4 = createSeat(bus, 4, BigDecimal.valueOf(50000), SeatType.STANDARD, SeatStatus.AVAILABLE);
        bus.setSeats(List.of(seat1, seat2, seat3, seat4));
        busRepository.save(bus);

        // When
        Double occupancyRate = busRepository.calculateOccupancyRate(bus.getId());

        // Then
        assertThat(occupancyRate).isNotNull();
        assertThat(occupancyRate).isEqualTo(0.5f); // 2 de 4 asientos ocupados = 50%
    }

    @Test
    @DisplayName("Debe retornar lista vacía cuando no hay buses con el estado especificado")
    void shouldReturnEmptyListWhenNoBusesWithStatus() {
        // Given
        Bus bus = createBus("III999", 40, BusStatus.AVAILABLE, OffsetDateTime.now().plusYears(1), Set.of());
        busRepository.save(bus);
        // When
        List<Bus> buses = busRepository.findBusesByStatus(BusStatus.OUT_OF_SERVICE);

        // Then
        assertThat(buses).isEmpty();
    }
}
