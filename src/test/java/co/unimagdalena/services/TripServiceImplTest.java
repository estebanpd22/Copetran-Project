package co.unimagdalena.services;

import co.unimagdalena.api.dto.BusDto;
import co.unimagdalena.api.dto.RouteDto;
import co.unimagdalena.api.dto.TripDto.*;
import co.unimagdalena.domine.entities.*;
import co.unimagdalena.domine.repositories.*;
import co.unimagdalena.exception.NotFoundException;
import co.unimagdalena.services.impl.TripServiceImpl;
import co.unimagdalena.services.mapper.TripMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TripServiceImpl Test Suite")
class TripServiceImplTest {

    @Mock
    private TripRepository tripRepository;

    @Mock
    private RouteRepository routeRepository;

    @Mock
    private BusRepository busRepository;

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private TripMapper tripMapper;

    @InjectMocks
    private TripServiceImpl tripService;

    // ==================== HELPER METHODS ====================

    private Route createTestRoute() {
        return Route.builder()
                .id(1L)
                .code("RT-001")
                .name("Route 1")
                .origin("Cartagena")
                .destination("Barranquilla")
                .distanceKm(150f)
                .durationMin(180f)
                .build();
    }

    private Bus createTestBus() {
        return Bus.builder()
                .id(1L)
                .plate("ABC-123")
                .capacity(40)
                .status(BusStatus.AVAILABLE)
                .build();
    }

    private Trip createTestTrip(Long id, TripStatus status, LocalDate date, OffsetDateTime departure, OffsetDateTime arrival) {
        return Trip.builder()
                .id(id)
                .date(date)
                .departureAt(departure)
                .arrivalAt(arrival)
                .status(status)
                .route(createTestRoute())
                .bus(createTestBus())
                .build();
    }

    private TripCreateRequest createTripCreateRequest() {
        LocalDate today = LocalDate.now();
        OffsetDateTime departure = today.atTime(14, 0, 0).atOffset(java.time.ZoneOffset.of("-05:00"));
        OffsetDateTime arrival = today.atTime(17, 0, 0).atOffset(java.time.ZoneOffset.of("-05:00"));

        return new TripCreateRequest(
                today,
                departure,
                arrival,
                TripStatus.SCHEDULED,
                1L,
                1L
        );
    }

    private TripUpdateRequest createTripUpdateRequest() {
        return new TripUpdateRequest(
                null,
                null,
                null,
                null,
                null,
                null
        );
    }

    // ==================== CREATE TRIP TESTS ====================

    @Nested
    @DisplayName("createTrip")
    class CreateTripTests {

        @Test
        @DisplayName("should create trip successfully with valid data")
        void shouldCreateTripSuccessfully() {
            // Arrange
            TripCreateRequest request = createTripCreateRequest();
            Route route = createTestRoute();
            Bus bus = createTestBus();
            Trip trip = createTestTrip(null, TripStatus.SCHEDULED, request.date(), request.departureAt(), request.arrivalAt());
            Trip savedTrip = createTestTrip(1L, TripStatus.SCHEDULED, request.date(), request.departureAt(), request.arrivalAt());
            TripResponse response = new TripResponse(1L, request.date(), request.departureAt(), request.arrivalAt(),
                    TripStatus.SCHEDULED, new RouteDto.RouteSummary(1L, "RT-001", "Route 1"),
                    new BusDto.BusSummary(1L, "ABC-123", 40, BusStatus.AVAILABLE));

            when(routeRepository.findById(request.routeId())).thenReturn(Optional.of(route));
            when(busRepository.findById(request.busId())).thenReturn(Optional.of(bus));
            when(tripMapper.toEntity(request)).thenReturn(trip);
            when(tripRepository.save(any(Trip.class))).thenReturn(savedTrip);

            // Act
            TripResponse result = tripService.createTrip(request);

            // Assert
            assertNotNull(result);
            assertEquals(1L, result.id());
            assertEquals(TripStatus.SCHEDULED, result.status());
            verify(tripRepository, times(1)).save(any(Trip.class));
            verify(routeRepository, times(1)).findById(request.routeId());
            verify(busRepository, times(1)).findById(request.busId());
        }

