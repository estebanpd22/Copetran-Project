package co.unimagdalena.services;

import co.unimagdalena.api.dto.PurchaseDto.PurchaseCreateRequest;
import co.unimagdalena.api.dto.TicketDto.*;
import co.unimagdalena.domine.entities.*;
import co.unimagdalena.domine.repositories.*;
import co.unimagdalena.services.impl.TicketServiceImpl;
import co.unimagdalena.services.mapper.TicketMapper;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TicketServiceImpl Tests")
class TicketServiceImplTest {

    @Mock
    private TicketRepository ticketRepository;
    @Mock
    private TripRepository tripRepository;
    @Mock
    private PassengerRepository passengerRepository;
    @Mock
    private StopRepository stopRepository;
    @Mock
    private PurchaseRepository purchaseRepository;
    @Mock
    private SeatRepository seatRepository;
    @Mock
    private TicketMapper ticketMapper;

    @InjectMocks
    private TicketServiceImpl ticketService;

    private PurchaseCreateRequest.TicketRequest ticketRequest;
    private Purchase purchase;
    private Trip trip;
    private Passenger passenger;
    private Seat seat;
    private Stop fromStop;
    private Stop toStop;
    private Route route;
    private Bus bus;
    private Ticket ticket;

    @BeforeEach
    void setUp() {
        // Crear Route
        route = createRoute();

        // Crear Bus
        bus = createBus();

        // Crear Stops
        fromStop = createStop(1, route);
        toStop = createStop(2, route);

        // Crear Trip
        trip = createTrip(route, bus);

        // Crear Passenger
        passenger = createPassenger();

        // Crear Seat
        seat = createSeat(bus);

        // Crear Purchase
        purchase = createPurchase();

        // Crear TicketRequest
        ticketRequest = createTicketRequest();

        // Crear Ticket
        ticket = createTicket(trip, passenger, seat, fromStop, toStop, purchase);
    }

    // ==================== TESTS: createTicket ====================

