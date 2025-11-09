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
import java.util.Set;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class BaggageRepositoryTest extends AbstractRepositoryTI {
    @Autowired
    private BaggageRepository baggageRepository;
    @Autowired
    private TicketRepository ticketRepository;
    @Autowired
    private TripRepository tripRepository;
    @Autowired
    private PurchaseRepository purchaseRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RouteRepository routeRepository;
    @Autowired
    private BusRepository busRepository;
    @Autowired
    private SeatRepository seatRepository;
    @Autowired
    private StopRepository stopRepository;

    private User createUser(String fullName, String email, String password,
                            UserRole role, UserStatus status, LocalDateTime date, String phone) {
        return userRepository.save(User.builder()
                .fullName(fullName)
                .email(email)
                .passwordHash(password)
                .role(role)
                .status(status)
                .phone(phone)
                .createdAt(date).build());
    }

    private Route createRoute(String code, String origin, String destination,
                              Float distanceKm, Float durationMin, String name) {
        return routeRepository.save(Route.builder()
                .code(code)
                .name(name)
                .origin(origin)
                .destination(destination)
                .distanceKm(distanceKm)
                .durationMin(durationMin).build());
    }

    private Stop createStop(String name, Integer order, double latitude,
                            double longitude, Route route) {
        return stopRepository.save(Stop.builder()
                .name(name)
                .order(order)
                .latitude(latitude)
                .longitude(longitude)
                .route(route).build());
    }

    private Bus createBusWithoutSaving(String plate, Set<Amenity> amenities,
                                       Integer capacity, OffsetDateTime soatExpirationDate,
                                       BusStatus status) {
        return Bus.builder()
                .plate(plate)
                .amenities(amenities)
                .capacity(capacity)
                .soatExpirationDate(soatExpirationDate)
                .status(status).build();
    }

    private Seat createSeat(Integer number, BigDecimal price,
                            SeatType type, SeatStatus status) {
        return seatRepository.save(Seat.builder()
                .number(number)
                .price(price)
                .type(type)
                .status(status).build());
    }

    private Trip createTrip(LocalDate date, OffsetDateTime departureAt,
                            OffsetDateTime arrivalAt, TripStatus status, Route route) {
        return tripRepository.save(Trip.builder()
                .date(date)
                .departureAt(departureAt)
                .arrivalAt(arrivalAt)
                .status(status)
                .route(route).build());
    }

    private Purchase createPurchase(PaymentMethod paymentMethod, BigDecimal totalAmount,
                                    PaymentStatus paymentStatus, OffsetDateTime createdAt, User user) {
        return purchaseRepository.save(Purchase.builder()
                .paymentMethod(paymentMethod)
                .totalAmount(totalAmount)
                .paymentStatus(paymentStatus)
                .createdAt(createdAt)
                .user(user).build());
    }

    private Ticket createTicket(BigDecimal price, TicketStatus status, String qrCode,
                                Purchase purchase, Trip trip, Seat seat, Stop fromStop, Stop toStop) {
        return ticketRepository.save(Ticket.builder()
                .price(price)
                .status(status)
                .qrCode(qrCode)
                .purchase(purchase)
                .trip(trip)
                .seat(seat)
                .fromStop(fromStop)
                .toStop(toStop).build());
    }

    private Baggage createBaggage(Float weightKg, BigDecimal fee, String tagCode, Ticket ticket) {
        return baggageRepository.save(Baggage.builder()
                .weightKg(weightKg)
                .fee(fee)
                .tagCode(tagCode)
                .ticket(ticket).build());
    }

    @Test
    @DisplayName("Debe encontrar todos los equipajes por el id del ticket")
    void shouldFindByTicketId() {
        // Given
        LocalDateTime userCreatedAt = LocalDateTime.now().minusMonths(1);
        OffsetDateTime purchaseCreatedAt = OffsetDateTime.now();
        OffsetDateTime departure = OffsetDateTime.now().plusHours(5);
        OffsetDateTime arrival = OffsetDateTime.now().plusHours(10);
        LocalDate tripDate = LocalDate.now();

        // Crear usuario
        User user = createUser("Juan Pérez", "juan.perez@gmail.com",
                "password123", UserRole.PASSENGER, UserStatus.ACTIVE,
                userCreatedAt, "3001234567");

        // Crear ruta
        Route route = createRoute("RT001", "Bogotá", "Medellín",
                400.5f, 480.0f, "Bogotá-Medellín Express");

        // Crear paradas
        Stop fromStop = createStop("terminal1", 1, 4.7110, -74.0721, route);
        Stop toStop = createStop("terminal2", 5, 6.2442, -75.5812, route);

        // Crear bus con asientos
        Bus bus = createBusWithoutSaving("ABC123",
                Set.of(new Amenity(1L, "WiFi"), new Amenity(2L, "Aire acondicionado")),
                40, OffsetDateTime.now().plusMonths(6), BusStatus.ASSIGNED);

        Seat seat1 = createSeat(15, BigDecimal.valueOf(50000), SeatType.STANDARD, SeatStatus.AVAILABLE);
        bus.setSeats(List.of(seat1));

        // Crear viaje
        Trip trip = createTrip(tripDate, departure, arrival, TripStatus.SCHEDULED, route);
        bus.addTrip(trip);
        bus = busRepository.save(bus);

        // Crear compra
        Purchase purchase = createPurchase(PaymentMethod.CARD, BigDecimal.valueOf(50000),
                PaymentStatus.CONFIRMED, purchaseCreatedAt, user);

        // Crear ticket
        Ticket ticket = createTicket(BigDecimal.valueOf(50000), TicketStatus.SOLD,
                "QR123456", purchase, trip, seat1, fromStop, toStop);

        // Crear equipajes
        Baggage baggage1 = createBaggage(15.5f, BigDecimal.valueOf(10000), "TAG001", ticket);
        Baggage baggage2 = createBaggage(8.2f, BigDecimal.valueOf(5000), "TAG002", ticket);

        // When
        List<Baggage> foundBaggages = baggageRepository.findByTicketId(ticket.getId());

        // Then
        assertThat(foundBaggages.size()).isEqualTo(2);
        assertThat(foundBaggages).isNotNull();

        // Verificar primer equipaje
        Baggage firstBaggage = foundBaggages.getFirst();
        assertThat(firstBaggage.getId()).isNotNull();
        assertThat(firstBaggage.getWeightKg()).isEqualTo(15.5f);
        assertThat(firstBaggage.getFee()).isEqualByComparingTo(BigDecimal.valueOf(10000));
        assertThat(firstBaggage.getTagCode()).isEqualTo("TAG001");
        assertThat(firstBaggage.getTicket().getId()).isEqualTo(ticket.getId());
        assertThat(firstBaggage.getTicket().getQrCode()).isEqualTo("QR123456");
        assertThat(firstBaggage.getTicket().getStatus()).isEqualTo(TicketStatus.SOLD);

        // Verificar segundo equipaje
        Baggage secondBaggage = foundBaggages.get(1);
        assertThat(secondBaggage.getId()).isNotNull();
        assertThat(secondBaggage.getWeightKg()).isEqualTo(8.2f);
        assertThat(secondBaggage.getFee()).isEqualByComparingTo(BigDecimal.valueOf(5000));
        assertThat(secondBaggage.getTagCode()).isEqualTo("TAG002");
        assertThat(secondBaggage.getTicket().getId()).isEqualTo(ticket.getId());
    }

    @Test
    @DisplayName("Debe encontrar todos los equipajes por el id del viaje")
    void shouldFindByTripId() {
        // Given
        LocalDateTime userCreatedAt = LocalDateTime.now().minusMonths(2);
        OffsetDateTime purchaseCreatedAt = OffsetDateTime.now();
        OffsetDateTime departure = OffsetDateTime.now().plusHours(3);
        OffsetDateTime arrival = OffsetDateTime.now().plusHours(8);
        LocalDate tripDate = LocalDate.now();

        // Crear usuarios
        User user1 = createUser("María García", "maria.garcia@gmail.com",
                "password456", UserRole.PASSENGER, UserStatus.ACTIVE,
                userCreatedAt, "3009876543");

        User user2 = createUser("Carlos López", "carlos.lopez@gmail.com",
                "password789", UserRole.PASSENGER, UserStatus.ACTIVE,
                userCreatedAt.plusDays(1), "3007654321");

        // Crear ruta
        Route route = createRoute("RT002", "Cali", "Cartagena",
                Float.valueOf(650.0f), Float.valueOf(720.0f), "Cali-Cartagena Directo");

        // Crear paradas
        Stop fromStop = createStop("Terminal Cali", 1, 3.4516, -76.5320, route);
        Stop toStop = createStop("Terminal Cartagena", 8, 10.3910, -75.4794, route);

        // Crear bus con asientos
        Bus bus = createBusWithoutSaving("XYZ789",
                Set.of(new Amenity(1L, "TV"), new Amenity(2L, "Baño")),
                50, OffsetDateTime.now().plusMonths(8), BusStatus.ASSIGNED);

        Seat seat1 = createSeat(10, BigDecimal.valueOf(80000), SeatType.PREFERENTIAL, SeatStatus.AVAILABLE);
        Seat seat2 = createSeat(20, BigDecimal.valueOf(60000), SeatType.STANDARD, SeatStatus.AVAILABLE);
        bus.setSeats(List.of(seat1, seat2));

        // Crear viaje
        Trip trip = createTrip(tripDate, departure, arrival, TripStatus.BOARDING, route);
        bus.addTrip(trip);
        bus = busRepository.save(bus);

        // Crear compras
        Purchase purchase1 = createPurchase(PaymentMethod.TRANSFER, BigDecimal.valueOf(80000),
                PaymentStatus.CONFIRMED, purchaseCreatedAt, user1);

        Purchase purchase2 = createPurchase(PaymentMethod.CASH, BigDecimal.valueOf(60000),
                PaymentStatus.CONFIRMED, purchaseCreatedAt.plusMinutes(10), user2);

        // Crear tickets
        Ticket ticket1 = createTicket(BigDecimal.valueOf(80000), TicketStatus.SOLD,
                "QR789012", purchase1, trip, seat1, fromStop, toStop);

        Ticket ticket2 = createTicket(BigDecimal.valueOf(60000), TicketStatus.SOLD,
                "QR345678", purchase2, trip, seat2, fromStop, toStop);

        // Crear equipajes para ticket1
        Baggage baggage1 = createBaggage(20.0f, BigDecimal.valueOf(15000), "TAG100", ticket1);
        Baggage baggage2 = createBaggage(12.5f, BigDecimal.valueOf(8000), "TAG101", ticket1);

        // Crear equipaje para ticket2
        Baggage baggage3 = createBaggage(18.3f, BigDecimal.valueOf(12000), "TAG200", ticket2);

        // When
        List<Baggage> foundBaggages = baggageRepository.findByTripId(trip.getId());

        // Then
        assertThat(foundBaggages.size()).isEqualTo(3);
        assertThat(foundBaggages).isNotNull();

        // Verificar que todos los equipajes pertenecen al viaje correcto
        foundBaggages.forEach(baggage -> {
            assertThat(baggage.getId()).isNotNull();
            assertThat(baggage.getWeightKg()).isNotNull();
            assertThat(baggage.getFee()).isNotNull();
            assertThat(baggage.getTagCode()).isNotNull();
            assertThat(baggage.getTicket()).isNotNull();
            assertThat(baggage.getTicket().getTrip().getId()).isEqualTo(trip.getId());
            assertThat(baggage.getTicket().getTrip().getStatus()).isEqualTo(TripStatus.BOARDING);
        });

        // Verificar equipajes específicos
        Baggage foundBaggage1 = foundBaggages.stream()
                .filter(b -> b.getTagCode().equals("TAG100"))
                .findFirst()
                .orElse(null);
        assertThat(foundBaggage1).isNotNull();
        assertThat(foundBaggage1.getWeightKg()).isEqualTo(20.0f);
        assertThat(foundBaggage1.getFee()).isEqualByComparingTo(BigDecimal.valueOf(15000));

        Baggage foundBaggage3 = foundBaggages.stream()
                .filter(b -> b.getTagCode().equals("TAG200"))
                .findFirst()
                .orElse(null);
        assertThat(foundBaggage3).isNotNull();
        assertThat(foundBaggage3.getWeightKg()).isEqualTo(18.3f);
        assertThat(foundBaggage3.getFee()).isEqualByComparingTo(BigDecimal.valueOf(12000));
        assertThat(foundBaggage3.getTicket().getId()).isEqualTo(ticket2.getId());
    }

    @Test
    @DisplayName("Debe retornar lista vacía cuando no hay equipajes para un ticket")
    void shouldReturnEmptyListWhenNoBaggageForTicket() {
        // Given
        LocalDateTime userCreatedAt = LocalDateTime.now();
        OffsetDateTime purchaseCreatedAt = OffsetDateTime.now();
        OffsetDateTime departure = OffsetDateTime.now().plusHours(2);
        OffsetDateTime arrival = OffsetDateTime.now().plusHours(6);
        LocalDate tripDate = LocalDate.now();

        User user = createUser("Pedro Sánchez", "pedro.sanchez@gmail.com",
                "password321", UserRole.PASSENGER, UserStatus.ACTIVE,
                userCreatedAt, "3005551234");

        Route route = createRoute("RT003", "Barranquilla", "Bucaramanga",
                Float.valueOf(300.0f), Float.valueOf(360.0f), "Barranquilla-Bucaramanga");

        Stop fromStop = createStop("Terminal Barranquilla", 1, 10.9639, -74.7964, route);
        Stop toStop = createStop("Terminal Bucaramanga", 4, 7.1193, -73.1227, route);

        Bus bus = createBusWithoutSaving("DEF456",
                Set.of(new Amenity(1L, "WiFi")),
                30, OffsetDateTime.now().plusMonths(4), BusStatus.ASSIGNED);

        Seat seat = createSeat(5, BigDecimal.valueOf(45000), SeatType.STANDARD, SeatStatus.AVAILABLE);
        bus.setSeats(List.of(seat));

        Trip trip = createTrip(tripDate, departure, arrival, TripStatus.SCHEDULED, route);
        bus.addTrip(trip);
        bus = busRepository.save(bus);

        Purchase purchase = createPurchase(PaymentMethod.QR, BigDecimal.valueOf(45000),
                PaymentStatus.CONFIRMED, purchaseCreatedAt, user);

        Ticket ticket = createTicket(BigDecimal.valueOf(45000), TicketStatus.SOLD,
                "QR111222", purchase, trip, seat, fromStop, toStop);

        // When - No crear equipajes
        List<Baggage> foundBaggages = baggageRepository.findByTicketId(ticket.getId());

        // Then
        assertThat(foundBaggages).isNotNull();
        assertThat(foundBaggages.size()).isEqualTo(0);
    }

    @Test
    @DisplayName("Debe retornar lista vacía cuando no hay equipajes para un viaje")
    void shouldReturnEmptyListWhenNoBaggageForTrip() {
        // Given
        OffsetDateTime departure = OffsetDateTime.now().plusHours(4);
        OffsetDateTime arrival = OffsetDateTime.now().plusHours(9);
        LocalDate tripDate = LocalDate.now();

        Route route = createRoute("RT004", "Pereira", "Pasto",
                Float.valueOf(450.0f), Float.valueOf(540.0f), "Pereira-Pasto Express");

        Bus bus = createBusWithoutSaving("GHI789",
                Set.of(new Amenity(1L, "Aire acondicionado")),
                35, OffsetDateTime.now().plusMonths(5), BusStatus.ASSIGNED);

        Seat seat = createSeat(12, BigDecimal.valueOf(70000), SeatType.STANDARD, SeatStatus.AVAILABLE);
        bus.setSeats(List.of(seat));

        Trip trip = createTrip(tripDate, departure, arrival, TripStatus.SCHEDULED, route);
        bus.addTrip(trip);
        bus = busRepository.save(bus);

        // When - No crear tickets ni equipajes
        List<Baggage> foundBaggages = baggageRepository.findByTripId(trip.getId());

        // Then
        assertThat(foundBaggages).isNotNull();
        assertThat(foundBaggages.size()).isEqualTo(0);
    }
}