        @Test
        @DisplayName("should throw NotFoundException when route does not exist")
        void shouldThrowNotFoundExceptionWhenRouteNotExists() {
            // Arrange
            TripCreateRequest request = createTripCreateRequest();
            when(routeRepository.findById(request.routeId())).thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(NotFoundException.class, () -> tripService.createTrip(request));
            verify(tripRepository, never()).save(any());
        }

        @Test
        @DisplayName("should throw NotFoundException when bus does not exist")
        void shouldThrowNotFoundExceptionWhenBusNotExists() {
            // Arrange
            TripCreateRequest request = createTripCreateRequest();
            Route route = createTestRoute();
            when(routeRepository.findById(request.routeId())).thenReturn(Optional.of(route));
            when(busRepository.findById(request.busId())).thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(NotFoundException.class, () -> tripService.createTrip(request));
            verify(tripRepository, never()).save(any());
        }

        @Test
        @DisplayName("should throw exception when bus is not AVAILABLE")
        void shouldThrowExceptionWhenBusNotAvailable() {
            // Arrange
            TripCreateRequest request = createTripCreateRequest();
            Route route = createTestRoute();
            Bus unavailableBus = createTestBus();
            unavailableBus.setStatus(BusStatus.IN_MAINTENANCE);

            when(routeRepository.findById(request.routeId())).thenReturn(Optional.of(route));
            when(busRepository.findById(request.busId())).thenReturn(Optional.of(unavailableBus));

            // Act & Assert
            assertThrows(IllegalArgumentException.class, () -> tripService.createTrip(request));
            verify(tripRepository, never()).save(any());
        }

        @Test
        @DisplayName("should throw exception when creating trip in the past")
        void shouldThrowExceptionWhenCreatingTripInPast() {
            // Arrange
            LocalDate pastDate = LocalDate.now().minusDays(1);
            OffsetDateTime departure = pastDate.atTime(14, 0, 0).atOffset(java.time.ZoneOffset.of("-05:00"));
            OffsetDateTime arrival = pastDate.atTime(17, 0, 0).atOffset(java.time.ZoneOffset.of("-05:00"));

            TripCreateRequest request = new TripCreateRequest(pastDate, departure, arrival, TripStatus.SCHEDULED, 1L, 1L);
            Route route = createTestRoute();
            Bus bus = createTestBus();

            when(routeRepository.findById(request.routeId())).thenReturn(Optional.of(route));
            when(busRepository.findById(request.busId())).thenReturn(Optional.of(bus));

            // Act & Assert
            assertThrows(IllegalArgumentException.class, () -> tripService.createTrip(request));
            verify(tripRepository, never()).save(any());
        }

        @Test
        @DisplayName("should throw exception when departure is not before arrival")
        void shouldThrowExceptionWhenDepartureNotBeforeArrival() {
            // Arrange
            LocalDate today = LocalDate.now();
            OffsetDateTime time = today.atTime(15, 0, 0).atOffset(java.time.ZoneOffset.of("-05:00"));

            TripCreateRequest request = new TripCreateRequest(today, time, time, TripStatus.SCHEDULED, 1L, 1L);
            Route route = createTestRoute();
            Bus bus = createTestBus();

            when(routeRepository.findById(request.routeId())).thenReturn(Optional.of(route));
            when(busRepository.findById(request.busId())).thenReturn(Optional.of(bus));

            // Act & Assert
            assertThrows(IllegalArgumentException.class, () -> tripService.createTrip(request));
            verify(tripRepository, never()).save(any());
        }

