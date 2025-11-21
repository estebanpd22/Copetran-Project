package co.unimagdalena.services;

import co.unimagdalena.api.dto.AssignmentDto.*;
import co.unimagdalena.domine.entities.*;
import co.unimagdalena.domine.repositories.AssignmentRepository;
import co.unimagdalena.domine.repositories.BusRepository;
import co.unimagdalena.domine.repositories.TripRepository;
import co.unimagdalena.domine.repositories.UserRepository;
import co.unimagdalena.exception.NotFoundException;
import co.unimagdalena.services.impl.AssignmentServiceImpl;
import co.unimagdalena.services.mapper.AssignmentMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AssignmentServiceImplTest {

    @Mock
    private AssignmentRepository assignmentRepository;

    @Mock
    private TripRepository tripRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private BusRepository busRepository;

    @Spy
    private AssignmentMapper assignmentMapper = Mappers.getMapper(AssignmentMapper.class);

    @InjectMocks
    private AssignmentServiceImpl assignmentService;

    // ======================================================================
    // HELPER METHODS - Creación de entidades de prueba
    // ======================================================================

    private User createUser(Long id, String email, String name,
                            String phone, UserRole role, UserStatus status,
                            String password, LocalDateTime createdAt) {
        return User.builder()
                .id(id)
                .email(email)
                .fullName(name)
                .phone(phone)
                .role(role)
                .status(status)
                .passwordHash(password)
                .createdAt(createdAt)
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

    private Assignment createAssignment(Long id, Boolean checkListOk,
                                        LocalDateTime assignedAt, Trip trip,
                                        User driver, User dispatcher) {
        return Assignment.builder()
                .id(id)
                .checkListOk(checkListOk)
                .assignedAt(assignedAt)
                .trip(trip)
                .driver(driver)
                .dispatcher(dispatcher)
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

    // ======================================================================
    // TESTS - assignDriverToTrip
    // ======================================================================

    @Test
    @DisplayName("Debe asignar conductor a un viaje de manera exitosa cuando no existe asignación previa")
    void shouldAssignDriverToTrip() {
        // Arrange
        Long tripId = 1L;
        Long driverId = 1L;

        Route route = createRoute(1L, "R001", "Ruta Norte", "Bogotá", "Medellín");
        Bus bus = createBus(1L, "ABC123", 40, BusStatus.AVAILABLE);

        OffsetDateTime departureAt = OffsetDateTime.now().plusDays(1);
        OffsetDateTime arrivalAt = departureAt.plusHours(8);

        Trip trip = createTrip(tripId, LocalDate.now().plusDays(1),
                departureAt, arrivalAt, TripStatus.SCHEDULED, route, bus);

        User driver = createUser(driverId, "driver@test.com", "John Doe",
                "1234567890", UserRole.DRIVER, UserStatus.ACTIVE,
                "hashedPassword", LocalDateTime.now());

        when(tripRepository.findById(tripId)).thenReturn(Optional.of(trip));
        when(userRepository.findById(driverId)).thenReturn(Optional.of(driver));
        when(assignmentRepository.findAssignmentByTrip_Id(tripId)).thenReturn(Optional.empty());
        when(assignmentRepository.findByDriver(driverId)).thenReturn(new ArrayList<>());
        when(assignmentRepository.save(any(Assignment.class))).thenAnswer(invocation -> {
            Assignment saved = invocation.getArgument(0);
            saved.setId(1L);
            return saved;
        });

        // Act
        AssignmentResponse response = assignmentService.assignDriverToTrip(tripId, driverId);

        // Assert
        assertNotNull(response);
        assertEquals(tripId, response.trip().id());
        assertEquals(driverId, response.driver().id());
        verify(assignmentRepository, times(1)).save(any(Assignment.class));
        verify(tripRepository, times(1)).findById(tripId);
        verify(userRepository, times(1)).findById(driverId);
    }

    @Test
    @DisplayName("Debe lanzar excepcion si el Usuario asignado no es un conductor")
    void shouldThrowExceptionWhenAssignedDriverIsNotDriver() {
        // Arrange
        Long tripId = 1L;
        Long userId = 2L;

        Route route = createRoute(1L, "R001", "Ruta Norte", "Bogotá", "Medellín");
        Bus bus = createBus(1L, "ABC123", 40, BusStatus.AVAILABLE);

        Trip trip = createTrip(tripId, LocalDate.now().plusDays(1),
                OffsetDateTime.now().plusDays(1), OffsetDateTime.now().plusDays(1).plusHours(8),
                TripStatus.SCHEDULED, route, bus);

        // Usuario NO es conductor
        User passenger = createUser(userId, "passenger@test.com", "Jane Doe",
                "0987654321", UserRole.PASSENGER, UserStatus.ACTIVE,
                "hashedPassword", LocalDateTime.now());

        when(tripRepository.findById(tripId)).thenReturn(Optional.of(trip));
        when(userRepository.findById(userId)).thenReturn(Optional.of(passenger));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> assignmentService.assignDriverToTrip(tripId, userId));

        assertEquals("User is not a driver", exception.getMessage());
        verify(assignmentRepository, never()).save(any(Assignment.class));
    }

    @Test
    @DisplayName("Debe lanzar excepcion si el conductor que se desea asignar tiene conflictos con los viajes a los que ya pertenece")
    void shouldThrowExceptionWhenDriverHasConflictingTrip() {
        // Arrange
        Long tripId = 1L;
        Long driverId = 1L;
        Long conflictingTripId = 2L;

        Route route = createRoute(1L, "R001", "Ruta Norte", "Bogotá", "Medellín");
        Bus bus = createBus(1L, "ABC123", 40, BusStatus.AVAILABLE);

        OffsetDateTime departureAt = OffsetDateTime.now().plusDays(1).withHour(10).withMinute(0);
        OffsetDateTime arrivalAt = departureAt.plusHours(8);

        Trip trip = createTrip(tripId, LocalDate.now().plusDays(1),
                departureAt, arrivalAt, TripStatus.SCHEDULED, route, bus);

        // Viaje conflictivo con horario superpuesto
        OffsetDateTime conflictDeparture = departureAt.plusHours(4);
        OffsetDateTime conflictArrival = conflictDeparture.plusHours(6);

        Trip conflictingTrip = createTrip(conflictingTripId, LocalDate.now().plusDays(1),
                conflictDeparture, conflictArrival, TripStatus.SCHEDULED, route, bus);

        User driver = createUser(driverId, "driver@test.com", "John Doe",
                "1234567890", UserRole.DRIVER, UserStatus.ACTIVE,
                "hashedPassword", LocalDateTime.now());

        Assignment existingAssignment = createAssignment(1L, true, LocalDateTime.now(),
                conflictingTrip, driver, null);

        when(tripRepository.findById(tripId)).thenReturn(Optional.of(trip));
        when(userRepository.findById(driverId)).thenReturn(Optional.of(driver));
        when(assignmentRepository.findByDriver(driverId)).thenReturn(List.of(existingAssignment));

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> assignmentService.assignDriverToTrip(tripId, driverId));

        assertEquals("Driver has conflicting trip", exception.getMessage());
        verify(assignmentRepository, never()).save(any(Assignment.class));
    }

    @Test
    @DisplayName("Debe actualizar el Conductor de la asignacion cuando el viaje ya tiene uno asignado")
    void shouldUpdateAssignmentDriverWhenTripAlreadyHasOne() {
        // Arrange
        Long tripId = 1L;
        Long oldDriverId = 1L;
        Long newDriverId = 2L;

        Route route = createRoute(1L, "R001", "Ruta Norte", "Bogotá", "Medellín");
        Bus bus = createBus(1L, "ABC123", 40, BusStatus.AVAILABLE);

        OffsetDateTime departureAt = OffsetDateTime.now().plusDays(1);
        OffsetDateTime arrivalAt = departureAt.plusHours(8);

        Trip trip = createTrip(tripId, LocalDate.now().plusDays(1),
                departureAt, arrivalAt, TripStatus.SCHEDULED, route, bus);

        User oldDriver = createUser(oldDriverId, "olddriver@test.com", "Old Driver",
                "1234567890", UserRole.DRIVER, UserStatus.ACTIVE,
                "hashedPassword", LocalDateTime.now());

        User newDriver = createUser(newDriverId, "newdriver@test.com", "New Driver",
                "0987654321", UserRole.DRIVER, UserStatus.ACTIVE,
                "hashedPassword", LocalDateTime.now());

        Assignment existingAssignment = createAssignment(1L, true, LocalDateTime.now().minusDays(1),
                trip, oldDriver, null);

        when(tripRepository.findById(tripId)).thenReturn(Optional.of(trip));
        when(userRepository.findById(newDriverId)).thenReturn(Optional.of(newDriver));
        when(assignmentRepository.findAssignmentByTrip_Id(tripId)).thenReturn(Optional.of(existingAssignment));
        when(assignmentRepository.findByDriver(newDriverId)).thenReturn(new ArrayList<>());
        when(assignmentRepository.save(any(Assignment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        AssignmentResponse response = assignmentService.assignDriverToTrip(tripId, newDriverId);

        // Assert
        assertNotNull(response);
        assertEquals(newDriverId, response.driver().id());
        verify(assignmentRepository, times(1)).save(existingAssignment);
        verify(assignmentRepository, times(1)).findAssignmentByTrip_Id(tripId);
    }

    @Test
    @DisplayName("Debe lanzar excepcion cuando el Conductor que se desea asignar no existe")
    void shouldThrowExceptionWhenDriverDoesNotExist() {
        // Arrange
        Long tripId = 1L;
        Long driverId = 999L;

        Route route = createRoute(1L, "R001", "Ruta Norte", "Bogotá", "Medellín");
        Bus bus = createBus(1L, "ABC123", 40, BusStatus.AVAILABLE);

        Trip trip = createTrip(tripId, LocalDate.now().plusDays(1),
                OffsetDateTime.now().plusDays(1), OffsetDateTime.now().plusDays(1).plusHours(8),
                TripStatus.SCHEDULED, route, bus);

        when(tripRepository.findById(tripId)).thenReturn(Optional.of(trip));
        when(userRepository.findById(driverId)).thenReturn(Optional.empty());

        // Act & Assert
        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> assignmentService.assignDriverToTrip(tripId, driverId));

        assertEquals("Driver not found", exception.getMessage());
        verify(assignmentRepository, never()).save(any(Assignment.class));
    }

    @Test
    @DisplayName("Debe lanzar excepcion cuando el Viaje no existe")
    void shouldThrowExceptionWhenTripDoesNotExist() {
        // Arrange
        Long tripId = 999L;
        Long driverId = 1L;

        when(tripRepository.findById(tripId)).thenReturn(Optional.empty());

        // Act & Assert
        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> assignmentService.assignDriverToTrip(tripId, driverId));

        assertEquals("Trip not found", exception.getMessage());
        verify(userRepository, never()).findById(anyLong());
        verify(assignmentRepository, never()).save(any(Assignment.class));
    }

    // ======================================================================
    // TESTS - assignBusToTrip
    // ======================================================================

    @Test
    @DisplayName("Debe asignar de manera exitosa el Bus al Viaje")
    void shouldAssignBusToTrip() {
        // Arrange
        Long tripId = 1L;
        Long busId = 1L;

        Route route = createRoute(1L, "R001", "Ruta Norte", "Bogotá", "Medellín");
        Bus bus = createBus(busId, "ABC123", 40, BusStatus.AVAILABLE);

        OffsetDateTime departureAt = OffsetDateTime.now().plusDays(1);
        OffsetDateTime arrivalAt = departureAt.plusHours(8);

        Trip trip = createTrip(tripId, LocalDate.now().plusDays(1),
                departureAt, arrivalAt, TripStatus.SCHEDULED, route, null);

        when(tripRepository.findById(tripId)).thenReturn(Optional.of(trip));
        when(busRepository.findById(busId)).thenReturn(Optional.of(bus));
        when(tripRepository.save(any(Trip.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(assignmentRepository.findAssignmentByTrip_Id(tripId)).thenReturn(Optional.empty());
        when(assignmentRepository.save(any(Assignment.class))).thenAnswer(invocation -> {
            Assignment saved = invocation.getArgument(0);
            saved.setId(1L);
            return saved;
        });

        // Act
        AssignmentResponse response = assignmentService.assignBusToTrip(tripId, busId);

        // Assert
        assertNotNull(response);
        assertEquals(tripId, response.trip().id());
        verify(tripRepository, times(1)).save(trip);
        verify(assignmentRepository, times(1)).save(any(Assignment.class));
    }

    @Test
    @DisplayName("Debe lanzar excepcion cuando el Bus que se desea asignar tiene conflicto con sus Viajes ya asignados")
    void shouldThrowExceptionWhenBusHasConflictingTrips() {
        // Arrange
        Long tripId = 1L;
        Long busId = 1L;

        Route route = createRoute(1L, "R001", "Ruta Norte", "Bogotá", "Medellín");
        Bus bus = createBus(busId, "ABC123", 40, BusStatus.AVAILABLE);

        OffsetDateTime departureAt = OffsetDateTime.now().plusDays(1).withHour(10).withMinute(0);
        OffsetDateTime arrivalAt = departureAt.plusHours(8);

        Trip newTrip = createTrip(tripId, LocalDate.now().plusDays(1),
                departureAt, arrivalAt, TripStatus.SCHEDULED, route, null);

        // Viaje conflictivo ya asignado al bus
        OffsetDateTime conflictDeparture = departureAt.plusHours(4);
        OffsetDateTime conflictArrival = conflictDeparture.plusHours(6);

        Trip conflictingTrip = createTrip(2L, LocalDate.now().plusDays(1),
                conflictDeparture, conflictArrival, TripStatus.SCHEDULED, route, bus);

        bus.getTrips().add(conflictingTrip);

        when(tripRepository.findById(tripId)).thenReturn(Optional.of(newTrip));
        when(busRepository.findById(busId)).thenReturn(Optional.of(bus));

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> assignmentService.assignBusToTrip(tripId, busId));

        assertEquals("Bus has conflicting trip", exception.getMessage());
        verify(tripRepository, never()).save(any(Trip.class));
        verify(assignmentRepository, never()).save(any(Assignment.class));
    }

    @Test
    @DisplayName("Debe lanzar excepcion cuando el Bus no existe")
    void shouldThrowExceptionWhenBusDoesNotExist() {
        // Arrange
        Long tripId = 1L;
        Long busId = 999L;

        Route route = createRoute(1L, "R001", "Ruta Norte", "Bogotá", "Medellín");
        Trip trip = createTrip(tripId, LocalDate.now().plusDays(1),
                OffsetDateTime.now().plusDays(1), OffsetDateTime.now().plusDays(1).plusHours(8),
                TripStatus.SCHEDULED, route, null);

        when(tripRepository.findById(tripId)).thenReturn(Optional.of(trip));
        when(busRepository.findById(busId)).thenReturn(Optional.empty());

        // Act & Assert
        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> assignmentService.assignBusToTrip(tripId, busId));

        assertEquals("Bus not found", exception.getMessage());
    }

    // ======================================================================
    // TESTS - updateAssignment
    // ======================================================================

    @Test
    @DisplayName("Debe actualizar de manera exitosa la Asignacion")
    void shouldUpdateAssignment() {
        // Arrange
        Long assignmentId = 1L;

        Route route = createRoute(1L, "R001", "Ruta Norte", "Bogotá", "Medellín");
        Bus bus = createBus(1L, "ABC123", 40, BusStatus.AVAILABLE);
        Trip trip = createTrip(1L, LocalDate.now().plusDays(1),
                OffsetDateTime.now().plusDays(1), OffsetDateTime.now().plusDays(1).plusHours(8),
                TripStatus.SCHEDULED, route, bus);

        User driver = createUser(1L, "driver@test.com", "John Doe",
                "1234567890", UserRole.DRIVER, UserStatus.ACTIVE,
                "hashedPassword", LocalDateTime.now());

        Assignment assignment = createAssignment(assignmentId, false, LocalDateTime.now(),
                trip, driver, null);

        AssignmentUpdateRequest updateRequest = new AssignmentUpdateRequest(
                true, LocalDateTime.now(), null, null, null
        );

        when(assignmentRepository.findById(assignmentId)).thenReturn(Optional.of(assignment));
        when(assignmentRepository.save(any(Assignment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        assignmentService.updateAssignment(assignmentId, updateRequest);

        // Assert
        verify(assignmentRepository, times(1)).save(assignment);
        verify(assignmentRepository, times(1)).findById(assignmentId);
    }

    @Test
    @DisplayName("Debe lanzar excepcion cuando no se encuentra la Asignacion para actualizar")
    void shouldThrowExceptionWhenAssignmentNotFoundForUpdate() {
        // Arrange
        Long assignmentId = 999L;
        AssignmentUpdateRequest updateRequest = new AssignmentUpdateRequest(
                true, LocalDateTime.now(), null, null, null
        );

        when(assignmentRepository.findById(assignmentId)).thenReturn(Optional.empty());

        // Act & Assert
        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> assignmentService.updateAssignment(assignmentId, updateRequest));

        assertEquals("Assignment not found", exception.getMessage());
        verify(assignmentRepository, never()).save(any(Assignment.class));
    }

    // ======================================================================
    // TESTS - deleteAssignment
    // ======================================================================

    @Test
    @DisplayName("Debe eliminar de manera exitosa la Asignacion")
    void shouldDeleteAssignment() {
        // Arrange
        Long assignmentId = 1L;

        when(assignmentRepository.existsById(assignmentId)).thenReturn(true);
        doNothing().when(assignmentRepository).deleteById(assignmentId);

        // Act
        assignmentService.deleteAssignment(assignmentId);

        // Assert
        verify(assignmentRepository, times(1)).existsById(assignmentId);
        verify(assignmentRepository, times(1)).deleteById(assignmentId);
    }

    @Test
    @DisplayName("Debe lanzar excepcion cuando no se encuentra la Asignacion para eliminar")
    void shouldThrowExceptionWhenAssignmentNotFoundForDelete() {
        // Arrange
        Long assignmentId = 999L;

        when(assignmentRepository.existsById(assignmentId)).thenReturn(false);

        // Act & Assert
        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> assignmentService.deleteAssignment(assignmentId));

        assertEquals("Assignment not found", exception.getMessage());
        verify(assignmentRepository, never()).deleteById(anyLong());
    }

    // ======================================================================
    // TESTS - getAssignment
    // ======================================================================

    @Test
    @DisplayName("Debe obtener de manera exitosa la Asignacion por ID")
    void shouldGetAssignment() {
        // Arrange
        Long assignmentId = 1L;

        Route route = createRoute(1L, "R001", "Ruta Norte", "Bogotá", "Medellín");
        Bus bus = createBus(1L, "ABC123", 40, BusStatus.AVAILABLE);
        Trip trip = createTrip(1L, LocalDate.now().plusDays(1),
                OffsetDateTime.now().plusDays(1), OffsetDateTime.now().plusDays(1).plusHours(8),
                TripStatus.SCHEDULED, route, bus);

        User driver = createUser(1L, "driver@test.com", "John Doe",
                "1234567890", UserRole.DRIVER, UserStatus.ACTIVE,
                "hashedPassword", LocalDateTime.now());

        Assignment assignment = createAssignment(assignmentId, true, LocalDateTime.now(),
                trip, driver, null);

        when(assignmentRepository.findById(assignmentId)).thenReturn(Optional.of(assignment));

        // Act
        AssignmentResponse response = assignmentService.getAssignment(assignmentId);

        // Assert
        assertNotNull(response);
        assertEquals(assignmentId, response.id());
        assertEquals(trip.getId(), response.trip().id());
        assertEquals(driver.getId(), response.driver().id());
        verify(assignmentRepository, times(1)).findById(assignmentId);
    }

    @Test
    @DisplayName("Debe lanzar excepcion cuando no se encuentra la Asignacion por ID")
    void shouldThrowExceptionWhenAssignmentNotFoundById() {
        // Arrange
        Long assignmentId = 999L;

        when(assignmentRepository.findById(assignmentId)).thenReturn(Optional.empty());

        // Act & Assert
        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> assignmentService.getAssignment(assignmentId));

        assertEquals("Assignment not found", exception.getMessage());
    }

    // ======================================================================
    // TESTS - getAssignmentByTripId
    // ======================================================================

    @Test
    @DisplayName("Debe obtener la Asignacion dado el identificador del Viaje")
    void shouldGetAssignmentByTripId() {
        // Arrange
        Long tripId = 1L;
        Long assignmentId = 1L;

        Route route = createRoute(1L, "R001", "Ruta Norte", "Bogotá", "Medellín");
        Bus bus = createBus(1L, "ABC123", 40, BusStatus.AVAILABLE);
        Trip trip = createTrip(tripId, LocalDate.now().plusDays(1),
                OffsetDateTime.now().plusDays(1), OffsetDateTime.now().plusDays(1).plusHours(8),
                TripStatus.SCHEDULED, route, bus);

        User driver = createUser(1L, "driver@test.com", "John Doe",
                "1234567890", UserRole.DRIVER, UserStatus.ACTIVE,
                "hashedPassword", LocalDateTime.now());

        Assignment assignment = createAssignment(assignmentId, true, LocalDateTime.now(),
                trip, driver, null);

        when(assignmentRepository.findAssignmentByTrip_Id(tripId)).thenReturn(Optional.of(assignment));

        // Act
        AssignmentResponse response = assignmentService.getAssignmentByTripId(tripId);

        // Assert
        assertNotNull(response);
        assertEquals(tripId, response.trip().id());
        verify(assignmentRepository, times(1)).findAssignmentByTrip_Id(tripId);
    }

    @Test
    @DisplayName("Debe lanzar excepcion cuando no se encontro la Asignacion dado el identificador del Viaje")
    void shouldThrowExceptionWhenAssignmentNotFoundByTrip() {
        // Arrange
        Long tripId = 999L;

        when(assignmentRepository.findAssignmentByTrip_Id(tripId)).thenReturn(Optional.empty());

        // Act & Assert
        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> assignmentService.getAssignmentByTripId(tripId));

        assertEquals("Assignment not found for trip", exception.getMessage());
    }

    // ======================================================================
    // TESTS - getAssignmentByDriverId
    // ======================================================================

    @Test
    @DisplayName("Debe obtener las Asignaciones dado el identificador del Conductor")
    void shouldGetAssignmentsByDriverId() {
        // Arrange
        Long driverId = 1L;

        Route route = createRoute(1L, "R001", "Ruta Norte", "Bogotá", "Medellín");
        Bus bus = createBus(1L, "ABC123", 40, BusStatus.AVAILABLE);

        Trip trip1 = createTrip(1L, LocalDate.now().plusDays(1),
                OffsetDateTime.now().plusDays(1), OffsetDateTime.now().plusDays(1).plusHours(8),
                TripStatus.SCHEDULED, route, bus);

        Trip trip2 = createTrip(2L, LocalDate.now().plusDays(2),
                OffsetDateTime.now().plusDays(2), OffsetDateTime.now().plusDays(2).plusHours(8),
                TripStatus.SCHEDULED, route, bus);

        User driver = createUser(driverId, "driver@test.com", "John Doe",
                "1234567890", UserRole.DRIVER, UserStatus.ACTIVE,
                "hashedPassword", LocalDateTime.now());

        Assignment assignment1 = createAssignment(1L, true, LocalDateTime.now(), trip1, driver, null);
        Assignment assignment2 = createAssignment(2L, true, LocalDateTime.now(), trip2, driver, null);

        when(assignmentRepository.findByDriver(driverId)).thenReturn(List.of(assignment1, assignment2));

        // Act
        List<AssignmentResponse> responses = assignmentService.getAssignmentByDriverId(driverId);

        // Assert
        assertNotNull(responses);
        assertEquals(2, responses.size());
        assertEquals(driverId, responses.get(0).driver().id());
        assertEquals(driverId, responses.get(1).driver().id());
        verify(assignmentRepository, times(1)).findByDriver(driverId);
    }

    @Test
    @DisplayName("Debe retornar lista vacia cuando el Conductor no tiene Asignaciones")
    void shouldReturnEmptyListWhenDriverHasNoAssignments() {
        // Arrange
        Long driverId = 1L;

        when(assignmentRepository.findByDriver(driverId)).thenReturn(new ArrayList<>());

        // Act
        List<AssignmentResponse> responses = assignmentService.getAssignmentByDriverId(driverId);

        // Assert
        assertNotNull(responses);
        assertTrue(responses.isEmpty());
        verify(assignmentRepository, times(1)).findByDriver(driverId);
    }

    // ======================================================================
    // TESTS - checkDriverConflict
    // ======================================================================

    @Test
    @DisplayName("Debe retornar true cuando hay conflicto de horarios del conductor")
    void shouldReturnTrueWhenDriverHasConflict() {
        // Arrange
        Long driverId = 1L;
        OffsetDateTime start = OffsetDateTime.now().plusDays(1).withHour(10).withMinute(0);
        OffsetDateTime end = start.plusHours(8);

        Route route = createRoute(1L, "R001", "Ruta Norte", "Bogotá", "Medellín");
        Bus bus = createBus(1L, "ABC123", 40, BusStatus.AVAILABLE);

        User driver = createUser(driverId, "driver@test.com", "John Doe",
                "1234567890", UserRole.DRIVER, UserStatus.ACTIVE,
                "hashedPassword", LocalDateTime.now());

        // Viaje existente con horario superpuesto
        OffsetDateTime existingStart = start.plusHours(4);
        OffsetDateTime existingEnd = existingStart.plusHours(6);

        Trip existingTrip = createTrip(1L, LocalDate.now().plusDays(1),
                existingStart, existingEnd, TripStatus.SCHEDULED, route, bus);

        Assignment existingAssignment = createAssignment(1L, true, LocalDateTime.now(),
                existingTrip, driver, null);

        when(assignmentRepository.findByDriver(driverId)).thenReturn(List.of(existingAssignment));

        // Act
        boolean hasConflict = assignmentService.checkDriverConflict(driverId, start, end);

        // Assert
        assertTrue(hasConflict);
        verify(assignmentRepository, times(1)).findByDriver(driverId);
    }

    @Test
    @DisplayName("Debe retornar false cuando no hay conflicto de horarios del conductor")
    void shouldReturnFalseWhenDriverHasNoConflict() {
        // Arrange
        Long driverId = 1L;
        OffsetDateTime start = OffsetDateTime.now().plusDays(1).withHour(10).withMinute(0);
        OffsetDateTime end = start.plusHours(8);

        Route route = createRoute(1L, "R001", "Ruta Norte", "Bogotá", "Medellín");
        Bus bus = createBus(1L, "ABC123", 40, BusStatus.AVAILABLE);

        User driver = createUser(driverId, "driver@test.com", "John Doe",
                "1234567890", UserRole.DRIVER, UserStatus.ACTIVE,
                "hashedPassword", LocalDateTime.now());

        // Viaje existente SIN horario superpuesto (termina antes)
        OffsetDateTime existingStart = start.minusHours(10);
        OffsetDateTime existingEnd = start.minusHours(2);

        Trip existingTrip = createTrip(1L, LocalDate.now().plusDays(1),
                existingStart, existingEnd, TripStatus.SCHEDULED, route, bus);

        Assignment existingAssignment = createAssignment(1L, true, LocalDateTime.now(),
                existingTrip, driver, null);

        when(assignmentRepository.findByDriver(driverId)).thenReturn(List.of(existingAssignment));

        // Act
        boolean hasConflict = assignmentService.checkDriverConflict(driverId, start, end);

        // Assert
        assertFalse(hasConflict);
        verify(assignmentRepository, times(1)).findByDriver(driverId);
    }
}