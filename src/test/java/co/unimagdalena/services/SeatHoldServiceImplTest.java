package co.unimagdalena.services;

import co.unimagdalena.api.dto.SeatHoldDto.*;
import co.unimagdalena.domine.entities.*;
import co.unimagdalena.domine.repositories.SeatHoldRepository;
import co.unimagdalena.domine.repositories.TripRepository;
import co.unimagdalena.domine.repositories.UserRepository;
import co.unimagdalena.exception.NotFoundException;
import co.unimagdalena.services.impl.SeatHoldServiceImpl;
import co.unimagdalena.services.mapper.SeatHoldMapper;
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
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SeatHoldServiceImplTest {

    @Mock
    private SeatHoldRepository seatHoldRepository;

    @Mock
    private TripRepository tripRepository;

    @Mock
    private UserRepository userRepository;

    @Spy
    private SeatHoldMapper seatHoldMapper = Mappers.getMapper(SeatHoldMapper.class);

    @InjectMocks
    private SeatHoldServiceImpl seatHoldService;

    // ======================================================================
    // HELPER METHODS - Creación de entidades de prueba
    // ======================================================================

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

    private User createUser(Long id, String email, String fullName,
                            String phone, UserRole role, UserStatus status) {
        return User.builder()
                .id(id)
                .email(email)
                .fullName(fullName)
                .phone(phone)
                .role(role)
                .status(status)
                .passwordHash("hashedPassword")
                .createdAt(java.time.LocalDateTime.now())
                .seatHolds(new ArrayList<>())
                .purchases(new ArrayList<>())
                .build();
    }

    private Seat createSeat(Long id, Integer number, SeatType type,
                            SeatStatus status, Bus bus) {
        return Seat.builder()
                .id(id)
                .number(number)
                .type(type)
                .status(status)
                .price(BigDecimal.valueOf(50000))
                .bus(bus)
                .build();
    }

    private SeatHold createSeatHold(Long id, String seatNumber, Trip trip,
                                    User user, SeatHoldStatus status,
                                    OffsetDateTime expiresAt) {
        return SeatHold.builder()
                .id(id)
                .seatNumber(seatNumber)
                .trip(trip)
                .user(user)
                .status(status)
                .expiresAt(expiresAt)
                .build();
    }

    // ======================================================================
    // TESTS - createSeatHold
    // ======================================================================

    @Test
    @DisplayName("Debe crear exitosamente un SeatHold cuando todos los parámetros son válidos")
    void shouldCreateSeatHoldSuccessfully() {
        // Arrange
        Long tripId = 1L;
        Long userId = 1L;
        String seatNumber = "A1";

        Route route = createRoute(1L, "R001", "Ruta Norte", "Bogotá", "Medellín");
        Bus bus = createBus(1L, "ABC123", 40, BusStatus.AVAILABLE);
        Trip trip = createTrip(tripId, LocalDate.now().plusDays(1),
                OffsetDateTime.now().plusDays(1), OffsetDateTime.now().plusDays(1).plusHours(8),
                TripStatus.SCHEDULED, route, bus);

        User user = createUser(userId, "user@test.com", "John Doe",
                "1234567890", UserRole.PASSENGER, UserStatus.ACTIVE);

        SeatHoldCreateRequest request = new SeatHoldCreateRequest(
                seatNumber, null, SeatHoldStatus.HOLD, 1L, tripId, userId
        );

        when(tripRepository.findById(tripId)).thenReturn(Optional.of(trip));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(seatHoldRepository.findByTripId(tripId)).thenReturn(new ArrayList<>());
        when(seatHoldRepository.save(any(SeatHold.class))).thenAnswer(invocation -> {
            SeatHold saved = invocation.getArgument(0);
            saved.setId(1L);
            return saved;
        });

        // Act
        SeatHoldResponse response = seatHoldService.createSeatHold(request);

        // Assert
        assertNotNull(response);
        assertEquals(seatNumber, response.seatNumber());
        assertEquals(tripId, response.trip().id());
        assertEquals(userId, response.user().id());
        assertEquals(SeatHoldStatus.HOLD, response.status());
        verify(seatHoldRepository, times(1)).save(any(SeatHold.class));
        verify(tripRepository, times(1)).findById(tripId);
        verify(userRepository, times(1)).findById(userId);
    }

    @Test
    @DisplayName("Debe lanzar NotFoundException cuando el viaje no existe")
    void shouldThrowExceptionWhenTripNotFound() {
        // Arrange
        Long tripId = 999L;
        Long userId = 1L;
        String seatNumber = "A1";

        SeatHoldCreateRequest request = new SeatHoldCreateRequest(
                seatNumber, null, SeatHoldStatus.HOLD, 1L, tripId, userId
        );

        when(tripRepository.findById(tripId)).thenReturn(Optional.empty());

        // Act & Assert
        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> seatHoldService.createSeatHold(request));

        assertEquals("Viaje no encontrado", exception.getMessage());
        verify(userRepository, never()).findById(anyLong());
        verify(seatHoldRepository, never()).save(any(SeatHold.class));
    }

    @Test
    @DisplayName("Debe lanzar NotFoundException cuando el usuario no existe")
    void shouldThrowExceptionWhenUserNotFound() {
        // Arrange
        Long tripId = 1L;
        Long userId = 999L;
        String seatNumber = "A1";

        Route route = createRoute(1L, "R001", "Ruta Norte", "Bogotá", "Medellín");
        Bus bus = createBus(1L, "ABC123", 40, BusStatus.AVAILABLE);
        Trip trip = createTrip(tripId, LocalDate.now().plusDays(1),
                OffsetDateTime.now().plusDays(1), OffsetDateTime.now().plusDays(1).plusHours(8),
                TripStatus.SCHEDULED, route, bus);

        SeatHoldCreateRequest request = new SeatHoldCreateRequest(
                seatNumber, null, SeatHoldStatus.HOLD, 1L, tripId, userId
        );

        when(tripRepository.findById(tripId)).thenReturn(Optional.of(trip));
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Act & Assert
        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> seatHoldService.createSeatHold(request));

        assertEquals("Usuario no encontrado", exception.getMessage());
        verify(seatHoldRepository, never()).save(any(SeatHold.class));
    }

    @Test
    @DisplayName("Debe lanzar IllegalStateException cuando el asiento ya está en reserva")
    void shouldThrowExceptionWhenSeatAlreadyOnHold() {
        // Arrange
        Long tripId = 1L;
        Long userId = 1L;
        Long userId2 = 2L;
        String seatNumber = "A1";

        Route route = createRoute(1L, "R001", "Ruta Norte", "Bogotá", "Medellín");
        Bus bus = createBus(1L, "ABC123", 40, BusStatus.AVAILABLE);
        Trip trip = createTrip(tripId, LocalDate.now().plusDays(1),
                OffsetDateTime.now().plusDays(1), OffsetDateTime.now().plusDays(1).plusHours(8),
                TripStatus.SCHEDULED, route, bus);

        User user = createUser(userId, "user@test.com", "John Doe",
                "1234567890", UserRole.PASSENGER, UserStatus.ACTIVE);

        User user2 = createUser(userId2, "user2@test.com", "Jane Doe",
                "0987654321", UserRole.PASSENGER, UserStatus.ACTIVE);

        // SeatHold existente, activo y no expirado
        SeatHold existingSeatHold = createSeatHold(1L, seatNumber, trip, user,
                SeatHoldStatus.HOLD, OffsetDateTime.now().plusMinutes(10));

        SeatHoldCreateRequest request = new SeatHoldCreateRequest(
                seatNumber, null, SeatHoldStatus.HOLD, 1L, tripId, userId2
        );

        when(tripRepository.findById(tripId)).thenReturn(Optional.of(trip));
        when(userRepository.findById(userId2)).thenReturn(Optional.of(user2));
        when(seatHoldRepository.findByTripId(tripId)).thenReturn(List.of(existingSeatHold));

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> seatHoldService.createSeatHold(request));

        assertTrue(exception.getMessage().contains("ya está reservado"));
        verify(seatHoldRepository, never()).save(any(SeatHold.class));
    }

    // ======================================================================
    // TESTS - releaseSeatHold
    // ======================================================================

    @Test
    @DisplayName("Debe liberar exitosamente una reserva de asiento")
    void shouldReleaseSeatHoldSuccessfully() {
        // Arrange
        Long holdId = 1L;
        Route route = createRoute(1L, "R001", "Ruta Norte", "Bogotá", "Medellín");
        Bus bus = createBus(1L, "ABC123", 40, BusStatus.AVAILABLE);
        Trip trip = createTrip(1L, LocalDate.now().plusDays(1),
                OffsetDateTime.now().plusDays(1), OffsetDateTime.now().plusDays(1).plusHours(8),
                TripStatus.SCHEDULED, route, bus);
        User user = createUser(1L, "user@test.com", "John Doe",
                "1234567890", UserRole.PASSENGER, UserStatus.ACTIVE);

        SeatHold seatHold = createSeatHold(holdId, "A1", trip, user,
                SeatHoldStatus.HOLD, OffsetDateTime.now().plusMinutes(10));

        when(seatHoldRepository.findById(holdId)).thenReturn(Optional.of(seatHold));
        when(seatHoldRepository.save(any(SeatHold.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        seatHoldService.releaseSeatHold(holdId);

        // Assert
        assertEquals(SeatHoldStatus.EXPIRED, seatHold.getStatus());
        verify(seatHoldRepository, times(1)).save(seatHold);
        verify(seatHoldRepository, times(1)).findById(holdId);
    }

    @Test
    @DisplayName("Debe lanzar NotFoundException cuando la reserva no existe")
    void shouldThrowExceptionWhenSeatHoldNotFoundForRelease() {
        // Arrange
        Long holdId = 999L;

        when(seatHoldRepository.findById(holdId)).thenReturn(Optional.empty());

        // Act & Assert
        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> seatHoldService.releaseSeatHold(holdId));

        assertEquals("Reserva de asiento no encontrada", exception.getMessage());
        verify(seatHoldRepository, never()).save(any(SeatHold.class));
    }

    @Test
    @DisplayName("Debe retornar sin hacer nada cuando la reserva ya está expirada")
    void shouldReturnWhenSeatHoldAlreadyExpired() {
        // Arrange
        Long holdId = 1L;
        Route route = createRoute(1L, "R001", "Ruta Norte", "Bogotá", "Medellín");
        Bus bus = createBus(1L, "ABC123", 40, BusStatus.AVAILABLE);
        Trip trip = createTrip(1L, LocalDate.now().plusDays(1),
                OffsetDateTime.now().plusDays(1), OffsetDateTime.now().plusDays(1).plusHours(8),
                TripStatus.SCHEDULED, route, bus);
        User user = createUser(1L, "user@test.com", "John Doe",
                "1234567890", UserRole.PASSENGER, UserStatus.ACTIVE);

        SeatHold expiredSeatHold = createSeatHold(holdId, "A1", trip, user,
                SeatHoldStatus.EXPIRED, OffsetDateTime.now().minusMinutes(5));

        when(seatHoldRepository.findById(holdId)).thenReturn(Optional.of(expiredSeatHold));

        // Act
        seatHoldService.releaseSeatHold(holdId);

        // Assert
        verify(seatHoldRepository, never()).save(any(SeatHold.class));
    }

    // ======================================================================
    // TESTS - expireOldHolds
    // ======================================================================

    @Test
    @DisplayName("Debe marcar como expiradas las reservas vencidas")
    void shouldExpireOldHoldsSuccessfully() {
        // Arrange
        Route route = createRoute(1L, "R001", "Ruta Norte", "Bogotá", "Medellín");
        Bus bus = createBus(1L, "ABC123", 40, BusStatus.AVAILABLE);
        Trip trip = createTrip(1L, LocalDate.now().plusDays(1),
                OffsetDateTime.now().plusDays(1), OffsetDateTime.now().plusDays(1).plusHours(8),
                TripStatus.SCHEDULED, route, bus);
        User user = createUser(1L, "user@test.com", "John Doe",
                "1234567890", UserRole.PASSENGER, UserStatus.ACTIVE);

        SeatHold expiredHold1 = createSeatHold(1L, "A1", trip, user,
                SeatHoldStatus.HOLD, OffsetDateTime.now().minusMinutes(20));
        SeatHold expiredHold2 = createSeatHold(2L, "A2", trip, user,
                SeatHoldStatus.HOLD, OffsetDateTime.now().minusMinutes(15));

        when(seatHoldRepository.findExpiredHolds()).thenReturn(List.of(expiredHold1, expiredHold2));
        when(seatHoldRepository.saveAll(anyList())).thenReturn(List.of(expiredHold1, expiredHold2));

        // Act
        int expiredCount = seatHoldService.expireOldHolds();

        // Assert
        assertEquals(2, expiredCount);
        assertEquals(SeatHoldStatus.EXPIRED, expiredHold1.getStatus());
        assertEquals(SeatHoldStatus.EXPIRED, expiredHold2.getStatus());
        verify(seatHoldRepository, times(1)).findExpiredHolds();
        verify(seatHoldRepository, times(1)).saveAll(anyList());
    }

    @Test
    @DisplayName("Debe retornar 0 cuando no hay reservas expiradas")
    void shouldReturnZeroWhenNoExpiredHolds() {
        // Arrange
        when(seatHoldRepository.findExpiredHolds()).thenReturn(new ArrayList<>());

        // Act
        int expiredCount = seatHoldService.expireOldHolds();

        // Assert
        assertEquals(0, expiredCount);
        verify(seatHoldRepository, times(1)).findExpiredHolds();
        verify(seatHoldRepository, never()).saveAll(anyList());
    }

    // ======================================================================
    // TESTS - deleteExpiredHolds
    // ======================================================================

    @Test
    @DisplayName("Debe eliminar las reservas expiradas exitosamente")
    void shouldDeleteExpiredHoldsSuccessfully() {
        // Arrange
        Route route = createRoute(1L, "R001", "Ruta Norte", "Bogotá", "Medellín");
        Bus bus = createBus(1L, "ABC123", 40, BusStatus.AVAILABLE);
        Trip trip = createTrip(1L, LocalDate.now().plusDays(1),
                OffsetDateTime.now().plusDays(1), OffsetDateTime.now().plusDays(1).plusHours(8),
                TripStatus.SCHEDULED, route, bus);
        User user = createUser(1L, "user@test.com", "John Doe",
                "1234567890", UserRole.PASSENGER, UserStatus.ACTIVE);

        SeatHold expiredHold1 = createSeatHold(1L, "A1", trip, user,
                SeatHoldStatus.EXPIRED, OffsetDateTime.now().minusMinutes(20));
        SeatHold expiredHold2 = createSeatHold(2L, "A2", trip, user,
                SeatHoldStatus.EXPIRED, OffsetDateTime.now().minusMinutes(15));
        SeatHold activeHold = createSeatHold(3L, "A3", trip, user,
                SeatHoldStatus.HOLD, OffsetDateTime.now().plusMinutes(10));

        when(seatHoldRepository.findAll()).thenReturn(List.of(expiredHold1, expiredHold2, activeHold));
        doNothing().when(seatHoldRepository).deleteAll(anyList());

        // Act
        int deletedCount = seatHoldService.deleteExpiredHolds();

        // Assert
        assertEquals(2, deletedCount);
        verify(seatHoldRepository, times(1)).findAll();
        verify(seatHoldRepository, times(1)).deleteAll(anyList());
    }

    @Test
    @DisplayName("Debe retornar 0 cuando no hay reservas expiradas para eliminar")
    void shouldReturnZeroWhenNoExpiredHoldsToDelete() {
        // Arrange
        Route route = createRoute(1L, "R001", "Ruta Norte", "Bogotá", "Medellín");
        Bus bus = createBus(1L, "ABC123", 40, BusStatus.AVAILABLE);
        Trip trip = createTrip(1L, LocalDate.now().plusDays(1),
                OffsetDateTime.now().plusDays(1), OffsetDateTime.now().plusDays(1).plusHours(8),
                TripStatus.SCHEDULED, route, bus);
        User user = createUser(1L, "user@test.com", "John Doe",
                "1234567890", UserRole.PASSENGER, UserStatus.ACTIVE);

        // Solo reservas activas, sin expiradas
        SeatHold activeHold1 = createSeatHold(1L, "A1", trip, user,
                SeatHoldStatus.HOLD, OffsetDateTime.now().plusMinutes(10));
        SeatHold activeHold2 = createSeatHold(2L, "A2", trip, user,
                SeatHoldStatus.HOLD, OffsetDateTime.now().plusMinutes(10));

        when(seatHoldRepository.findAll()).thenReturn(List.of(activeHold1, activeHold2));

        // Act
        int deletedCount = seatHoldService.deleteExpiredHolds();

        // Assert
        assertEquals(0, deletedCount);
        verify(seatHoldRepository, times(1)).findAll();
        verify(seatHoldRepository, never()).deleteAll(anyList());
    }

    // ======================================================================
    // TESTS - isSeatOnHold
    // ======================================================================

    @Test
    @DisplayName("Debe retornar true cuando el asiento está en reserva activa y no expirada")
    void shouldReturnTrueWhenSeatIsOnHold() {
        // Arrange
        Long tripId = 1L;
        String seatNumber = "A1";

        Route route = createRoute(1L, "R001", "Ruta Norte", "Bogotá", "Medellín");
        Bus bus = createBus(1L, "ABC123", 40, BusStatus.AVAILABLE);
        Trip trip = createTrip(tripId, LocalDate.now().plusDays(1),
                OffsetDateTime.now().plusDays(1), OffsetDateTime.now().plusDays(1).plusHours(8),
                TripStatus.SCHEDULED, route, bus);
        User user = createUser(1L, "user@test.com", "John Doe",
                "1234567890", UserRole.PASSENGER, UserStatus.ACTIVE);

        SeatHold activeSeatHold = createSeatHold(1L, seatNumber, trip, user,
                SeatHoldStatus.HOLD, OffsetDateTime.now().plusMinutes(10));

        when(seatHoldRepository.findByTripId(tripId)).thenReturn(List.of(activeSeatHold));

        // Act
        boolean isOnHold = seatHoldService.isSeatOnHold(tripId, seatNumber);

        // Assert
        assertTrue(isOnHold);
        verify(seatHoldRepository, times(1)).findByTripId(tripId);
    }

    @Test
    @DisplayName("Debe retornar false cuando el asiento no está en reserva")
    void shouldReturnFalseWhenSeatNotOnHold() {
        // Arrange
        Long tripId = 1L;
        String seatNumber = "A1";

        when(seatHoldRepository.findByTripId(tripId)).thenReturn(new ArrayList<>());

        // Act
        boolean isOnHold = seatHoldService.isSeatOnHold(tripId, seatNumber);

        // Assert
        assertFalse(isOnHold);
        verify(seatHoldRepository, times(1)).findByTripId(tripId);
    }

    @Test
    @DisplayName("Debe retornar false cuando la reserva está expirada")
    void shouldReturnFalseWhenSeatHoldExpired() {
        // Arrange
        Long tripId = 1L;
        String seatNumber = "A1";

        Route route = createRoute(1L, "R001", "Ruta Norte", "Bogotá", "Medellín");
        Bus bus = createBus(1L, "ABC123", 40, BusStatus.AVAILABLE);
        Trip trip = createTrip(tripId, LocalDate.now().plusDays(1),
                OffsetDateTime.now().plusDays(1), OffsetDateTime.now().plusDays(1).plusHours(8),
                TripStatus.SCHEDULED, route, bus);
        User user = createUser(1L, "user@test.com", "John Doe",
                "1234567890", UserRole.PASSENGER, UserStatus.ACTIVE);

        SeatHold expiredSeatHold = createSeatHold(1L, seatNumber, trip, user,
                SeatHoldStatus.EXPIRED, OffsetDateTime.now().minusMinutes(10));

        when(seatHoldRepository.findByTripId(tripId)).thenReturn(List.of(expiredSeatHold));

        // Act
        boolean isOnHold = seatHoldService.isSeatOnHold(tripId, seatNumber);

        // Assert
        assertFalse(isOnHold);
        verify(seatHoldRepository, times(1)).findByTripId(tripId);
    }

    // ======================================================================
    // TESTS - validateActiveHolds
    // ======================================================================

    @Test
    @DisplayName("Debe validar exitosamente las reservas activas del usuario")
    void shouldValidateActiveHoldsSuccessfully() {
        // Arrange
        Long tripId = 1L;
        Long userId = 1L;
        List<String> seatNumbers = List.of("A1", "A2");

        Route route = createRoute(1L, "R001", "Ruta Norte", "Bogotá", "Medellín");
        Bus bus = createBus(1L, "ABC123", 40, BusStatus.AVAILABLE);
        Trip trip = createTrip(tripId, LocalDate.now().plusDays(1),
                OffsetDateTime.now().plusDays(1), OffsetDateTime.now().plusDays(1).plusHours(8),
                TripStatus.SCHEDULED, route, bus);
        User user = createUser(userId, "user@test.com", "John Doe",
                "1234567890", UserRole.PASSENGER, UserStatus.ACTIVE);

        SeatHold hold1 = createSeatHold(1L, "A1", trip, user,
                SeatHoldStatus.HOLD, OffsetDateTime.now().plusMinutes(10));
        SeatHold hold2 = createSeatHold(2L, "A2", trip, user,
                SeatHoldStatus.HOLD, OffsetDateTime.now().plusMinutes(10));

        when(seatHoldRepository.findByTripId(tripId)).thenReturn(List.of(hold1, hold2));

        // Act & Assert - no debe lanzar excepción
        assertDoesNotThrow(() -> seatHoldService.validateActiveHolds(tripId, seatNumbers, userId));
        verify(seatHoldRepository, times(1)).findByTripId(tripId);
    }

    @Test
    @DisplayName("Debe lanzar excepcion cuando falta alguna reserva activa")
    void shouldThrowExceptionWhenMissingSeatHold() {
        // Arrange
        Long tripId = 1L;
        Long userId = 1L;
        List<String> seatNumbers = List.of("A1", "A2", "A3");

        Route route = createRoute(1L, "R001", "Ruta Norte", "Bogotá", "Medellín");
        Bus bus = createBus(1L, "ABC123", 40, BusStatus.AVAILABLE);
        Trip trip = createTrip(tripId, LocalDate.now().plusDays(1),
                OffsetDateTime.now().plusDays(1), OffsetDateTime.now().plusDays(1).plusHours(8),
                TripStatus.SCHEDULED, route, bus);
        User user = createUser(userId, "user@test.com", "John Doe",
                "1234567890", UserRole.PASSENGER, UserStatus.ACTIVE);

        // Solo dos reservas activas para tres asientos solicitados
        SeatHold hold1 = createSeatHold(1L, "A1", trip, user,
                SeatHoldStatus.HOLD, OffsetDateTime.now().plusMinutes(10));
        SeatHold hold2 = createSeatHold(2L, "A2", trip, user,
                SeatHoldStatus.HOLD, OffsetDateTime.now().plusMinutes(10));

        when(seatHoldRepository.findByTripId(tripId)).thenReturn(List.of(hold1, hold2));

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> seatHoldService.validateActiveHolds(tripId, seatNumbers, userId));

        assertTrue(exception.getMessage().contains("expirado o no existe"));
        verify(seatHoldRepository, times(1)).findByTripId(tripId);
    }

    @Test
    @DisplayName("Debe lanzar excepcion cuando la reserva no pertenece al usuario")
    void shouldThrowExceptionWhenSeatHoldNotBelongsToUser() {
        // Arrange
        Long tripId = 1L;
        Long userId = 1L;
        Long otherUserId = 2L;
        List<String> seatNumbers = List.of("A1");

        Route route = createRoute(1L, "R001", "Ruta Norte", "Bogotá", "Medellín");
        Bus bus = createBus(1L, "ABC123", 40, BusStatus.AVAILABLE);
        Trip trip = createTrip(tripId, LocalDate.now().plusDays(1),
                OffsetDateTime.now().plusDays(1), OffsetDateTime.now().plusDays(1).plusHours(8),
                TripStatus.SCHEDULED, route, bus);
        User user = createUser(userId, "user@test.com", "John Doe",
                "1234567890", UserRole.PASSENGER, UserStatus.ACTIVE);
        User otherUser = createUser(otherUserId, "other@test.com", "Jane Doe",
                "0987654321", UserRole.PASSENGER, UserStatus.ACTIVE);

        // Reserva pertenece a otro usuario
        SeatHold holdOfOtherUser = createSeatHold(1L, "A1", trip, otherUser,
                SeatHoldStatus.HOLD, OffsetDateTime.now().plusMinutes(10));

        when(seatHoldRepository.findByTripId(tripId)).thenReturn(List.of(holdOfOtherUser));

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> seatHoldService.validateActiveHolds(tripId, seatNumbers, userId));

        assertTrue(exception.getMessage().contains("no pertenece al usuario"));
        verify(seatHoldRepository, times(1)).findByTripId(tripId);
    }

    // ======================================================================
    // TESTS - getActiveHoldsByTrip
    // ======================================================================

    @Test
    @DisplayName("Debe obtener exitosamente las reservas activas de un viaje")
    void shouldGetActiveHoldsByTripSuccessfully() {
        // Arrange
        Long tripId = 1L;

        Route route = createRoute(1L, "R001", "Ruta Norte", "Bogotá", "Medellín");
        Bus bus = createBus(1L, "ABC123", 40, BusStatus.AVAILABLE);
        Trip trip = createTrip(tripId, LocalDate.now().plusDays(1),
                OffsetDateTime.now().plusDays(1), OffsetDateTime.now().plusDays(1).plusHours(8),
                TripStatus.SCHEDULED, route, bus);
        User user = createUser(1L, "user@test.com", "John Doe",
                "1234567890", UserRole.PASSENGER, UserStatus.ACTIVE);

        SeatHold activeHold1 = createSeatHold(1L, "A1", trip, user,
                SeatHoldStatus.HOLD, OffsetDateTime.now().plusMinutes(10));
        SeatHold activeHold2 = createSeatHold(2L, "A2", trip, user,
                SeatHoldStatus.HOLD, OffsetDateTime.now().plusMinutes(10));
        SeatHold expiredHold = createSeatHold(3L, "A3", trip, user,
                SeatHoldStatus.EXPIRED, OffsetDateTime.now().minusMinutes(10));

        when(seatHoldRepository.findByTripId(tripId))
                .thenReturn(List.of(activeHold1, activeHold2, expiredHold));

        // Act
        List<SeatHoldResponse> responses = seatHoldService.getActiveHoldsByTrip(tripId);

        // Assert
        assertNotNull(responses);
        assertEquals(2, responses.size());
        assertTrue(responses.stream().allMatch(r -> r.status() == SeatHoldStatus.HOLD));
        verify(seatHoldRepository, times(1)).findByTripId(tripId);
    }

    @Test
    @DisplayName("Debe retornar lista vacía cuando no hay reservas activas")
    void shouldReturnEmptyListWhenNoActiveHoldsForTrip() {
        // Arrange
        Long tripId = 1L;

        when(seatHoldRepository.findByTripId(tripId)).thenReturn(new ArrayList<>());

        // Act
        List<SeatHoldResponse> responses = seatHoldService.getActiveHoldsByTrip(tripId);

        // Assert
        assertNotNull(responses);
        assertTrue(responses.isEmpty());
        verify(seatHoldRepository, times(1)).findByTripId(tripId);
    }

    // ======================================================================
    // TESTS - getActiveHoldsByTripAndUser
    // ======================================================================

    @Test
    @DisplayName("Debe obtener exitosamente las reservas activas de un usuario en un viaje")
    void shouldGetActiveHoldsByTripAndUserSuccessfully() {
        // Arrange
        Long tripId = 1L;
        Long userId = 1L;
        Long otherUserId = 2L;

        Route route = createRoute(1L, "R001", "Ruta Norte", "Bogotá", "Medellín");
        Bus bus = createBus(1L, "ABC123", 40, BusStatus.AVAILABLE);
        Trip trip = createTrip(tripId, LocalDate.now().plusDays(1),
                OffsetDateTime.now().plusDays(1), OffsetDateTime.now().plusDays(1).plusHours(8),
                TripStatus.SCHEDULED, route, bus);
        User user = createUser(userId, "user@test.com", "John Doe",
                "1234567890", UserRole.PASSENGER, UserStatus.ACTIVE);
        User otherUser = createUser(otherUserId, "other@test.com", "Jane Doe",
                "0987654321", UserRole.PASSENGER, UserStatus.ACTIVE);

        // Reservas del usuario
        SeatHold holdUser1 = createSeatHold(1L, "A1", trip, user,
                SeatHoldStatus.HOLD, OffsetDateTime.now().plusMinutes(10));
        SeatHold holdUser2 = createSeatHold(2L, "A2", trip, user,
                SeatHoldStatus.HOLD, OffsetDateTime.now().plusMinutes(10));

        // Reservas de otro usuario
        SeatHold holdOtherUser = createSeatHold(3L, "A3", trip, otherUser,
                SeatHoldStatus.HOLD, OffsetDateTime.now().plusMinutes(10));

        when(seatHoldRepository.findByTripId(tripId))
                .thenReturn(List.of(holdUser1, holdUser2, holdOtherUser));

        // Act
        List<SeatHoldResponse> responses = seatHoldService.getActiveHoldsByTripAndUser(tripId, userId);

        // Assert
        assertNotNull(responses);
        assertEquals(2, responses.size());
        assertTrue(responses.stream().allMatch(r -> r.user().id().equals(userId)));
        verify(seatHoldRepository, times(1)).findByTripId(tripId);
    }

    @Test
    @DisplayName("Debe retornar lista vacía cuando el usuario no tiene reservas en el viaje")
    void shouldReturnEmptyListWhenUserHasNoActiveHolds() {
        // Arrange
        Long tripId = 1L;
        Long userId = 1L;

        Route route = createRoute(1L, "R001", "Ruta Norte", "Bogotá", "Medellín");
        Bus bus = createBus(1L, "ABC123", 40, BusStatus.AVAILABLE);
        Trip trip = createTrip(tripId, LocalDate.now().plusDays(1),
                OffsetDateTime.now().plusDays(1), OffsetDateTime.now().plusDays(1).plusHours(8),
                TripStatus.SCHEDULED, route, bus);
        User otherUser = createUser(2L, "other@test.com", "Jane Doe",
                "0987654321", UserRole.PASSENGER, UserStatus.ACTIVE);

        SeatHold holdOtherUser = createSeatHold(1L, "A1", trip, otherUser,
                SeatHoldStatus.HOLD, OffsetDateTime.now().plusMinutes(10));

        when(seatHoldRepository.findByTripId(tripId)).thenReturn(List.of(holdOtherUser));

        // Act
        List<SeatHoldResponse> responses = seatHoldService.getActiveHoldsByTripAndUser(tripId, userId);

        // Assert
        assertNotNull(responses);
        assertTrue(responses.isEmpty());
        verify(seatHoldRepository, times(1)).findByTripId(tripId);
    }

    // ======================================================================
    // TESTS - calculateExpirationTime
    // ======================================================================

    @Test
    @DisplayName("Debe calcular correctamente el tiempo de expiración (15 minutos después)")
    void shouldCalculateExpirationTimeCorrectly() {
        // Act
        OffsetDateTime expirationTime = seatHoldService.calculateExpirationTime();
        OffsetDateTime now = OffsetDateTime.now();

        // Assert
        assertNotNull(expirationTime);
        long minutesDifference = java.time.temporal.ChronoUnit.MINUTES.between(now, expirationTime);
        assertTrue(minutesDifference >= 14 && minutesDifference <= 16); // Tolerancia de 1 minuto
    }

    // ======================================================================
    // TESTS - hasOverlappingHold
    // ======================================================================

    @Test
    @DisplayName("Debe retornar true cuando existe overlapping en la reserva")
    void shouldReturnTrueWhenHasOverlappingHold() {
        // Arrange
        Long tripId = 1L;
        String seatNumber = "A1";
        Integer fromStopOrder = 1;
        Integer toStopOrder = 5;

        Route route = createRoute(1L, "R001", "Ruta Norte", "Bogotá", "Medellín");
        Bus bus = createBus(1L, "ABC123", 40, BusStatus.AVAILABLE);
        Trip trip = createTrip(tripId, LocalDate.now().plusDays(1),
                OffsetDateTime.now().plusDays(1), OffsetDateTime.now().plusDays(1).plusHours(8),
                TripStatus.SCHEDULED, route, bus);
        User user = createUser(1L, "user@test.com", "John Doe",
                "1234567890", UserRole.PASSENGER, UserStatus.ACTIVE);

        SeatHold seatHold = createSeatHold(1L, seatNumber, trip, user,
                SeatHoldStatus.HOLD, OffsetDateTime.now().plusMinutes(10));

        when(seatHoldRepository.findByTripId(tripId)).thenReturn(List.of(seatHold));

        // Act
        boolean hasOverlap = seatHoldService.hasOverlappingHold(tripId, seatNumber, fromStopOrder, toStopOrder);

        // Assert
        assertTrue(hasOverlap);
        verify(seatHoldRepository, times(1)).findByTripId(tripId);
    }

    @Test
    @DisplayName("Debe retornar false cuando no existe overlapping")
    void shouldReturnFalseWhenNoOverlappingHold() {
        // Arrange
        Long tripId = 1L;
        String seatNumber = "A1";
        Integer fromStopOrder = 1;
        Integer toStopOrder = 5;

        when(seatHoldRepository.findByTripId(tripId)).thenReturn(new ArrayList<>());

        // Act
        boolean hasOverlap = seatHoldService.hasOverlappingHold(tripId, seatNumber, fromStopOrder, toStopOrder);

        // Assert
        assertFalse(hasOverlap);
        verify(seatHoldRepository, times(1)).findByTripId(tripId);
    }

    // ======================================================================
    // TESTS - getHoldById
    // ======================================================================

    @Test
    @DisplayName("Debe obtener exitosamente una reserva por su ID")
    void shouldGetHoldByIdSuccessfully() {
        // Arrange
        Long holdId = 1L;

        Route route = createRoute(1L, "R001", "Ruta Norte", "Bogotá", "Medellín");
        Bus bus = createBus(1L, "ABC123", 40, BusStatus.AVAILABLE);
        Trip trip = createTrip(1L, LocalDate.now().plusDays(1),
                OffsetDateTime.now().plusDays(1), OffsetDateTime.now().plusDays(1).plusHours(8),
                TripStatus.SCHEDULED, route, bus);
        User user = createUser(1L, "user@test.com", "John Doe",
                "1234567890", UserRole.PASSENGER, UserStatus.ACTIVE);

        SeatHold seatHold = createSeatHold(holdId, "A1", trip, user,
                SeatHoldStatus.HOLD, OffsetDateTime.now().plusMinutes(10));

        when(seatHoldRepository.findById(holdId)).thenReturn(Optional.of(seatHold));

        // Act
        SeatHoldResponse response = seatHoldService.getHoldById(holdId);

        // Assert
        assertNotNull(response);
        assertEquals(holdId, response.id());
        assertEquals("A1", response.seatNumber());
        verify(seatHoldRepository, times(1)).findById(holdId);
    }

    @Test
    @DisplayName("Debe lanzar NotFoundException cuando la reserva no existe")
    void shouldThrowExceptionWhenHoldNotFoundById() {
        // Arrange
        Long holdId = 999L;

        when(seatHoldRepository.findById(holdId)).thenReturn(Optional.empty());

        // Act & Assert
        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> seatHoldService.getHoldById(holdId));

        assertEquals("Reserva de asiento no encontrada", exception.getMessage());
        verify(seatHoldRepository, times(1)).findById(holdId);
    }

    // ======================================================================
    // TESTS - getHoldsByUser
    // ======================================================================

    @Test
    @DisplayName("Debe obtener exitosamente todas las reservas del usuario")
    void shouldGetHoldsByUserSuccessfully() {
        // Arrange
        Long userId = 1L;

        Route route = createRoute(1L, "R001", "Ruta Norte", "Bogotá", "Medellín");
        Bus bus = createBus(1L, "ABC123", 40, BusStatus.AVAILABLE);
        Trip trip1 = createTrip(1L, LocalDate.now().plusDays(1),
                OffsetDateTime.now().plusDays(1), OffsetDateTime.now().plusDays(1).plusHours(8),
                TripStatus.SCHEDULED, route, bus);
        Trip trip2 = createTrip(2L, LocalDate.now().plusDays(2),
                OffsetDateTime.now().plusDays(2), OffsetDateTime.now().plusDays(2).plusHours(8),
                TripStatus.SCHEDULED, route, bus);

        User user = createUser(userId, "user@test.com", "John Doe",
                "1234567890", UserRole.PASSENGER, UserStatus.ACTIVE);

        SeatHold hold1 = createSeatHold(1L, "A1", trip1, user,
                SeatHoldStatus.HOLD, OffsetDateTime.now().plusMinutes(10));
        SeatHold hold2 = createSeatHold(2L, "B1", trip2, user,
                SeatHoldStatus.HOLD, OffsetDateTime.now().plusMinutes(10));

        when(seatHoldRepository.findByUserId(userId)).thenReturn(List.of(hold1, hold2));

        // Act
        List<SeatHoldResponse> responses = seatHoldService.getHoldsByUser(userId);

        // Assert
        assertNotNull(responses);
        assertEquals(2, responses.size());
        assertTrue(responses.stream().allMatch(r -> r.user().id().equals(userId)));
        verify(seatHoldRepository, times(1)).findByUserId(userId);
    }

    @Test
    @DisplayName("Debe retornar lista vacía cuando el usuario no tiene reservas")
    void shouldReturnEmptyListWhenUserHasNoHolds() {
        // Arrange
        Long userId = 1L;

        when(seatHoldRepository.findByUserId(userId)).thenReturn(new ArrayList<>());

        // Act
        List<SeatHoldResponse> responses = seatHoldService.getHoldsByUser(userId);

        // Assert
        assertNotNull(responses);
        assertTrue(responses.isEmpty());
        verify(seatHoldRepository, times(1)).findByUserId(userId);
    }
<<<<<<< Updated upstream
}
=======
}
>>>>>>> Stashed changes