        @Test
        @DisplayName("should throw exception when trip duration exceeds 32 hours")
        void shouldThrowExceptionWhenDurationExceeds32Hours() {
            // Arrange
            LocalDate today = LocalDate.now();
            OffsetDateTime departure = today.atTime(14, 0, 0).atOffset(java.time.ZoneOffset.of("-05:00"));
            OffsetDateTime arrival = today.plusDays(2).atTime(22, 1, 0).atOffset(java.time.ZoneOffset.of("-05:00")); // 33+ horas

            TripCreateRequest request = new TripCreateRequest(today, departure, arrival, TripStatus.SCHEDULED, 1L, 1L);
            Route route = createTestRoute();
            Bus bus = createTestBus();

            when(routeRepository.findById(request.routeId())).thenReturn(Optional.of(route));
            when(busRepository.findById(request.busId())).thenReturn(Optional.of(bus));

            // Act & Assert
            assertThrows(IllegalArgumentException.class, () -> tripService.createTrip(request));
            verify(tripRepository, never()).save(any());
        }

        @Test
        @DisplayName("should throw exception when bus has scheduling conflict")
        void shouldThrowExceptionWhenBusHasSchedulingConflict() {
            // Arrange
            LocalDate today = LocalDate.now();
            OffsetDateTime departure = today.atTime(14, 0, 0).atOffset(java.time.ZoneOffset.of("-05:00"));
            OffsetDateTime arrival = today.atTime(17, 0, 0).atOffset(java.time.ZoneOffset.of("-05:00"));

            TripCreateRequest request = new TripCreateRequest(today, departure, arrival, TripStatus.SCHEDULED, 1L, 1L);
            Route route = createTestRoute();
            Bus bus = createTestBus();

            Trip conflictingTrip = createTestTrip(2L, TripStatus.SCHEDULED,
                    today.minusDays(1),
                    today.minusDays(1).atTime(20, 0, 0).atOffset(java.time.ZoneOffset.of("-05:00")),
                    today.atTime(10, 0, 0).atOffset(java.time.ZoneOffset.of("-05:00")));

            when(routeRepository.findById(request.routeId())).thenReturn(Optional.of(route));
            when(busRepository.findById(request.busId())).thenReturn(Optional.of(bus));
            when(tripRepository.findByBusIdAndStatus(request.busId(), TripStatus.SCHEDULED)).thenReturn(List.of(conflictingTrip));
            when(tripRepository.findByBusIdAndStatus(request.busId(), TripStatus.BOARDING)).thenReturn(List.of());
            when(tripRepository.findByBusIdAndStatus(request.busId(), TripStatus.DEPARTED)).thenReturn(List.of());

            // Act & Assert
            assertThrows(IllegalArgumentException.class, () -> tripService.createTrip(request));
            verify(tripRepository, never()).save(any());
        }
    }

    // ==================== UPDATE TRIP TESTS ====================

    @Nested
    @DisplayName("updateTrip")
    class UpdateTripTests {

        @Test
        @DisplayName("should update trip successfully with valid data")
        void shouldUpdateTripSuccessfully() {
            // Arrange
            Long tripId = 1L;
            Trip trip = createTestTrip(tripId, TripStatus.SCHEDULED, LocalDate.now(),
                    LocalDate.now().atTime(14, 0, 0).atOffset(java.time.ZoneOffset.of("-05:00")),
                    LocalDate.now().atTime(17, 0, 0).atOffset(java.time.ZoneOffset.of("-05:00")));
            TripUpdateRequest request = createTripUpdateRequest();

            when(tripRepository.findById(tripId)).thenReturn(Optional.of(trip));
            when(ticketRepository.countSoldByTrip(tripId)).thenReturn(0L);

            // Act
            assertDoesNotThrow(() -> tripService.updateTrip(tripId, request));

            // Assert
            verify(tripRepository, times(1)).findById(tripId);
            verify(tripRepository, times(1)).save(any(Trip.class));
        }