    @Test
    @DisplayName("Debe crear un ticket exitosamente con todas las validaciones pasadas")
    void shouldCreateTicketSuccessfully() {
        // Arrange
        when(tripRepository.findById(ticketRequest.tripId())).thenReturn(Optional.of(trip));
        when(passengerRepository.findById(ticketRequest.passengerId())).thenReturn(Optional.of(passenger));
        when(seatRepository.findById(ticketRequest.seatId())).thenReturn(Optional.of(seat));
        when(stopRepository.findById(ticketRequest.fromStopId())).thenReturn(Optional.of(fromStop));
        when(stopRepository.findById(ticketRequest.toStopId())).thenReturn(Optional.of(toStop));
        when(ticketRepository.findByTripId(trip.getId())).thenReturn(Collections.emptyList());
        when(ticketRepository.save(any(Ticket.class))).thenReturn(ticket);

        // Act
        Ticket result = ticketService.createTicket(ticketRequest, purchase);

        // Assert
        assertNotNull(result);
        assertEquals(trip.getId(), result.getTrip().getId());
        assertEquals(passenger.getId(), result.getPassenger().getId());
        assertEquals(seat.getId(), result.getSeat().getId());
        assertEquals(TicketStatus.SOLD, result.getStatus());
        assertNotNull(result.getQrCode());
        verify(ticketRepository, times(1)).save(any(Ticket.class));
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando el Trip no existe")
    void shouldThrowExceptionWhenTripNotFound() {
        // Arrange
        when(tripRepository.findById(ticketRequest.tripId())).thenReturn(Optional.empty());

        // Act & Assert
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> ticketService.createTicket(ticketRequest, purchase));
        assertTrue(exception.getMessage().contains("Trip"));
        verify(ticketRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando el Passenger no existe")
    void shouldThrowExceptionWhenPassengerNotFound() {
        // Arrange
        when(tripRepository.findById(ticketRequest.tripId())).thenReturn(Optional.of(trip));
        when(passengerRepository.findById(ticketRequest.passengerId())).thenReturn(Optional.empty());

        // Act & Assert
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> ticketService.createTicket(ticketRequest, purchase));
        assertTrue(exception.getMessage().contains("Passenger"));
        verify(ticketRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando el Seat no existe")
    void shouldThrowExceptionWhenSeatNotFound() {
        // Arrange
        when(tripRepository.findById(ticketRequest.tripId())).thenReturn(Optional.of(trip));
        when(passengerRepository.findById(ticketRequest.passengerId())).thenReturn(Optional.of(passenger));
        when(seatRepository.findById(ticketRequest.seatId())).thenReturn(Optional.empty());

        // Act & Assert
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> ticketService.createTicket(ticketRequest, purchase));
        assertTrue(exception.getMessage().contains("Seat"));
        verify(ticketRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando el Stop de origen no existe")
    void shouldThrowExceptionWhenFromStopNotFound() {
        // Arrange
        when(tripRepository.findById(ticketRequest.tripId())).thenReturn(Optional.of(trip));
        when(passengerRepository.findById(ticketRequest.passengerId())).thenReturn(Optional.of(passenger));
        when(seatRepository.findById(ticketRequest.seatId())).thenReturn(Optional.of(seat));
        when(stopRepository.findById(ticketRequest.fromStopId())).thenReturn(Optional.empty());

        // Act & Assert
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> ticketService.createTicket(ticketRequest, purchase));
        assertTrue(exception.getMessage().contains("Stop"));
        verify(ticketRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando el Stop de destino no existe")
    void shouldThrowExceptionWhenToStopNotFound() {
        // Arrange
        when(tripRepository.findById(ticketRequest.tripId())).thenReturn(Optional.of(trip));
        when(passengerRepository.findById(ticketRequest.passengerId())).thenReturn(Optional.of(passenger));
        when(seatRepository.findById(ticketRequest.seatId())).thenReturn(Optional.of(seat));
        when(stopRepository.findById(ticketRequest.fromStopId())).thenReturn(Optional.of(fromStop));
        when(stopRepository.findById(ticketRequest.toStopId())).thenReturn(Optional.empty());

        // Act & Assert
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> ticketService.createTicket(ticketRequest, purchase));
        assertTrue(exception.getMessage().contains("Stop"));
        verify(ticketRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando el Seat no pertenece al Bus del Trip")
    void shouldThrowExceptionWhenSeatNotBelongToBus() {
        // Arrange
        Bus differentBus = createBus();
        differentBus.setId(999L);
        seat.setBus(differentBus);

        when(tripRepository.findById(ticketRequest.tripId())).thenReturn(Optional.of(trip));
        when(passengerRepository.findById(ticketRequest.passengerId())).thenReturn(Optional.of(passenger));
        when(seatRepository.findById(ticketRequest.seatId())).thenReturn(Optional.of(seat));
        when(stopRepository.findById(ticketRequest.fromStopId())).thenReturn(Optional.of(fromStop));
        when(stopRepository.findById(ticketRequest.toStopId())).thenReturn(Optional.of(toStop));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> ticketService.createTicket(ticketRequest, purchase));
        assertTrue(exception.getMessage().contains("Seat does not belong to trip's bus"));
        verify(ticketRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando el Stop de origen no pertenece a la ruta del Trip")
    void shouldThrowExceptionWhenFromStopNotBelongToRoute() {
        // Arrange
        Route differentRoute = createRoute();
        differentRoute.setId(999L);
        Stop differentStop = createStop(1, differentRoute);

        when(tripRepository.findById(ticketRequest.tripId())).thenReturn(Optional.of(trip));
        when(passengerRepository.findById(ticketRequest.passengerId())).thenReturn(Optional.of(passenger));
        when(seatRepository.findById(ticketRequest.seatId())).thenReturn(Optional.of(seat));
        when(stopRepository.findById(ticketRequest.fromStopId())).thenReturn(Optional.of(differentStop));
        when(stopRepository.findById(ticketRequest.toStopId())).thenReturn(Optional.of(toStop));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> ticketService.createTicket(ticketRequest, purchase));
        assertTrue(exception.getMessage().contains("origin stop does not belong to the trip route"));
        verify(ticketRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando el Stop de destino no pertenece a la ruta del Trip")
    void shouldThrowExceptionWhenToStopNotBelongToRoute() {
        // Arrange
        Route differentRoute = createRoute();
        differentRoute.setId(999L);
        Stop differentStop = createStop(3, differentRoute);

        when(tripRepository.findById(ticketRequest.tripId())).thenReturn(Optional.of(trip));
        when(passengerRepository.findById(ticketRequest.passengerId())).thenReturn(Optional.of(passenger));
        when(seatRepository.findById(ticketRequest.seatId())).thenReturn(Optional.of(seat));
        when(stopRepository.findById(ticketRequest.fromStopId())).thenReturn(Optional.of(fromStop));
        when(stopRepository.findById(ticketRequest.toStopId())).thenReturn(Optional.of(differentStop));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> ticketService.createTicket(ticketRequest, purchase));
        assertTrue(exception.getMessage().contains("destination stop does not belong to the trip route"));
        verify(ticketRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando el orden del Stop de origen >= al Stop de destino")
    void shouldThrowExceptionWhenFromStopOrderGreaterOrEqualToStopOrder() {
        // Arrange
        Stop invalidFromStop = createStop(3, route);
        Stop invalidToStop = createStop(2, route);

        when(tripRepository.findById(ticketRequest.tripId())).thenReturn(Optional.of(trip));
        when(passengerRepository.findById(ticketRequest.passengerId())).thenReturn(Optional.of(passenger));
        when(seatRepository.findById(ticketRequest.seatId())).thenReturn(Optional.of(seat));
        when(stopRepository.findById(ticketRequest.fromStopId())).thenReturn(Optional.of(invalidFromStop));
        when(stopRepository.findById(ticketRequest.toStopId())).thenReturn(Optional.of(invalidToStop));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> ticketService.createTicket(ticketRequest, purchase));
        assertTrue(exception.getMessage().contains("origin stop must be before the destination"));
        verify(ticketRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando el asiento ya está ocupado en el segmento")
    void shouldThrowExceptionWhenSeatOccupiedInSegment() {
        // Arrange
        Ticket existingTicket = createTicket(trip, passenger, seat, fromStop, toStop, purchase);
        existingTicket.setStatus(TicketStatus.SOLD);

        when(tripRepository.findById(ticketRequest.tripId())).thenReturn(Optional.of(trip));
        when(passengerRepository.findById(ticketRequest.passengerId())).thenReturn(Optional.of(passenger));
        when(seatRepository.findById(ticketRequest.seatId())).thenReturn(Optional.of(seat));
        when(stopRepository.findById(ticketRequest.fromStopId())).thenReturn(Optional.of(fromStop));
        when(stopRepository.findById(ticketRequest.toStopId())).thenReturn(Optional.of(toStop));
        when(ticketRepository.findByTripId(trip.getId())).thenReturn(List.of(existingTicket));

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> ticketService.createTicket(ticketRequest, purchase));
        assertTrue(exception.getMessage().contains("Seat is already occupied"));
        verify(ticketRepository, never()).save(any());
    }

    // ==================== TESTS: getTicket ====================

    @Test
    @DisplayName("Debe obtener un ticket exitosamente por su ID")
    void shouldGetTicketSuccessfully() {
        // Arrange
        TicketResponse response = new TicketResponse(
                ticket.getId(), ticket.getPrice(), ticket.getPurchase().getPaymentMethod(),
                ticket.getStatus(), ticket.getCreatedAt(), ticket.getQrCode(),
                null, null, null, null, null
        );
        when(ticketRepository.findById(ticket.getId())).thenReturn(Optional.of(ticket));
        when(ticketMapper.toResponse(ticket)).thenReturn(response);

        // Act
        TicketResponse result = ticketService.getTicket(ticket.getId());

        // Assert
        assertNotNull(result);
        assertEquals(ticket.getId(), result.id());
        verify(ticketRepository, times(1)).findById(ticket.getId());
        verify(ticketMapper, times(1)).toResponse(ticket);
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando el Ticket no existe")
    void shouldThrowExceptionWhenTicketNotFound() {
        // Arrange
        when(ticketRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> ticketService.getTicket(999L));
        assertTrue(exception.getMessage().contains("Ticket"));
        verify(ticketMapper, never()).toResponse(any());
    }

    // ==================== TESTS: deleteTicket ====================

    @Test
    @DisplayName("Debe eliminar (cancelar) un ticket exitosamente")
    void shouldDeleteTicketSuccessfully() {
        // Arrange
        ticket.getTrip().setDepartureAt(OffsetDateTime.now().plusHours(5));
        when(ticketRepository.findById(ticket.getId())).thenReturn(Optional.of(ticket));
        when(ticketRepository.save(any(Ticket.class))).thenReturn(ticket);

        // Act
        ticketService.deleteTicket(ticket.getId());

        // Assert
        ArgumentCaptor<Ticket> captor = ArgumentCaptor.forClass(Ticket.class);
        verify(ticketRepository, times(1)).save(captor.capture());
        assertEquals(TicketStatus.CANCELLED, captor.getValue().getStatus());
    }

    @Test
    @DisplayName("Debe lanzar excepción al cancelar un ticket menos de 2 horas antes de la salida")
    void shouldThrowExceptionWhenCancellingTooLateBeforeDeparture() {
        // Arrange
        ticket.getTrip().setDepartureAt(OffsetDateTime.now().plusMinutes(30));
        when(ticketRepository.findById(ticket.getId())).thenReturn(Optional.of(ticket));

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> ticketService.deleteTicket(ticket.getId()));
        assertTrue(exception.getMessage().contains("Cannot cancel ticket less than 2 hours"));
        verify(ticketRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando el Ticket no existe al intentar eliminar")
    void shouldThrowExceptionWhenTicketNotFoundOnDelete() {
        // Arrange
        when(ticketRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> ticketService.deleteTicket(999L));
        assertTrue(exception.getMessage().contains("Ticket"));
        verify(ticketRepository, never()).save(any());
    }

    // ==================== TESTS: generateQrForTicket ====================

    @Test
    @DisplayName("Debe generar QR exitosamente para un ticket")
    void shouldGenerateQrSuccessfully() {
        // Arrange
        when(ticketRepository.findById(ticket.getId())).thenReturn(Optional.of(ticket));
        when(ticketRepository.save(any(Ticket.class))).thenReturn(ticket);

        // Act
        ticketService.generateQrForTicket(ticket.getId());

        // Assert
        verify(ticketRepository, times(1)).findById(ticket.getId());
        verify(ticketRepository, times(1)).save(any(Ticket.class));
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando el Ticket no existe al generar QR")
    void shouldThrowExceptionWhenTicketNotFoundOnGenerateQr() {
        // Arrange
        when(ticketRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> ticketService.generateQrForTicket(999L));
        assertTrue(exception.getMessage().contains("Ticket"));
        verify(ticketRepository, never()).save(any());
    }

    // ==================== TESTS: validateQrForTicket ====================

    @Test
    @DisplayName("Debe validar QR exitosamente para un ticket activo")
    void shouldValidateQrSuccessfully() {
        // Arrange
        String qrCode = "TKT_1_ABC12345_1234567890";
        ticket.setStatus(TicketStatus.SOLD);
        ticket.getTrip().setStatus(TripStatus.BOARDING);
        ticket.getTrip().setDepartureAt(OffsetDateTime.now().plusMinutes(10));

        when(ticketRepository.findByQrCode(qrCode)).thenReturn(Optional.of(ticket));

        // Act
        assertDoesNotThrow(() -> ticketService.validateQrForTicket(qrCode));

        // Assert
        verify(ticketRepository, times(1)).findByQrCode(qrCode);
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando el QR no existe")
    void shouldThrowExceptionWhenQrCodeNotFound() {
        // Arrange
        String qrCode = "INVALID_QR";
        when(ticketRepository.findByQrCode(qrCode)).thenReturn(Optional.empty());

        // Act & Assert
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> ticketService.validateQrForTicket(qrCode));
        assertTrue(exception.getMessage().contains("Ticket"));
        verify(ticketRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando el Ticket no está en estado SOLD")
    void shouldThrowExceptionWhenTicketNotSold() {
        // Arrange
        String qrCode = "TKT_1_ABC12345_1234567890";
        ticket.setStatus(TicketStatus.CANCELLED);
        when(ticketRepository.findByQrCode(qrCode)).thenReturn(Optional.of(ticket));

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> ticketService.validateQrForTicket(qrCode));
        assertTrue(exception.getMessage().contains("not active"));
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando el Trip no está en estado BOARDING")
    void shouldThrowExceptionWhenTripNotBoarding() {
        // Arrange
        String qrCode = "TKT_1_ABC12345_1234567890";
        ticket.setStatus(TicketStatus.SOLD);
        ticket.getTrip().setStatus(TripStatus.SCHEDULED);
        when(ticketRepository.findByQrCode(qrCode)).thenReturn(Optional.of(ticket));

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> ticketService.validateQrForTicket(qrCode));
        assertTrue(exception.getMessage().contains("not in boarding status"));
    }

    @Test
    @DisplayName("Debe marcar Ticket como NO_SHOW cuando está más de 5 minutos después de la salida")
    void shouldMarkTicketAsNoShowWhenTooLate() {
        // Arrange
        String qrCode = "TKT_1_ABC12345_1234567890";
        ticket.setStatus(TicketStatus.SOLD);
        ticket.getTrip().setStatus(TripStatus.BOARDING);
        ticket.getTrip().setDepartureAt(OffsetDateTime.now().minusMinutes(10));

        when(ticketRepository.findByQrCode(qrCode)).thenReturn(Optional.of(ticket));
        when(ticketRepository.save(any(Ticket.class))).thenReturn(ticket);

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> ticketService.validateQrForTicket(qrCode));
        assertTrue(exception.getMessage().contains("no-show"));
        verify(ticketRepository, times(1)).save(any(Ticket.class));
    }

    // ==================== TESTS: releaseSeatsByPurchase ====================

    @Test
    @DisplayName("Debe liberar asientos exitosamente para una compra")
    void shouldReleaseSeatsByPurchaseSuccessfully() {
        // Arrange
        List<Ticket> tickets = List.of(ticket);
        when(ticketRepository.findByPurchaseId(purchase.getId())).thenReturn(tickets);

        // Act
        ticketService.releaseSeatsByPurchase(purchase.getId());

        // Assert
        verify(ticketRepository, times(1)).findByPurchaseId(purchase.getId());
    }

    @Test
    @DisplayName("Debe retornar sin errores cuando no hay tickets para una compra")
    void shouldHandleEmptyTicketsForPurchase() {
        // Arrange
        when(ticketRepository.findByPurchaseId(purchase.getId())).thenReturn(Collections.emptyList());

        // Act
        ticketService.releaseSeatsByPurchase(purchase.getId());

        // Assert
        verify(ticketRepository, times(1)).findByPurchaseId(purchase.getId());
    }

    // ==================== TESTS: getTicketsByTrip ====================

    @Test
    @DisplayName("Debe obtener tickets exitosamente por ID del Trip")
    void shouldGetTicketsByTripSuccessfully() {
        // Arrange
        List<Ticket> tickets = List.of(ticket);
        TicketResponse response = new TicketResponse(
                ticket.getId(), ticket.getPrice(), ticket.getPurchase().getPaymentMethod(),
                ticket.getStatus(), ticket.getCreatedAt(), ticket.getQrCode(),
                null, null, null, null, null
        );
        when(ticketRepository.findByTripId(trip.getId())).thenReturn(tickets);
        when(ticketMapper.toResponse(ticket)).thenReturn(response);

        // Act
        List<TicketResponse> result = ticketService.getTicketsByTrip(trip.getId());

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(ticketRepository, times(1)).findByTripId(trip.getId());
        verify(ticketMapper, times(1)).toResponse(ticket);
    }

    @Test
    @DisplayName("Debe retornar lista vacía cuando no hay tickets para un Trip")
    void shouldReturnEmptyListWhenNoTicketsForTrip() {
        // Arrange
        when(ticketRepository.findByTripId(trip.getId())).thenReturn(Collections.emptyList());

        // Act
        List<TicketResponse> result = ticketService.getTicketsByTrip(trip.getId());

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(ticketMapper, never()).toResponse(any());
    }

    // ==================== TESTS: getTicketsByPurchase ====================

    @Test
    @DisplayName("Debe obtener tickets exitosamente por ID de la compra")
    void shouldGetTicketsByPurchaseSuccessfully() {
        // Arrange
        List<Ticket> tickets = List.of(ticket);
        TicketResponse response = new TicketResponse(
                ticket.getId(), ticket.getPrice(), ticket.getPurchase().getPaymentMethod(),
                ticket.getStatus(), ticket.getCreatedAt(), ticket.getQrCode(),
                null, null, null, null, null
        );
        when(ticketRepository.findByPurchaseId(purchase.getId())).thenReturn(tickets);
        when(ticketMapper.toResponse(ticket)).thenReturn(response);

        // Act
        List<TicketResponse> result = ticketService.getTicketsByPurchase(purchase.getId());

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(ticketRepository, times(1)).findByPurchaseId(purchase.getId());
        verify(ticketMapper, times(1)).toResponse(ticket);
    }

    @Test
    @DisplayName("Debe retornar lista vacía cuando no hay tickets para una compra")
    void shouldReturnEmptyListWhenNoTicketsForPurchase() {
        // Arrange
        when(ticketRepository.findByPurchaseId(purchase.getId())).thenReturn(Collections.emptyList());

        // Act
        List<TicketResponse> result = ticketService.getTicketsByPurchase(purchase.getId());

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(ticketMapper, never()).toResponse(any());
    }

    // ==================== TESTS: getTicketsByPassenger ====================

    @Test
    @DisplayName("Debe obtener tickets exitosamente por ID del Pasajero")
    void shouldGetTicketsByPassengerSuccessfully() {
        // Arrange
        List<Ticket> tickets = List.of(ticket);
        TicketResponse response = new TicketResponse(
                ticket.getId(), ticket.getPrice(), ticket.getPurchase().getPaymentMethod(),
                ticket.getStatus(), ticket.getCreatedAt(), ticket.getQrCode(),
                null, null, null, null, null
        );
        when(ticketRepository.findByPurchaseUserId(passenger.getId())).thenReturn(tickets);
        when(ticketMapper.toResponse(ticket)).thenReturn(response);

        // Act
        List<TicketResponse> result = ticketService.getTicketsByPassenger(passenger.getId());

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(ticketRepository, times(1)).findByPurchaseUserId(passenger.getId());
        verify(ticketMapper, times(1)).toResponse(ticket);
    }

    @Test
    @DisplayName("Debe retornar lista vacía cuando no hay tickets para un Pasajero")
    void shouldReturnEmptyListWhenNoTicketsForPassenger() {
        // Arrange
        when(ticketRepository.findByPurchaseUserId(passenger.getId())).thenReturn(Collections.emptyList());

        // Act
        List<TicketResponse> result = ticketService.getTicketsByPassenger(passenger.getId());

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(ticketMapper, never()).toResponse(any());
    }

    // ==================== HELPER METHODS ====================

    private Route createRoute() {
        return Route.builder()
                .id(1L)
                .code("R001")
                .name("Ruta Test")
                .origin("Bogotá")
                .destination("Medellín")
                .distanceKm(300F)
                .durationMin(360F)
                .stops(new ArrayList<>())
                .trips(new ArrayList<>())
                .fareRules(new ArrayList<>())
                .build();
    }

    private Bus createBus() {
        return Bus.builder()
                .id(1L)
                .plate("ABC123")
                .capacity(40)
                .status(BusStatus.AVAILABLE)
                .soatExpirationDate(OffsetDateTime.now().plusYears(1))
                .trips(new ArrayList<>())
                .seats(new ArrayList<>())
                .build();
    }

    private Stop createStop(int order, Route route) {
        return Stop.builder()
                .id((long) order)
                .name("Parada " + order)
                .order(order)
                .latitude(4.7110 + order * 0.01)
                .longitude(-74.0721 - order * 0.01)
                .route(route)
                .fareRulesFrom(new ArrayList<>())
                .fareRulesTo(new ArrayList<>())
                .build();
    }

    private Trip createTrip(Route route, Bus bus) {
        return Trip.builder()
                .id(1L)
                .date(LocalDate.now())
                .departureAt(OffsetDateTime.now().plusHours(2))
                .arrivalAt(OffsetDateTime.now().plusHours(8))
                .status(TripStatus.SCHEDULED)
                .route(route)
                .bus(bus)
                .seatHolds(new ArrayList<>())
                .tickets(new ArrayList<>())
                .parcels(new ArrayList<>())
                .build();
    }

    private Passenger createPassenger() {
        return Passenger.builder()
                .id(1L)
                .fullName("Juan Pérez")
                .documentType("CC")
                .documentNumber("123456789")
                .birthDate(LocalDate.of(1990, 1, 1))
                .phoneNumber("3001234567")
                .createdAt(OffsetDateTime.now())
                .tickets(new ArrayList<>())
                .build();
    }

    private Seat createSeat(Bus bus) {
        return Seat.builder()
                .id(1L)
                .price(BigDecimal.valueOf(50000))
                .number(1)
                .type(SeatType.STANDARD)
                .status(SeatStatus.AVAILABLE)
                .bus(bus)
                .build();
    }

    private Purchase createPurchase() {
        return Purchase.builder()
                .id(1L)
                .paymentMethod(PaymentMethod.CARD)
                .totalAmount(BigDecimal.valueOf(50000))
                .paymentStatus(PaymentStatus.CONFIRMED)
                .createdAt(OffsetDateTime.now())
                .tickets(new ArrayList<>())
                .build();
    }

    private PurchaseCreateRequest.TicketRequest createTicketRequest() {
        return new PurchaseCreateRequest.TicketRequest(
                1L, // tripId
                1L, // passengerId
                1L, // seatId
                "1", // seatNumber
                1L, // fromStopId
                2L, // toStopId
                null // baggage
        );
    }

    private Ticket createTicket(Trip trip, Passenger passenger, Seat seat, Stop fromStop, Stop toStop, Purchase purchase) {
        return Ticket.builder()
                .id(1L)
                .price(BigDecimal.valueOf(50000))
                .seatNumber("1")
                .status(TicketStatus.SOLD)
                .createdAt(OffsetDateTime.now())
                .qrCode("TKT_1_ABC12345_1234567890")
                .trip(trip)
                .passenger(passenger)
                .seat(seat)
                .fromStop(fromStop)
                .toStop(toStop)
                .purchase(purchase)
                .build();
    }
}
