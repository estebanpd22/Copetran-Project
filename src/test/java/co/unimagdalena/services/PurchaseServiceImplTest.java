package co.unimagdalena.services;

import co.unimagdalena.api.dto.PurchaseDto.*;
import co.unimagdalena.api.dto.TicketDto.*;
import co.unimagdalena.domine.entities.*;
import co.unimagdalena.domine.repositories.*;
import co.unimagdalena.exception.NotFoundException;
import co.unimagdalena.notification.NotificationHelper;
import co.unimagdalena.services.impl.PurchaseServiceImpl;
import co.unimagdalena.services.mapper.PurchaseMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.message.Message;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.logging.Logger;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PurchaseServiceImplTest {

    @Mock
    private PurchaseRepository purchaseRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TicketService ticketService;

    @Mock
    private SeatHoldService seatHoldService;

    @Mock
    private NotificationHelper notificationHelper;

    @Spy
    private PurchaseMapper purchaseMapper = Mappers.getMapper(PurchaseMapper.class);

    @InjectMocks
    private PurchaseServiceImpl purchaseService;

    // ======================================================================
    // HELPER METHODS - Creación de entidades de prueba
    // ======================================================================

    private User createUser(Long id, String email, String name, String phone,
                            UserRole role, UserStatus status) {
        return User.builder()
                .id(id)
                .email(email)
                .fullName(name)
                .phone(phone)
                .role(role)
                .status(status)
                .passwordHash("hashedPassword")
                .createdAt(LocalDateTime.now())
                .purchases(new ArrayList<>())
                .build();
    }

    private Route createRoute(Long id, String code, String name,
                              String origin, String destination) {
        return Route.builder()
                .id(id)
                .code(code)
                .name(name)
                .origin(origin)
                .destination(destination)
                .distanceKm(100.0f)
                .durationMin(120.0f)
                .stops(new ArrayList<>())
                .trips(new ArrayList<>())
                .fareRules(new ArrayList<>())
                .build();
    }

    private Bus createBus(Long id, String plate, Integer capacity, BusStatus status) {
        return Bus.builder()
                .id(id)
                .plate(plate)
                .capacity(capacity)
                .status(status)
                .soatExpirationDate(OffsetDateTime.now().plusMonths(6))
                .trips(new ArrayList<>())
                .seats(new ArrayList<>())
                .build();
    }

    private Trip createTrip(Long id, LocalDate date, OffsetDateTime departureAt,
                            OffsetDateTime arrivalAt, TripStatus status,
                            Route route, Bus bus) {
        return Trip.builder()
                .id(id)
                .date(date)
                .departureAt(departureAt)
                .arrivalAt(arrivalAt)
                .status(status)
                .route(route)
                .bus(bus)
                .seatHolds(new ArrayList<>())
                .tickets(new ArrayList<>())
                .parcels(new ArrayList<>())
                .build();
    }

    private Passenger createPassenger(Long id, String fullName, String documentNumber) {
        return Passenger.builder()
                .id(id)
                .fullName(fullName)
                .documentType("CC")
                .documentNumber(documentNumber)
                .birthDate(LocalDate.of(1990, 1, 1))
                .phoneNumber("1234567890")
                .createdAt(OffsetDateTime.now())
                .tickets(new ArrayList<>())
                .build();
    }

    private Seat createSeat(Long id, Integer number, BigDecimal price,
                            SeatType type, SeatStatus status, Bus bus) {
        return Seat.builder()
                .id(id)
                .number(number)
                .price(price)
                .type(type)
                .status(status)
                .bus(bus)
                .build();
    }

    private Stop createStop(Long id, String name, Integer order, Route route) {
        return Stop.builder()
                .id(id)
                .name(name)
                .order(order)
                .latitude(4.7110)
                .longitude(-74.0721)
                .route(route)
                .fareRulesFrom(new ArrayList<>())
                .fareRulesTo(new ArrayList<>())
                .build();
    }

    private Purchase createPurchase(Long id, PaymentMethod paymentMethod,
                                    BigDecimal totalAmount, PaymentStatus paymentStatus,
                                    User user) {
        return Purchase.builder()
                .id(id)
                .paymentMethod(paymentMethod)
                .totalAmount(totalAmount)
                .paymentStatus(paymentStatus)
                .createdAt(OffsetDateTime.now())
                .user(user)
                .tickets(new ArrayList<>())
                .build();
    }

    private Ticket createTicket(Long id, String seatNumber, BigDecimal price,
                                TicketStatus status, Purchase purchase, Trip trip,
                                Passenger passenger, Seat seat,
                                Stop fromStop, Stop toStop) {
        return Ticket.builder()
                .id(id)
                .seatNumber(seatNumber)
                .price(price)
                .status(status)
                .createdAt(OffsetDateTime.now())
                .qrCode(null)
                .purchase(purchase)
                .trip(trip)
                .passenger(passenger)
                .seat(seat)
                .fromStop(fromStop)
                .toStop(toStop)
                .build();
    }

    // ======================================================================
    // TESTS - createPurchase
    // ======================================================================

    @Test
    @DisplayName("Debe crear una Compra exitosamente con tickets válidos del mismo viaje")
    void shouldCreatePurchaseSuccessfully() {
        // Arrange
        Long userId = 1L;
        Long tripId = 1L;
        Long passengerId = 1L;
        Long seatId = 1L;
        Long fromStopId = 1L;
        Long toStopId = 2L;

        User user = createUser(userId, "user@test.com", "John Doe", "1234567890",
                UserRole.PASSENGER, UserStatus.ACTIVE);

        Route route = createRoute(1L, "R001", "Ruta Norte", "Bogotá", "Medellín");
        Bus bus = createBus(1L, "ABC123", 40, BusStatus.IN_SERVICE);
        Trip trip = createTrip(tripId, LocalDate.now().plusDays(1),
                OffsetDateTime.now().plusDays(1), OffsetDateTime.now().plusDays(1).plusHours(8),
                TripStatus.SCHEDULED, route, bus);

        Passenger passenger = createPassenger(passengerId, "John Doe", "1234567890");
        Seat seat = createSeat(seatId, 1, new BigDecimal("50000"),
                SeatType.STANDARD, SeatStatus.AVAILABLE, bus);
        Stop fromStop = createStop(fromStopId, "Bogotá", 1, route);
        Stop toStop = createStop(toStopId, "Medellín", 2, route);

        Ticket ticket = createTicket(1L, "1", new BigDecimal("50000"), TicketStatus.SOLD,
                null, trip, passenger, seat, fromStop, toStop);

        List<PurchaseCreateRequest.TicketRequest> ticketRequests = List.of(
                new PurchaseCreateRequest.TicketRequest(tripId, passengerId, seatId, "1", fromStopId, toStopId, null)
        );

        PurchaseCreateRequest createRequest = new PurchaseCreateRequest(
                userId, PaymentMethod.CASH, ticketRequests
        );

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        doNothing().when(seatHoldService).validateActiveHolds(anyLong(), anyList(), anyLong());
        when(ticketService.createTicket(any(), any())).thenReturn(ticket);
        when(purchaseRepository.save(any(Purchase.class))).thenAnswer(inv -> {
            Purchase p = inv.getArgument(0);
            p.setId(1L);
            return p;
        });

        // Act
        PurchaseResponse response = purchaseService.createPurchase(createRequest);

        // Assert
        assertNotNull(response);
        assertEquals(userId, response.user().id());
        assertEquals(PaymentMethod.CASH, response.paymentMethod());
        assertEquals(PaymentStatus.PENDING, response.paymentStatus());
        assertTrue(response.totalAmount().compareTo(BigDecimal.ZERO) > 0);
        verify(purchaseRepository, times(1)).save(any(Purchase.class));
        verify(ticketService, times(1)).createTicket(any(), any());
    }

    @Test
    @DisplayName("Debe lanzar excepcion cuando la lista de tickets está vacía")
    void shouldThrowExceptionWhenTicketsListIsEmpty() {
        // Arrange
        Long userId = 1L;
        User user = createUser(userId, "user@test.com", "John Doe", "1234567890",
                UserRole.PASSENGER, UserStatus.ACTIVE);

        PurchaseCreateRequest createRequest = new PurchaseCreateRequest(
                userId, PaymentMethod.CASH, new ArrayList<>()
        );

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> purchaseService.createPurchase(createRequest));

        assertEquals("La compra debe contener al menos un tiquete.", exception.getMessage());
        verify(purchaseRepository, never()).save(any(Purchase.class));
    }

    @Test
    @DisplayName("Debe lanzar excepcion cuando el Usuario no existe")
    void shouldThrowExceptionWhenUserNotFound() {
        // Arrange
        Long userId = 999L;
        List<PurchaseCreateRequest.TicketRequest> ticketRequests = List.of(
                new PurchaseCreateRequest.TicketRequest(1L, 1L, 1L, "1", 1L, 2L, null)
        );

        PurchaseCreateRequest createRequest = new PurchaseCreateRequest(
                userId, PaymentMethod.CASH, ticketRequests
        );

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Act & Assert
        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> purchaseService.createPurchase(createRequest));

        assertTrue(exception.getMessage().contains("Usuario") && exception.getMessage().contains("no encontrado"));
        verify(purchaseRepository, never()).save(any(Purchase.class));
    }

    @Test
    @DisplayName("Debe lanzar excepcion cuando los tickets no pertenecen al mismo viaje")
    void shouldThrowExceptionWhenTicketsFromDifferentTrips() {
        // Arrange
        Long userId = 1L;
        User user = createUser(userId, "user@test.com", "John Doe", "1234567890",
                UserRole.PASSENGER, UserStatus.ACTIVE);

        // Dos tickets de viajes diferentes
        List<PurchaseCreateRequest.TicketRequest> ticketRequests = List.of(
                new PurchaseCreateRequest.TicketRequest(1L, 1L, 1L, "1", 1L, 2L, null),
                new PurchaseCreateRequest.TicketRequest(2L, 1L, 2L, "2", 1L, 2L, null) // Diferente tripId
        );

        PurchaseCreateRequest createRequest = new PurchaseCreateRequest(
                userId, PaymentMethod.CASH, ticketRequests
        );

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> purchaseService.createPurchase(createRequest));

        assertTrue(exception.getMessage().toLowerCase().contains("mismo viaje"));
        verify(purchaseRepository, never()).save(any(Purchase.class));
    }

    @Test
    @DisplayName("Debe lanzar excepcion cuando el monto total es cero o negativo")
    void shouldThrowExceptionWhenTotalAmountIsZeroOrNegative() {
        // Arrange
        Long userId = 1L;
        Long tripId = 1L;

        User user = createUser(userId, "user@test.com", "John Doe", "1234567890",
                UserRole.PASSENGER, UserStatus.ACTIVE);

        Route route = createRoute(1L, "R001", "Ruta", "Orig", "Dest");
        Bus bus = createBus(1L, "ABC123", 40, BusStatus.IN_SERVICE);
        Trip trip = createTrip(tripId, LocalDate.now().plusDays(1),
                OffsetDateTime.now().plusDays(1), OffsetDateTime.now().plusDays(1).plusHours(8),
                TripStatus.SCHEDULED, route, bus);

        Passenger passenger = createPassenger(1L, "John", "1234567890");
        Seat seat = createSeat(1L, 1, BigDecimal.ZERO, SeatType.STANDARD, SeatStatus.AVAILABLE, bus);
        Stop fromStop = createStop(1L, "Origen", 1, route);
        Stop toStop = createStop(2L, "Destino", 2, route);

        Ticket ticket = createTicket(1L, "1", BigDecimal.ZERO, TicketStatus.SOLD,
                null, trip, passenger, seat, fromStop, toStop);

        List<PurchaseCreateRequest.TicketRequest> ticketRequests = List.of(
                new PurchaseCreateRequest.TicketRequest(tripId, 1L, 1L, "1", 1L, 2L, null)
        );

        PurchaseCreateRequest createRequest = new PurchaseCreateRequest(
                userId, PaymentMethod.CASH, ticketRequests
        );

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        doNothing().when(seatHoldService).validateActiveHolds(anyLong(), anyList(), anyLong());
        when(ticketService.createTicket(any(), any())).thenReturn(ticket);

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> purchaseService.createPurchase(createRequest));

        assertTrue(exception.getMessage().contains("total") && exception.getMessage().contains("cero o negativo"));
        verify(purchaseRepository, never()).save(any(Purchase.class));
    }

    // ======================================================================
    // TESTS - confirmPurchase
    // ======================================================================

    @Test
    @DisplayName("Debe confirmar una Compra exitosamente cambiando estado a CONFIRMED")
    void shouldConfirmPurchaseSuccessfully() {
        // Arrange
        Long purchaseId = 1L;
        String paymentReference = "REF123";

        User user = createUser(1L, "user@test.com", "John Doe", "1234567890",
                UserRole.PASSENGER, UserStatus.ACTIVE);

        Route route = createRoute(1L, "R001", "Ruta", "Orig", "Dest");
        Bus bus = createBus(1L, "ABC123", 40, BusStatus.IN_SERVICE);
        Trip trip = createTrip(1L, LocalDate.now().plusDays(1),
                OffsetDateTime.now().plusDays(1), OffsetDateTime.now().plusDays(1).plusHours(8),
                TripStatus.SCHEDULED, route, bus);

        Passenger passenger = createPassenger(1L, "John", "1234567890");
        Seat seat = createSeat(1L, 1, new BigDecimal("50000"), SeatType.STANDARD, SeatStatus.AVAILABLE, bus);
        Stop fromStop = createStop(1L, "Origen", 1, route);
        Stop toStop = createStop(2L, "Destino", 2, route);

        Ticket ticket = createTicket(1L, "1", new BigDecimal("50000"), TicketStatus.SOLD,
                null, trip, passenger, seat, fromStop, toStop);

        Purchase purchase = createPurchase(purchaseId, PaymentMethod.CASH,
                new BigDecimal("50000"), PaymentStatus.PENDING, user);
        purchase.addTicket(ticket);

        when(purchaseRepository.findPurchaseById(purchaseId)).thenReturn(Optional.of(purchase));
        doNothing().when(seatHoldService).validateActiveHolds(anyLong(), anyList(), anyLong());
        doNothing().when(ticketService).generateQrForTicket(anyLong());
        when(purchaseRepository.save(any(Purchase.class))).thenAnswer(inv -> inv.getArgument(0));
        doNothing().when(notificationHelper).sendPurchaseConfirmation(any(), any());

        // Act
        purchaseService.confirmPurchase(purchaseId, paymentReference);

        // Assert
        assertEquals(PaymentStatus.CONFIRMED, purchase.getPaymentStatus());
        verify(purchaseRepository, times(1)).save(purchase);
        verify(ticketService, times(1)).generateQrForTicket(1L);
        verify(notificationHelper, times(1)).sendPurchaseConfirmation(any(), any());
    }

    @Test
    @DisplayName("Debe lanzar excepcion cuando la Compra no está en estado PENDING para confirmar")
    void shouldThrowExceptionWhenPurchaseNotInPendingStatus() {
        // Arrange
        Long purchaseId = 1L;
        User user = createUser(1L, "user@test.com", "John Doe", "1234567890",
                UserRole.PASSENGER, UserStatus.ACTIVE);

        Purchase purchase = createPurchase(purchaseId, PaymentMethod.CASH,
                new BigDecimal("50000"), PaymentStatus.CONFIRMED, user); // Ya confirmada

        when(purchaseRepository.findPurchaseById(purchaseId)).thenReturn(Optional.of(purchase));

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> purchaseService.confirmPurchase(purchaseId, "REF123"));
        assertTrue(exception.getMessage().toLowerCase().contains("pending"));
        assertTrue(exception.getMessage().toLowerCase().contains("re-confirmar"));
        verify(purchaseRepository, never()).save(any(Purchase.class));
    }

    @Test
    @DisplayName("Debe lanzar excepcion cuando los SeatHolds han expirado durante confirmación")
    void shouldThrowExceptionWhenSeatHoldsExpired() {
        // Arrange
        Long purchaseId = 1L;
        User user = createUser(1L, "user@test.com", "John Doe", "1234567890",
                UserRole.PASSENGER, UserStatus.ACTIVE);

        Purchase purchase = createPurchase(purchaseId, PaymentMethod.CASH,
                new BigDecimal("50000"), PaymentStatus.PENDING, user);

        when(purchaseRepository.findPurchaseById(purchaseId)).thenReturn(Optional.of(purchase));
        doThrow(new IllegalStateException("SeatHolds expired"))
                .when(seatHoldService).validateActiveHolds(anyLong(), anyList(), anyLong());

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> purchaseService.confirmPurchase(purchaseId, "REF123"));

        assertTrue(exception.getMessage().toLowerCase().contains("expirado") ||
                exception.getMessage().toLowerCase().contains("reservado"));
        verify(purchaseRepository, never()).save(any(Purchase.class));
    }

    // ======================================================================
    // TESTS - cancelPurchase
    // ======================================================================

    @Test
    @DisplayName("Debe cancelar una Compra exitosamente")
    void shouldCancelPurchaseSuccessfully() {
        // Arrange
        Long purchaseId = 1L;
        User user = createUser(1L, "user@test.com", "John Doe", "1234567890",
                UserRole.PASSENGER, UserStatus.ACTIVE);

        Purchase purchase = createPurchase(purchaseId, PaymentMethod.CASH,
                new BigDecimal("50000"), PaymentStatus.PENDING, user);

        when(purchaseRepository.findPurchaseById(purchaseId)).thenReturn(Optional.of(purchase));
        doNothing().when(ticketService).releaseSeatsByPurchase(purchaseId);
        when(purchaseRepository.save(any(Purchase.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        purchaseService.cancelPurchase(purchaseId);

        // Assert
        assertEquals(PaymentStatus.CANCELLED, purchase.getPaymentStatus());
        verify(ticketService, times(1)).releaseSeatsByPurchase(purchaseId);
        verify(purchaseRepository, times(1)).save(purchase);
    }

    @Test
    @DisplayName("Debe lanzar excepcion cuando intenta cancelar una Compra ya confirmada")
    void shouldThrowExceptionWhenCancellingConfirmedPurchase() {
        // Arrange
        Long purchaseId = 1L;
        User user = createUser(1L, "user@test.com", "John Doe", "1234567890",
                UserRole.PASSENGER, UserStatus.ACTIVE);

        Purchase purchase = createPurchase(purchaseId, PaymentMethod.CASH,
                new BigDecimal("50000"), PaymentStatus.CONFIRMED, user);

        when(purchaseRepository.findPurchaseById(purchaseId)).thenReturn(Optional.of(purchase));

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> purchaseService.cancelPurchase(purchaseId));

        assertTrue(exception.getMessage().toLowerCase().contains("no se puede") &&
                exception.getMessage().toLowerCase().contains("confirmada"));
        verify(ticketService, never()).releaseSeatsByPurchase(purchaseId);
    }

    @Test
    @DisplayName("Debe lanzar excepcion cuando intenta cancelar una Compra ya cancelada")
    void shouldThrowExceptionWhenCancellingCancelledPurchase() {
        // Arrange
        Long purchaseId = 1L;
        User user = createUser(1L, "user@test.com", "John Doe", "1234567890",
                UserRole.PASSENGER, UserStatus.ACTIVE);

        Purchase purchase = createPurchase(purchaseId, PaymentMethod.CASH,
                new BigDecimal("50000"), PaymentStatus.CANCELLED, user);

        when(purchaseRepository.findPurchaseById(purchaseId)).thenReturn(Optional.of(purchase));

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> purchaseService.cancelPurchase(purchaseId));

        assertTrue(exception.getMessage().toLowerCase().contains("already") &&
                exception.getMessage().toLowerCase().contains("cancelled"));
        verify(ticketService, never()).releaseSeatsByPurchase(purchaseId);
    }

    // ======================================================================
    // TESTS - getPurchase
    // ======================================================================

    @Test
    @DisplayName("Debe obtener una Compra por ID exitosamente")
    void shouldGetPurchaseSuccessfully() {
        // Arrange
        Long purchaseId = 1L;
        User user = createUser(1L, "user@test.com", "John Doe", "1234567890",
                UserRole.PASSENGER, UserStatus.ACTIVE);

        Purchase purchase = createPurchase(purchaseId, PaymentMethod.CASH,
                new BigDecimal("50000"), PaymentStatus.CONFIRMED, user);

        when(purchaseRepository.findPurchaseById(purchaseId)).thenReturn(Optional.of(purchase));

        // Act
        PurchaseResponse response = purchaseService.getPurchase(purchaseId);

        // Assert
        assertNotNull(response);
        assertEquals(purchaseId, response.id());
        assertEquals(PaymentMethod.CASH, response.paymentMethod());
        verify(purchaseRepository, times(1)).findPurchaseById(purchaseId);
    }

    @Test
    @DisplayName("Debe lanzar excepcion cuando la Compra no existe")
    void shouldThrowExceptionWhenPurchaseNotFoundById() {
        // Arrange
        Long purchaseId = 999L;

        when(purchaseRepository.findPurchaseById(purchaseId)).thenReturn(Optional.empty());

        // Act & Assert
        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> purchaseService.getPurchase(purchaseId));

        assertTrue(exception.getMessage().toLowerCase().contains("compra") && exception.getMessage().toLowerCase().contains("no encontrada"));
    }

    // ======================================================================
    // TESTS - getPurchasesByUserId
    // ======================================================================

    @Test
    @DisplayName("Debe obtener todas las Compras de un Usuario exitosamente")
    void shouldGetPurchasesByUserIdSuccessfully() {
        // Arrange
        Long userId = 1L;
        User user = createUser(userId, "user@test.com", "John Doe", "1234567890",
                UserRole.PASSENGER, UserStatus.ACTIVE);

        Purchase purchase1 = createPurchase(1L, PaymentMethod.CASH,
                new BigDecimal("50000"), PaymentStatus.CONFIRMED, user);
        Purchase purchase2 = createPurchase(2L, PaymentMethod.CARD,
                new BigDecimal("60000"), PaymentStatus.PENDING, user);

        user.getPurchases().add(purchase1);
        user.getPurchases().add(purchase2);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // Act
        List<PurchaseResponse> responses = purchaseService.getPurchasesByUserId(userId);

        // Assert
        assertNotNull(responses);
        assertEquals(2, responses.size());
        verify(userRepository, times(1)).findById(userId);
    }

    @Test
    @DisplayName("Debe retornar lista vacía cuando el Usuario no tiene Compras")
    void shouldReturnEmptyListWhenUserHasNoPurchases() {
        // Arrange
        Long userId = 1L;
        User user = createUser(userId, "user@test.com", "John Doe", "1234567890",
                UserRole.PASSENGER, UserStatus.ACTIVE);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // Act
        List<PurchaseResponse> responses = purchaseService.getPurchasesByUserId(userId);

        // Assert
        assertNotNull(responses);
        assertTrue(responses.isEmpty());
    }

    @Test
    @DisplayName("Debe lanzar excepcion cuando el Usuario no existe")
    void shouldThrowExceptionWhenUserNotFoundForGetPurchases() {
        // Arrange
        Long userId = 999L;

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Act & Assert
        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> purchaseService.getPurchasesByUserId(userId));

        assertTrue(exception.getMessage().toLowerCase().contains("usuario") && exception.getMessage().toLowerCase().contains("no encontrado"));
    }

    // ======================================================================
    // TESTS - getPurchasesByDateRange
    // ======================================================================

    @Test
    @DisplayName("Debe obtener Compras dentro de un rango de fechas exitosamente")
    void shouldGetPurchasesByDateRangeSuccessfully() {
        // Arrange
        OffsetDateTime startDate = OffsetDateTime.now().minusDays(7);
        OffsetDateTime endDate = OffsetDateTime.now();

        User user = createUser(1L, "user@test.com", "John Doe", "1234567890",
                UserRole.PASSENGER, UserStatus.ACTIVE);

        Purchase purchase1 = createPurchase(1L, PaymentMethod.CASH,
                new BigDecimal("50000"), PaymentStatus.CONFIRMED, user);
        Purchase purchase2 = createPurchase(2L, PaymentMethod.CARD,
                new BigDecimal("60000"), PaymentStatus.CONFIRMED, user);

        when(purchaseRepository.findByDateRange(startDate, endDate))
                .thenReturn(List.of(purchase1, purchase2));

        // Act
        List<PurchaseResponse> responses = purchaseService.getPurchasesByDateRange(startDate, endDate);

        // Assert
        assertNotNull(responses);
        assertEquals(2, responses.size());
        verify(purchaseRepository, times(1)).findByDateRange(startDate, endDate);
    }

    @Test
    @DisplayName("Debe lanzar excepcion cuando la fecha de inicio es posterior a la fecha de fin")
    void shouldThrowExceptionWhenStartDateAfterEndDate() {
        // Arrange
        OffsetDateTime startDate = OffsetDateTime.now();
        OffsetDateTime endDate = OffsetDateTime.now().minusDays(7);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> purchaseService.getPurchasesByDateRange(startDate, endDate));

        assertTrue(exception.getMessage().toLowerCase().contains("inicio") &&
                exception.getMessage().toLowerCase().contains("anterior"));
    }

    @Test
    @DisplayName("Debe retornar lista vacía cuando no hay Compras en el rango de fechas")
    void shouldReturnEmptyListWhenNoPurchasesInDateRange() {
        // Arrange
        OffsetDateTime startDate = OffsetDateTime.now().minusDays(30);
        OffsetDateTime endDate = OffsetDateTime.now().minusDays(20);

        when(purchaseRepository.findByDateRange(startDate, endDate))
                .thenReturn(new ArrayList<>());

        // Act
        List<PurchaseResponse> responses = purchaseService.getPurchasesByDateRange(startDate, endDate);

        // Assert
        assertNotNull(responses);
        assertTrue(responses.isEmpty());
    }
}