        @Test
        @DisplayName("should throw NotFoundException when trip does not exist")
        void shouldThrowNotFoundExceptionWhenTripNotExists() {
            // Arrange
            Long tripId = 999L;
            TripUpdateRequest request = createTripUpdateRequest();
            when(tripRepository.findById(tripId)).thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(NotFoundException.class, () -> tripService.updateTrip(tripId, request));
            verify(tripRepository, never()).save(any());
        }

        @Test
        @DisplayName("should throw exception when updating dates with sold tickets")
        void shouldThrowExceptionWhenUpdatingDatesWithSoldTickets() {
            // Arrange
            Long tripId = 1L;
            LocalDate today = LocalDate.now();
            OffsetDateTime newDeparture = today.atTime(16, 0, 0).atOffset(java.time.ZoneOffset.of("-05:00"));
            OffsetDateTime newArrival = today.atTime(19, 0, 0).atOffset(java.time.ZoneOffset.of("-05:00"));

            Trip trip = createTestTrip(tripId, TripStatus.SCHEDULED, today,
                    today.atTime(14, 0, 0).atOffset(java.time.ZoneOffset.of("-05:00")),
                    today.atTime(17, 0, 0).atOffset(java.time.ZoneOffset.of("-05:00")));
            TripUpdateRequest request = new TripUpdateRequest(today, newDeparture, newArrival, null, null, null);

            when(tripRepository.findById(tripId)).thenReturn(Optional.of(trip));
            when(ticketRepository.countSoldByTrip(tripId)).thenReturn(5L);

            // Act & Assert
            assertThrows(IllegalArgumentException.class, () -> tripService.updateTrip(tripId, request));
            verify(tripRepository, never()).save(any());
        }

        @Test
        @DisplayName("should throw exception when changing to invalid status transition")
        void shouldThrowExceptionWhenInvalidStatusTransition() {
            // Arrange
            Long tripId = 1L;
            Trip trip = createTestTrip(tripId, TripStatus.ARRIVED, LocalDate.now(),
                    LocalDate.now().atTime(14, 0, 0).atOffset(java.time.ZoneOffset.of("-05:00")),
                    LocalDate.now().atTime(17, 0, 0).atOffset(java.time.ZoneOffset.of("-05:00")));
            TripUpdateRequest request = new TripUpdateRequest(null, null, null, TripStatus.SCHEDULED, null, null);

            when(tripRepository.findById(tripId)).thenReturn(Optional.of(trip));
            when(ticketRepository.countSoldByTrip(tripId)).thenReturn(0L);

            // Act & Assert
            assertThrows(IllegalArgumentException.class, () -> tripService.updateTrip(tripId, request));
        }
    }

    // ==================== DELETE TRIP TESTS ====================

    @Nested
    @DisplayName("deleteTrip")
    class DeleteTripTests {

        @Test
        @DisplayName("should delete trip successfully when no sold tickets")
        void shouldDeleteTripSuccessfully() {
            // Arrange
            Long tripId = 1L;
            Trip trip = createTestTrip(tripId, TripStatus.SCHEDULED, LocalDate.now(),
                    LocalDate.now().atTime(14, 0, 0).atOffset(java.time.ZoneOffset.of("-05:00")),
                    LocalDate.now().atTime(17, 0, 0).atOffset(java.time.ZoneOffset.of("-05:00")));

            when(tripRepository.findById(tripId)).thenReturn(Optional.of(trip));
            when(ticketRepository.countSoldByTrip(tripId)).thenReturn(0L);

            // Act
            assertDoesNotThrow(() -> tripService.deleteTrip(tripId));

            // Assert
            verify(tripRepository, times(1)).delete(trip);
        }

        @Test
        @DisplayName("should throw NotFoundException when trip does not exist")
        void shouldThrowNotFoundExceptionWhenTripNotExists() {
            // Arrange
            Long tripId = 999L;
            when(tripRepository.findById(tripId)).thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(NotFoundException.class, () -> tripService.deleteTrip(tripId));
            verify(tripRepository, never()).delete(any());
        }

