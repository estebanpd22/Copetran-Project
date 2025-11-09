package co.unimagdalena.repositories;

import co.unimagdalena.domine.entities.*;
import co.unimagdalena.domine.repositories.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class SeatHoldRepositoryTest extends AbstractRepositoryTI {
    @Autowired
    private SeatHoldRepository seatHoldRepository;
    @Autowired
    private SeatRepository seatRepository;
    @Autowired
    private TripRepository tripRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private BusRepository busRepository;
    @Autowired
    private RouteRepository routeRepository;

    private User createUser(String fullName, String email, String phone, String passwordHash,
                            UserRole role, UserStatus status, LocalDateTime createdAt) {
        return userRepository.save(User.builder()
                .fullName(fullName)
                .email(email)
                .phone(phone)
                .passwordHash(passwordHash)
                .role(role)
                .status(status)
                .createdAt(createdAt)
                .build());
    }

    private Bus createBus(String plate, Integer capacity, BusStatus status, OffsetDateTime soatExpirationDate) {
        return busRepository.save(Bus.builder()
                .plate(plate)
                .capacity(capacity)
                .status(status)
                .soatExpirationDate(soatExpirationDate)
                .build());
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

    private SeatHold createSeatHold(Seat seat, Trip trip, User user, String seatNumber,
                                    OffsetDateTime expiresAt, SeatHoldStatus status) {
        return seatHoldRepository.save(SeatHold.builder()
                .seat(seat)
                .trip(trip)
                .user(user)
                .seatNumber(seatNumber)
                .expiresAt(expiresAt)
                .status(status)
                .build());
    }

    @Test
    @DisplayName("Debe encontrar SeatHolds por tripId")
    void shouldFindByTripId() {
        // Given
        User user = createUser("Juan Pérez", "juan.perez@email.com", "3001234567",
                "hashedpassword", UserRole.PASSENGER, UserStatus.ACTIVE,
                LocalDateTime.now());
        Bus bus = createBus("ABC123", 40, BusStatus.AVAILABLE, OffsetDateTime.now().plusYears(1));
        Route route = createRoute("R001", "Ruta Norte", "Bogotá", "Medellín", 400.0f, 480.0f);
        Trip trip = createTrip(bus, route, LocalDate.now().plusDays(1), OffsetDateTime.now().plusDays(1),
                OffsetDateTime.now().plusDays(1).plusHours(8), TripStatus.SCHEDULED);
        Seat seat1 = createSeat(bus, 1, BigDecimal.valueOf(50000), SeatType.STANDARD, SeatStatus.UNAVAILABLE);
        Seat seat2 = createSeat(bus, 2, BigDecimal.valueOf(60000), SeatType.PREFERENTIAL, SeatStatus.UNAVAILABLE);

        SeatHold seatHold = createSeatHold(seat1, trip, user, "1", OffsetDateTime.now().plusMinutes(15), SeatHoldStatus.HOLD);
        SeatHold seatHold1 = createSeatHold(seat2, trip, user, "2", OffsetDateTime.now().plusMinutes(15), SeatHoldStatus.HOLD);

        // When
        List<SeatHold> seatHolds = seatHoldRepository.findByTripId(trip.getId());

        // Then
        assertThat(seatHolds).hasSize(2);
        assertThat(seatHolds).allMatch(sh -> sh.getTrip().getId().equals(trip.getId()));
    }

    @Test
    @DisplayName("Debe encontrar SeatHolds por userId")
    void shouldFindByUserId() {
        // Given
        User user = createUser("María López", "maria.lopez@email.com", "3009876543",
                "hashedpassword1", UserRole.PASSENGER, UserStatus.ACTIVE,
                LocalDateTime.now());
        Bus bus = createBus("XYZ789", 40, BusStatus.AVAILABLE, OffsetDateTime.now().plusYears(1));
        Route route = createRoute("R002", "Ruta Sur", "Cali", "Pasto", 300.0f, 360.0f);
        Trip trip = createTrip(bus, route, LocalDate.now().plusDays(2), OffsetDateTime.now().plusDays(2),
                OffsetDateTime.now().plusDays(2).plusHours(6), TripStatus.SCHEDULED);
        Seat seat = createSeat(bus, 3, BigDecimal.valueOf(55000), SeatType.STANDARD, SeatStatus.UNAVAILABLE);

        createSeatHold(seat, trip, user, "3", OffsetDateTime.now().plusMinutes(20), SeatHoldStatus.HOLD);

        // When
        List<SeatHold> seatHolds = seatHoldRepository.findByUserId(user.getId());

        // Then
        assertThat(seatHolds).hasSize(1);
        assertThat(seatHolds.getFirst().getUser().getId()).isEqualTo(user.getId());
        assertThat(seatHolds.getFirst().getSeatNumber()).isEqualTo("3");
        assertThat(seatHolds.getFirst().getStatus()).isEqualTo(SeatHoldStatus.HOLD);
    }

    @Test
    @DisplayName("Debe encontrar SeatHolds expirados")
    void shouldFindExpiredHolds() {
        // Given
        User user = createUser("Carlos Ruiz", "carlos.ruiz@email.com", "3002345678",
                "hashedpassword2", UserRole.PASSENGER, UserStatus.ACTIVE,
                LocalDateTime.now());
        Bus bus = createBus("DEF456", 40, BusStatus.AVAILABLE, OffsetDateTime.now().plusYears(1));
        Route route = createRoute("R003", "Ruta Oriente", "Bucaramanga", "Cúcuta", 200.0f, 240.0f);
        Trip trip = createTrip(bus, route, LocalDate.now().plusDays(3), OffsetDateTime.now().plusDays(3),
                OffsetDateTime.now().plusDays(3).plusHours(4), TripStatus.SCHEDULED);
        Seat seat = createSeat(bus, 4, BigDecimal.valueOf(45000), SeatType.STANDARD, SeatStatus.UNAVAILABLE);

        // Crear una reserva expirada (en el pasado)
        createSeatHold(seat, trip, user, "4", OffsetDateTime.now().minusMinutes(5), SeatHoldStatus.HOLD);

        // When
        List<SeatHold> expiredHolds = seatHoldRepository.findExpiredHolds();

        // Then
        assertThat(expiredHolds).isNotEmpty();
        assertThat(expiredHolds).allMatch(sh -> sh.getExpiresAt().isBefore(OffsetDateTime.now()) &&
                sh.getStatus() == SeatHoldStatus.HOLD);
    }

    @Test
    @DisplayName("Debe cambiar el estado del bus")
    void shouldChangeBusStatus() {
        // Given
        Bus bus = createBus("GHI789", 40, BusStatus.AVAILABLE, OffsetDateTime.now().plusYears(1));

        // When
        seatHoldRepository.changeBusStatus(bus.getId(), BusStatus.IN_MAINTENANCE);
        seatHoldRepository.flush();

        // Then
        Bus updated = busRepository.findById(bus.getId()).get();
        assertThat(updated.getStatus()).isEqualTo(BusStatus.IN_MAINTENANCE);
    }

    @Test
    @DisplayName("Debe retornar lista vacía cuando no hay SeatHolds para el viaje")
    void shouldReturnEmptyListWhenNoSeatHoldsForTrip() {
        // When
        List<SeatHold> seatHolds = seatHoldRepository.findByTripId(999L);

        // Then
        assertThat(seatHolds).isEmpty();
    }

    @Test
    @DisplayName("Debe retornar lista vacía cuando el usuario no tiene SeatHolds")
    void shouldReturnEmptyListWhenUserHasNoSeatHolds() {
        // When
        List<SeatHold> seatHolds = seatHoldRepository.findByUserId(999L);

        // Then
        assertThat(seatHolds).isEmpty();
    }

    @Test
    @DisplayName("Debe retornar lista vacía cuando no hay SeatHolds expirados")
    void shouldReturnEmptyListWhenNoExpiredHolds() {
        // Given
        User user = createUser("Ana Torres", "ana.torres@email.com", "3008765432",
                "hashedpassword3", UserRole.PASSENGER, UserStatus.ACTIVE,
                LocalDateTime.now());
        Bus bus = createBus("JKL012", 40, BusStatus.AVAILABLE, OffsetDateTime.now().plusYears(1));
        Route route = createRoute("R004", "Ruta Costa", "Barranquilla", "Cartagena", 120.0f, 150.0f);
        Trip trip = createTrip(bus, route, LocalDate.now().plusDays(4), OffsetDateTime.now().plusDays(4),
                OffsetDateTime.now().plusDays(4).plusHours(2), TripStatus.SCHEDULED);
        Seat seat = createSeat(bus, 5, BigDecimal.valueOf(48000), SeatType.STANDARD, SeatStatus.UNAVAILABLE);

        // Crear una reserva que NO está expirada
        createSeatHold(seat, trip, user, "5", OffsetDateTime.now().plusHours(1), SeatHoldStatus.HOLD);

        // When
        List<SeatHold> expiredHolds = seatHoldRepository.findExpiredHolds();

        // Then
        assertThat(expiredHolds).isEmpty();
    }
}