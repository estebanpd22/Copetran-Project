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
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

public class TicketRepositoryTest extends AbstractRepositoryTI {
    @Autowired
    private TicketRepository ticketRepository;
    @Autowired
    private TripRepository tripRepository;
    @Autowired
    private PurchaseRepository purchaseRepository;
    @Autowired
    private SeatRepository seatRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private BusRepository busRepository;
    @Autowired
    private RouteRepository routeRepository;
    @Autowired
    private StopRepository stopRepository;

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

    private Stop createStop(Route route, String name, Integer order, double latitude, double longitude) {
        return stopRepository.save(Stop.builder()
                .route(route)
                .name(name)
                .order(order)
                .latitude(latitude)
                .longitude(longitude)
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

    private Purchase createPurchase(User user, PaymentMethod paymentMethod, BigDecimal totalAmount,
                                    PaymentStatus paymentStatus, OffsetDateTime createdAt) {
        return purchaseRepository.save(Purchase.builder()
                .user(user)
                .paymentMethod(paymentMethod)
                .totalAmount(totalAmount)
                .paymentStatus(paymentStatus)
                .createdAt(createdAt)
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

    private Ticket createTicket(Purchase purchase, Trip trip, Seat seat, Stop fromStop, Stop toStop,
                                BigDecimal price, TicketStatus status, String qrCode) {
        return ticketRepository.save(Ticket.builder()
                .purchase(purchase)
                .trip(trip)
                .seat(seat)
                .fromStop(fromStop)
                .toStop(toStop)
                .price(price)
                .status(status)
                .qrCode(qrCode)
                .build());
    }

    @Test
    @DisplayName("Debe encontrar tickets por tripId")
    void shouldFindByTripId() {
        // Given
        User user = createUser("Juan Pérez", "juan.perez@email.com", "3001234567",
                "hashedpassword", UserRole.PASSENGER, UserStatus.ACTIVE,
                LocalDateTime.now());
        Bus bus = createBus("ABC123", 40, BusStatus.AVAILABLE, OffsetDateTime.now().plusYears(1));
        Route route = createRoute("R001", "Ruta Norte", "Bogotá", "Medellín", 400.0f, 480.0f);
        Stop stop1 = createStop(route, "Terminal Bogotá", 1, 4.6097, -74.0817);
        Stop stop2 = createStop(route, "Terminal Medellín", 2, 6.2442, -75.5812);
        Trip trip = createTrip(bus, route, LocalDate.now().plusDays(1), OffsetDateTime.now().plusDays(1),
                OffsetDateTime.now().plusDays(1).plusHours(8), TripStatus.SCHEDULED);
        Purchase purchase = createPurchase(user, PaymentMethod.CARD, BigDecimal.valueOf(150000),
                PaymentStatus.CONFIRMED, OffsetDateTime.now());
        Seat seat1 = createSeat(bus, 1, BigDecimal.valueOf(75000), SeatType.STANDARD, SeatStatus.UNAVAILABLE);
        Seat seat2 = createSeat(bus, 2, BigDecimal.valueOf(75000), SeatType.STANDARD, SeatStatus.UNAVAILABLE);

        Ticket t1 = createTicket(purchase, trip, seat1, stop1, stop2, BigDecimal.valueOf(75000), TicketStatus.SOLD, "QR001");
        Ticket t2 = createTicket(purchase, trip, seat2, stop1, stop2, BigDecimal.valueOf(75000), TicketStatus.SOLD, "QR002");

        // When
        List<Ticket> tickets = ticketRepository.findByTripId(trip.getId());

        // Then
        assertThat(tickets).hasSize(2);
        assertThat(tickets).allMatch(t -> t.getTrip().getId().equals(trip.getId()));
    }

    @Test
    @DisplayName("Debe encontrar tickets por purchaseId")
    void shouldFindByPurchaseId() {
        // Given
        User user = createUser("María López", "maria.lopez@email.com", "3009876543",
                "hashedpassword1", UserRole.PASSENGER, UserStatus.ACTIVE,
                LocalDateTime.now());
        Bus bus = createBus("XYZ789", 40, BusStatus.AVAILABLE, OffsetDateTime.now().plusYears(1));
        Route route = createRoute("R002", "Ruta Sur", "Cali", "Pasto", 300.0f, 360.0f);
        Stop stop1 = createStop(route, "Terminal Cali", 1, 3.4516, -76.5320);
        Stop stop2 = createStop(route, "Terminal Pasto", 2, 1.2136, -77.2811);
        Trip trip = createTrip(bus, route, LocalDate.now().plusDays(2), OffsetDateTime.now().plusDays(2),
                OffsetDateTime.now().plusDays(2).plusHours(6), TripStatus.SCHEDULED);
        Purchase purchase = createPurchase(user, PaymentMethod.TRANSFER, BigDecimal.valueOf(130000),
                PaymentStatus.CONFIRMED, OffsetDateTime.now());
        Seat seat = createSeat(bus, 3, BigDecimal.valueOf(65000), SeatType.STANDARD, SeatStatus.UNAVAILABLE);

        createTicket(purchase, trip, seat, stop1, stop2, BigDecimal.valueOf(65000), TicketStatus.SOLD, "QR003");

        // When
        List<Ticket> tickets = ticketRepository.findByPurchaseId(purchase.getId());

        // Then
        assertThat(tickets).hasSize(1);
        assertThat(tickets.getFirst().getPurchase().getId()).isEqualTo(purchase.getId());
        assertThat(tickets.getFirst().getPrice()).isEqualByComparingTo(BigDecimal.valueOf(65000));
    }

    @Test
    @DisplayName("Debe encontrar un ticket por qrCode")
    void shouldFindByQrCode() {
        // Given
        User user = createUser("Carlos Ruiz", "carlos.ruiz@email.com", "3002345678",
                "hashedpassword2", UserRole.PASSENGER, UserStatus.ACTIVE,
                LocalDateTime.now());
        Bus bus = createBus("DEF456", 40, BusStatus.AVAILABLE, OffsetDateTime.now().plusYears(1));
        Route route = createRoute("R003", "Ruta Oriente", "Bucaramanga", "Cúcuta", 200.0f, 240.0f);
        Stop stop1 = createStop(route, "Terminal Bucaramanga", 1, 7.1193, -73.1227);
        Stop stop2 = createStop(route, "Terminal Cúcuta", 2, 7.8939, -72.5078);
        Trip trip = createTrip(bus, route, LocalDate.now().plusDays(3), OffsetDateTime.now().plusDays(3),
                OffsetDateTime.now().plusDays(3).plusHours(4), TripStatus.SCHEDULED);
        Purchase purchase = createPurchase(user, PaymentMethod.QR, BigDecimal.valueOf(60000),
                PaymentStatus.CONFIRMED, OffsetDateTime.now());
        Seat seat = createSeat(bus, 4, BigDecimal.valueOf(60000), SeatType.STANDARD, SeatStatus.UNAVAILABLE);

        createTicket(purchase, trip, seat, stop1, stop2, BigDecimal.valueOf(60000), TicketStatus.SOLD, "QR12345");

        // When
        Optional<Ticket> found = ticketRepository.findByQrCode("QR12345");

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getQrCode()).isEqualTo("QR12345");
        assertThat(found.get().getStatus()).isEqualTo(TicketStatus.SOLD);
    }

    @Test
    @DisplayName("Debe encontrar tickets por estado")
    void shouldFindTicketByStatus() {
        // Given
        User user = createUser("Ana Torres", "ana.torres@email.com", "3008765432",
                "hashedpassword3", UserRole.PASSENGER, UserStatus.ACTIVE,
                LocalDateTime.now());
        Bus bus = createBus("GHI789", 40, BusStatus.AVAILABLE, OffsetDateTime.now().plusYears(1));
        Route route = createRoute("R004", "Ruta Costa", "Barranquilla", "Cartagena", 120.0f, 150.0f);
        Stop stop1 = createStop(route, "Terminal Barranquilla", 1, 10.9639, -74.7964);
        Stop stop2 = createStop(route, "Terminal Cartagena", 2, 10.3910, -75.4794);
        Trip trip = createTrip(bus, route, LocalDate.now().plusDays(4), OffsetDateTime.now().plusDays(4),
                OffsetDateTime.now().plusDays(4).plusHours(2), TripStatus.SCHEDULED);
        Purchase purchase = createPurchase(user, PaymentMethod.CASH, BigDecimal.valueOf(90000),
                PaymentStatus.CONFIRMED, OffsetDateTime.now());
        Seat seat1 = createSeat(bus, 5, BigDecimal.valueOf(45000), SeatType.STANDARD, SeatStatus.UNAVAILABLE);
        Seat seat2 = createSeat(bus, 6, BigDecimal.valueOf(45000), SeatType.STANDARD, SeatStatus.AVAILABLE);

        createTicket(purchase, trip, seat1, stop1, stop2, BigDecimal.valueOf(45000), TicketStatus.SOLD, "QR004");
        createTicket(purchase, trip, seat2, stop1, stop2, BigDecimal.valueOf(45000), TicketStatus.CANCELLED, "QR005");

        // When
        List<Ticket> soldTickets = ticketRepository.findTicketByStatus(TicketStatus.SOLD);

        // Then
        assertThat(soldTickets).isNotEmpty();
        assertThat(soldTickets).allMatch(t -> t.getStatus() == TicketStatus.SOLD);
    }

    @Test
    @DisplayName("Debe encontrar tickets por purchaseUserId")
    void shouldFindByPurchaseUserId() {
        // Given
        User user = createUser("Pedro Gómez", "pedro.gomez@email.com", "3003456789",
                "hashedpassword4", UserRole.PASSENGER, UserStatus.ACTIVE,
                LocalDateTime.now());
        Bus bus = createBus("JKL012", 40, BusStatus.AVAILABLE, OffsetDateTime.now().plusYears(1));
        Route route = createRoute("R005", "Ruta Eje", "Pereira", "Armenia", 50.0f, 60.0f);
        Stop stop1 = createStop(route, "Terminal Pereira", 1, 4.8133, -75.6961);
        Stop stop2 = createStop(route, "Terminal Armenia", 2, 4.5339, -75.6811);
        Trip trip = createTrip(bus, route, LocalDate.now().plusDays(5), OffsetDateTime.now().plusDays(5),
                OffsetDateTime.now().plusDays(5).plusHours(1), TripStatus.SCHEDULED);
        Purchase purchase = createPurchase(user, PaymentMethod.CARD, BigDecimal.valueOf(35000),
                PaymentStatus.CONFIRMED, OffsetDateTime.now());
        Seat seat = createSeat(bus, 7, BigDecimal.valueOf(35000), SeatType.STANDARD, SeatStatus.UNAVAILABLE);

        createTicket(purchase, trip, seat, stop1, stop2, BigDecimal.valueOf(35000), TicketStatus.SOLD, "QR006");

        // When
        List<Ticket> tickets = ticketRepository.findByPurchaseUserId(user.getId());

        // Then
        assertThat(tickets).hasSize(1);
        assertThat(tickets.getFirst().getPurchase().getUser().getId()).isEqualTo(user.getId());
    }

    @Test
    @DisplayName("Debe contar tickets vendidos por viaje")
    void shouldCountSoldByTrip() {
        // Given
        User user = createUser("Laura Díaz", "laura.diaz@email.com", "3007654321",
                "hashedpassword5", UserRole.PASSENGER, UserStatus.ACTIVE,
                LocalDateTime.now());
        Bus bus = createBus("MNO345", 40, BusStatus.AVAILABLE, OffsetDateTime.now().plusYears(1));
        Route route = createRoute("R006", "Ruta Central", "Manizales", "Ibagué", 150.0f, 180.0f);
        Stop stop1 = createStop(route, "Terminal Manizales", 1, 5.0689, -75.5174);
        Stop stop2 = createStop(route, "Terminal Ibagué", 2, 4.4389, -75.2322);
        Trip trip = createTrip(bus, route, LocalDate.now().plusDays(6), OffsetDateTime.now().plusDays(6),
                OffsetDateTime.now().plusDays(6).plusHours(3), TripStatus.SCHEDULED);
        Purchase purchase = createPurchase(user, PaymentMethod.TRANSFER, BigDecimal.valueOf(110000),
                PaymentStatus.CONFIRMED, OffsetDateTime.now());
        Seat seat1 = createSeat(bus, 8, BigDecimal.valueOf(55000), SeatType.STANDARD, SeatStatus.UNAVAILABLE);
        Seat seat2 = createSeat(bus, 9, BigDecimal.valueOf(55000), SeatType.STANDARD, SeatStatus.UNAVAILABLE);

        createTicket(purchase, trip, seat1, stop1, stop2, BigDecimal.valueOf(55000), TicketStatus.SOLD, "QR007");
        createTicket(purchase, trip, seat2, stop1, stop2, BigDecimal.valueOf(55000), TicketStatus.SOLD, "QR008");

        // When
        long count = ticketRepository.countSoldByTrip(trip.getId());

        // Then
        assertThat(count).isEqualTo(2);
    }

    @Test
    @DisplayName("Debe contar tickets por estado y rango de fechas opcional")
    void shouldCountByStatusAndOptionalDateRange() {
        // Given
        User user = createUser("Luis Martínez", "luis.martinez@email.com", "3004567890",
                "hashedpassword6", UserRole.PASSENGER, UserStatus.ACTIVE,
                LocalDateTime.now());
        Bus bus = createBus("PQR678", 40, BusStatus.AVAILABLE, OffsetDateTime.now().plusYears(1));
        Route route = createRoute("R007", "Ruta Llanos", "Villavicencio", "Bogotá", 120.0f, 150.0f);
        Stop stop1 = createStop(route, "Terminal Villavicencio", 1, 4.1420, -73.6266);
        Stop stop2 = createStop(route, "Terminal Bogotá", 2, 4.6097, -74.0817);
        Trip trip = createTrip(bus, route, LocalDate.now().plusDays(7), OffsetDateTime.now().plusDays(7),
                OffsetDateTime.now().plusDays(7).plusHours(2), TripStatus.SCHEDULED);
        Purchase purchase = createPurchase(user, PaymentMethod.QR, BigDecimal.valueOf(50000),
                PaymentStatus.CONFIRMED, OffsetDateTime.now());
        Seat seat = createSeat(bus, 10, BigDecimal.valueOf(50000), SeatType.STANDARD, SeatStatus.UNAVAILABLE);

        createTicket(purchase, trip, seat, stop1, stop2, BigDecimal.valueOf(50000), TicketStatus.SOLD, "QR009");

        OffsetDateTime start = OffsetDateTime.now().minusDays(1);
        OffsetDateTime end = OffsetDateTime.now().plusDays(1);

        // When
        long count = ticketRepository.countByStatusAndOptionalDateRange(TicketStatus.SOLD, start, end);

        // Then
        assertThat(count).isGreaterThanOrEqualTo(1);
    }

    @Test
    @DisplayName("Debe retornar empty cuando no existe el ticket con el qrCode")
    void shouldReturnEmptyWhenTicketNotFoundByQrCode() {
        // When
        Optional<Ticket> found = ticketRepository.findByQrCode("NONEXISTENT");

        // Then
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("Debe retornar lista vacía cuando no hay tickets para el viaje")
    void shouldReturnEmptyListWhenNoTicketsForTrip() {
        // When
        List<Ticket> tickets = ticketRepository.findByTripId(999L);

        // Then
        assertThat(tickets).isEmpty();
    }
}