        @Test
        @DisplayName("should throw exception when trip has sold tickets")
        void shouldThrowExceptionWhenTripHasSoldTickets() {
            // Arrange
            Long tripId = 1L;
            Trip trip = createTestTrip(tripId, TripStatus.SCHEDULED, LocalDate.now(),
                    LocalDate.now().atTime(14, 0, 0).atOffset(java.time.ZoneOffset.of("-05:00")),
                    LocalDate.now().atTime(17, 0, 0).atOffset(java.time.ZoneOffset.of("-05:00")));

            when(tripRepository.findById(tripId)).thenReturn(Optional.of(trip));
            when(ticketRepository.countSoldByTrip(tripId)).thenReturn(3L);

            // Act & Assert
            assertThrows(IllegalArgumentException.class, () -> tripService.deleteTrip(tripId));
            verify(tripRepository, never()).delete(any());
        }
    }

    // ==================== GET TRIPS TESTS ====================

    @Nested
    @DisplayName("getTrips")
    class GetTripsTests {

        @Test
        @DisplayName("should retrieve trips for valid search criteria")
        void shouldRetrieveTripsSuccessfully() {
            // Arrange
            String origin = "Cartagena";
            String destination = "Barranquilla";
            LocalDate date = LocalDate.now();

            Route route = createTestRoute();
            Trip trip = createTestTrip(1L, TripStatus.SCHEDULED, date,
                    date.atTime(14, 0, 0).atOffset(java.time.ZoneOffset.of("-05:00")),
                    date.atTime(17, 0, 0).atOffset(java.time.ZoneOffset.of("-05:00")));

            when(tripRepository.findAvailableTrips(null, date, List.of(TripStatus.SCHEDULED, TripStatus.BOARDING)))
                    .thenReturn(List.of(trip));

            // Act
            List<TripResponse> results = tripService.getTrips(origin, destination, date);

            // Assert
            assertNotNull(results);
            assertEquals(1, results.size());
            verify(tripRepository, times(1)).findAvailableTrips(null, date, List.of(TripStatus.SCHEDULED, TripStatus.BOARDING));
        }

        @Test
        @DisplayName("should throw exception when origin is null")
        void shouldThrowExceptionWhenOriginIsNull() {
            // Act & Assert
            assertThrows(IllegalArgumentException.class, () -> tripService.getTrips(null, "Destination", LocalDate.now()));
        }

        @Test
        @DisplayName("should throw exception when origin is blank")
        void shouldThrowExceptionWhenOriginIsBlank() {
            // Act & Assert
            assertThrows(IllegalArgumentException.class, () -> tripService.getTrips("   ", "Destination", LocalDate.now()));
        }

        @Test
        @DisplayName("should throw exception when destination is null")
        void shouldThrowExceptionWhenDestinationIsNull() {
            // Act & Assert
            assertThrows(IllegalArgumentException.class, () -> tripService.getTrips("Origin", null, LocalDate.now()));
        }

        @Test
        @DisplayName("should throw exception when destination is blank")
        void shouldThrowExceptionWhenDestinationIsBlank() {
            // Act & Assert
            assertThrows(IllegalArgumentException.class, () -> tripService.getTrips("Origin", "   ", LocalDate.now()));
        }

        @Test
        @DisplayName("should throw exception when date is null")
        void shouldThrowExceptionWhenDateIsNull() {
            // Act & Assert
            assertThrows(IllegalArgumentException.class, () -> tripService.getTrips("Origin", "Destination", null));
        }

        @Test
        @DisplayName("should return empty list when no trips found")
        void shouldReturnEmptyListWhenNoTripsFound() {
            // Arrange
            String origin = "Cartagena";
            String destination = "Barranquilla";
            LocalDate date = LocalDate.now();

            when(tripRepository.findAvailableTrips(null, date, List.of(TripStatus.SCHEDULED, TripStatus.BOARDING)))
                    .thenReturn(List.of());

            // Act
            List<TripResponse> results = tripService.getTrips(origin, destination, date);

            // Assert
            assertNotNull(results);
            assertEquals(0, results.size());
        }

        @Test
        @DisplayName("should filter trips by origin and destination with case-insensitive match")
        void shouldFilterTripsByCaseInsensitiveOriginAndDestination() {
            // Arrange
            String origin = "CARTAGENA";
            String destination = "barranquilla";
            LocalDate date = LocalDate.now();

            Route route = createTestRoute();
            Trip trip = createTestTrip(1L, TripStatus.SCHEDULED, date,
                    date.atTime(14, 0, 0).atOffset(java.time.ZoneOffset.of("-05:00")),
                    date.atTime(17, 0, 0).atOffset(java.time.ZoneOffset.of("-05:00")));

            when(tripRepository.findAvailableTrips(null, date, List.of(TripStatus.SCHEDULED, TripStatus.BOARDING)))
                    .thenReturn(List.of(trip));

            // Act
            List<TripResponse> results = tripService.getTrips(origin, destination, date);

            // Assert
            assertNotNull(results);
            assertEquals(1, results.size());
        }
    }

    // ==================== GET TRIP DETAILS TESTS ====================

    @Nested
    @DisplayName("getTripDetails")
    class GetTripDetailsTests {

        @Test
        @DisplayName("should retrieve trip details successfully")
        void shouldRetrieveTripDetailsSuccessfully() {
            // Arrange
            Long tripId = 1L;
            Trip trip = createTestTrip(tripId, TripStatus.SCHEDULED, LocalDate.now(),
                    LocalDate.now().atTime(14, 0, 0).atOffset(java.time.ZoneOffset.of("-05:00")),
                    LocalDate.now().atTime(17, 0, 0).atOffset(java.time.ZoneOffset.of("-05:00")));

            when(tripRepository.findByIdWithBusAndSeats(tripId)).thenReturn(Optional.of(trip));

            // Act
            TripResponse result = tripService.getTripDetails(tripId);

            // Assert
            assertThat(result).isNotNull();
            assertEquals(tripId, result.id());
            verify(tripRepository, times(1)).findByIdWithBusAndSeats(tripId);
        }

        @Test
        @DisplayName("should throw NotFoundException when trip does not exist")
        void shouldThrowNotFoundExceptionWhenTripNotExists() {
            // Arrange
            Long tripId = 999L;
            when(tripRepository.findByIdWithBusAndSeats(tripId)).thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(NotFoundException.class, () -> tripService.getTripDetails(tripId));
        }
    }

    // ==================== CHECK OVERBOOKING CONDITIONS TESTS ====================

    @Nested
    @DisplayName("checkOverbookingConditions")
    class CheckOverbookingConditionsTests {

        @Test
        @DisplayName("should allow overbooking when occupancy > 95% and < 30 minutes to departure")
        void shouldAllowOverbookingWhenConditionsMet() {
            // Arrange
            Long tripId = 1L;
            OffsetDateTime departure = OffsetDateTime.now().plusMinutes(15);
            Trip trip = createTestTrip(tripId, TripStatus.BOARDING, LocalDate.now(), departure,
                    departure.plusHours(3));
            trip.getBus().setCapacity(40);

            when(tripRepository.findById(tripId)).thenReturn(Optional.of(trip));
            when(ticketRepository.countSoldByTrip(tripId)).thenReturn(39L); // 97.5% occupancy

            // Act
            boolean result = tripService.checkOverbookingConditions(tripId);

            // Assert
            assertTrue(result);
        }

        @Test
        @DisplayName("should deny overbooking when occupancy <= 95%")
        void shouldDenyOverbookingWhenOccupancyLow() {
            // Arrange
            Long tripId = 1L;
            OffsetDateTime departure = OffsetDateTime.now().plusMinutes(15);
            Trip trip = createTestTrip(tripId, TripStatus.BOARDING, LocalDate.now(), departure,
                    departure.plusHours(3));
            trip.getBus().setCapacity(40);

            when(tripRepository.findById(tripId)).thenReturn(Optional.of(trip));
            when(ticketRepository.countSoldByTrip(tripId)).thenReturn(38L); // 95% occupancy

            // Act
            boolean result = tripService.checkOverbookingConditions(tripId);

            // Assert
            assertFalse(result);
        }

        @Test
        @DisplayName("should deny overbooking when time until departure >= 30 minutes")
        void shouldDenyOverbookingWhenTimeAbove30Minutes() {
            // Arrange
            Long tripId = 1L;
            OffsetDateTime departure = OffsetDateTime.now().plusMinutes(45);
            Trip trip = createTestTrip(tripId, TripStatus.BOARDING, LocalDate.now(), departure,
                    departure.plusHours(3));
            trip.getBus().setCapacity(40);

            when(tripRepository.findById(tripId)).thenReturn(Optional.of(trip));
            when(ticketRepository.countSoldByTrip(tripId)).thenReturn(39L); // 97.5% occupancy

            // Act
            boolean result = tripService.checkOverbookingConditions(tripId);

            // Assert
            assertFalse(result);
        }

        @Test
        @DisplayName("should throw NotFoundException when trip does not exist")
        void shouldThrowNotFoundExceptionWhenTripNotExists() {
            // Arrange
            Long tripId = 999L;
            when(tripRepository.findById(tripId)).thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(NotFoundException.class, () -> tripService.checkOverbookingConditions(tripId));
        }
    }

    // ==================== GET TRIP STATISTICS TESTS ====================

    @Nested
    @DisplayName("getTripStatistics")
    class GetTripStatisticsTests {

        @Test
        @DisplayName("should return count of sold tickets for trip")
        void shouldReturnSoldTicketsCount() {
            // Arrange
            Long tripId = 1L;
            when(tripRepository.existsById(tripId)).thenReturn(true);
            when(ticketRepository.countSoldByTrip(tripId)).thenReturn(25L);

            // Act
            Long result = tripService.getTripStatistics(tripId);

            // Assert
            assertEquals(25L, result);
            verify(ticketRepository, times(1)).countSoldByTrip(tripId);
        }

        @Test
        @DisplayName("should return zero when trip has no sold tickets")
        void shouldReturnZeroWhenNoSoldTickets() {
            // Arrange
            Long tripId = 1L;
            when(tripRepository.existsById(tripId)).thenReturn(true);
            when(ticketRepository.countSoldByTrip(tripId)).thenReturn(0L);

            // Act
            Long result = tripService.getTripStatistics(tripId);

            // Assert
            assertEquals(0L, result);
        }

        @Test
        @DisplayName("should throw NotFoundException when trip does not exist")
        void shouldThrowNotFoundExceptionWhenTripNotExists() {
            // Arrange
            Long tripId = 999L;
            when(tripRepository.existsById(tripId)).thenReturn(false);

            // Act & Assert
            assertThrows(NotFoundException.class, () -> tripService.getTripStatistics(tripId));
        }
    }

    // ==================== UPDATE TRIP STATUS TESTS ====================

    @Nested
    @DisplayName("updateTripStatus")
    class UpdateTripStatusTests {

        @Test
        @DisplayName("should update trip status successfully for valid transition")
        void shouldUpdateTripStatusSuccessfully() {
            // Arrange
            Long tripId = 1L;
            Trip trip = createTestTrip(tripId, TripStatus.SCHEDULED, LocalDate.now(),
                    LocalDate.now().atTime(14, 0, 0).atOffset(java.time.ZoneOffset.of("-05:00")),
                    LocalDate.now().atTime(17, 0, 0).atOffset(java.time.ZoneOffset.of("-05:00")));

            when(tripRepository.findById(tripId)).thenReturn(Optional.of(trip));
            when(ticketRepository.countSoldByTrip(tripId)).thenReturn(5L);

            // Act
            assertDoesNotThrow(() -> tripService.updateTripStatus(tripId, TripStatus.BOARDING));

            // Assert
            ArgumentCaptor<Trip> tripCaptor = ArgumentCaptor.forClass(Trip.class);
            verify(tripRepository, times(1)).save(tripCaptor.capture());
            assertEquals(TripStatus.BOARDING, tripCaptor.getValue().getStatus());
        }

        @Test
        @DisplayName("should throw NotFoundException when trip does not exist")
        void shouldThrowNotFoundExceptionWhenTripNotExists() {
            // Arrange
            Long tripId = 999L;
            when(tripRepository.findById(tripId)).thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(NotFoundException.class, () -> tripService.updateTripStatus(tripId, TripStatus.BOARDING));
            verify(tripRepository, never()).save(any());
        }

        @Test
        @DisplayName("should throw exception when status transition is invalid")
        void shouldThrowExceptionWhenInvalidStatusTransition() {
            // Arrange
            Long tripId = 1L;
            Trip trip = createTestTrip(tripId, TripStatus.ARRIVED, LocalDate.now(),
                    LocalDate.now().atTime(14, 0, 0).atOffset(java.time.ZoneOffset.of("-05:00")),
                    LocalDate.now().atTime(17, 0, 0).atOffset(java.time.ZoneOffset.of("-05:00")));

            when(tripRepository.findById(tripId)).thenReturn(Optional.of(trip));
            when(ticketRepository.countSoldByTrip(tripId)).thenReturn(0L);

            // Act & Assert
            assertThrows(IllegalArgumentException.class, () -> tripService.updateTripStatus(tripId, TripStatus.SCHEDULED));
            verify(tripRepository, never()).save(any());
        }

        @Test
        @DisplayName("should allow SCHEDULED to CANCELLED transition")
        void shouldAllowScheduledToCancelledTransition() {
            // Arrange
            Long tripId = 1L;
            Trip trip = createTestTrip(tripId, TripStatus.SCHEDULED, LocalDate.now(),
                    LocalDate.now().atTime(14, 0, 0).atOffset(java.time.ZoneOffset.of("-05:00")),
                    LocalDate.now().atTime(17, 0, 0).atOffset(java.time.ZoneOffset.of("-05:00")));

            when(tripRepository.findById(tripId)).thenReturn(Optional.of(trip));
            when(ticketRepository.countSoldByTrip(tripId)).thenReturn(0L);

            // Act
            assertDoesNotThrow(() -> tripService.updateTripStatus(tripId, TripStatus.CANCELLED));

            // Assert
            verify(tripRepository, times(1)).save(any());
        }

        @Test
        @DisplayName("should throw exception when cancelling trip with sold tickets")
        void shouldThrowExceptionWhenCancellingTripWithSoldTickets() {
            // Arrange
            Long tripId = 1L;
            Trip trip = createTestTrip(tripId, TripStatus.SCHEDULED, LocalDate.now(),
                    LocalDate.now().atTime(14, 0, 0).atOffset(java.time.ZoneOffset.of("-05:00")),
                    LocalDate.now().atTime(17, 0, 0).atOffset(java.time.ZoneOffset.of("-05:00")));

            when(tripRepository.findById(tripId)).thenReturn(Optional.of(trip));
            when(ticketRepository.countSoldByTrip(tripId)).thenReturn(5L);

            // Act & Assert
            assertThrows(IllegalArgumentException.class, () -> tripService.updateTripStatus(tripId, TripStatus.CANCELLED));
            verify(tripRepository, never()).save(any());
        }
    }
}
