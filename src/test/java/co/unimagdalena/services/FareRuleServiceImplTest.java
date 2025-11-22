package co.unimagdalena.services;

import co.unimagdalena.api.dto.FareRuleDto.*;
import co.unimagdalena.domine.entities.*;
import co.unimagdalena.domine.repositories.*;
import co.unimagdalena.exception.NotFoundException;
import co.unimagdalena.services.impl.FareRuleServiceImpl;
import co.unimagdalena.services.mapper.FareRuleMapper;
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
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class FareRuleServiceImplTest {

    @Mock
    private FareRuleRepository fareRuleRepository;

    @Mock
    private RouteRepository routeRepository;

    @Mock
    private StopRepository stopRepository;

    @Mock
    private PassengerRepository passengerRepository;

    @Mock
    private SeatRepository seatRepository;

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private BusRepository busRepository;

    @Spy
    private FareRuleMapper fareRuleMapper = Mappers.getMapper(FareRuleMapper.class);

    @InjectMocks
    private FareRuleServiceImpl fareRuleService;

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

    private Stop createStop(Long id, String name, Integer order,
                            double latitude, double longitude, Route route) {
        return Stop.builder()
                .id(id)
                .name(name)
                .order(order)
                .latitude(latitude)
                .longitude(longitude)
                .route(route)
                .fareRulesFrom(new ArrayList<>())
                .fareRulesTo(new ArrayList<>())
                .build();
    }

    private FareRule createFareRule(Long id, BigDecimal basePrice,
                                    DynamicPricing dynamicPricing,
                                    Map<String, Double> discounts,
                                    Route route, Stop fromStop, Stop toStop) {
        return FareRule.builder()
                .id(id)
                .basePrice(basePrice)
                .dynamicPricing(dynamicPricing)
                .discounts(discounts != null ? discounts : new HashMap<>())
                .route(route)
                .fromStop(fromStop)
                .toStop(toStop)
                .build();
    }

    private Bus createBus(Long id, String plate, Integer capacity) {
        return Bus.builder()
                .id(id)
                .plate(plate)
                .capacity(capacity)
                .status(BusStatus.AVAILABLE)
                .build();
    }

    private Seat createSeat(Long id, Integer number, SeatType type, Bus bus) {
        return Seat.builder()
                .id(id)
                .number(number)
                .type(type)
                .price(new BigDecimal("50000"))
                .status(SeatStatus.AVAILABLE)
                .bus(bus)
                .build();
    }

    private Passenger createPassenger(Long id, LocalDate birthDate) {
        return Passenger.builder()
                .id(id)
                .fullName("Test Passenger")
                .birthDate(birthDate)
                .documentNumber("1234567890")
                .phoneNumber("3001234567")
                .build();
    }

    // ======================================================================
    // TESTS - createFareRule
    // ======================================================================

    @Test
    @DisplayName("Debe crear una Regla de Tarifa exitosamente con parámetros válidos")
    void shouldCreateFareRuleSuccessfully() {
        // Arrange
        Long routeId = 1L;
        Long fromStopId = 1L;
        Long toStopId = 2L;

        Route route = createRoute(routeId, "R001", "Ruta Norte", "Bogotá", "Medellín");
        Stop fromStop = createStop(fromStopId, "Bogotá", 1, 4.7110, -74.0721, route);
        Stop toStop = createStop(toStopId, "Medellín", 2, 6.2442, -75.5812, route);

        Map<String, Double> discounts = new HashMap<>();
        discounts.put("student", 0.15);
        discounts.put("senior", 0.20);

        FareRuleCreateRequest createRequest = new FareRuleCreateRequest(
                new BigDecimal("50000"),
                DynamicPricing.ON,
                discounts,
                routeId,
                fromStopId,
                toStopId
        );

        when(routeRepository.findById(routeId)).thenReturn(Optional.of(route));
        when(stopRepository.findById(fromStopId)).thenReturn(Optional.of(fromStop));
        when(stopRepository.findById(toStopId)).thenReturn(Optional.of(toStop));
        when(fareRuleRepository.save(any(FareRule.class))).thenAnswer(invocation -> {
            FareRule saved = invocation.getArgument(0);
            saved.setId(1L);
            return saved;
        });

        // Act
        FareRuleResponse response = fareRuleService.createFareRule(createRequest);

        // Assert
        assertNotNull(response);
        assertEquals(new BigDecimal("50000"), response.basePrice());
        assertEquals(DynamicPricing.ON, response.dynamicPricing());
        assertEquals(2, response.discounts().size());
        verify(fareRuleRepository, times(1)).save(any(FareRule.class));
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando la Ruta no existe")
    void shouldThrowExceptionWhenRouteDoesNotExist() {
        // Arrange
        FareRuleCreateRequest createRequest = new FareRuleCreateRequest(
                new BigDecimal("50000"),
                DynamicPricing.ON,
                new HashMap<>(),
                999L,
                1L,
                2L
        );

        when(routeRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> fareRuleService.createFareRule(createRequest));

        assertTrue(exception.getMessage().contains("Ruta no encontrada"));
        verify(fareRuleRepository, never()).save(any(FareRule.class));
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando la Parada origen no existe")
    void shouldThrowExceptionWhenFromStopDoesNotExist() {
        // Arrange
        Long routeId = 1L;
        Route route = createRoute(routeId, "R001", "Ruta Norte", "Bogotá", "Medellín");

        FareRuleCreateRequest createRequest = new FareRuleCreateRequest(
                new BigDecimal("50000"),
                DynamicPricing.ON,
                new HashMap<>(),
                routeId,
                999L,
                2L
        );

        when(routeRepository.findById(routeId)).thenReturn(Optional.of(route));
        when(stopRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> fareRuleService.createFareRule(createRequest));

        assertTrue(exception.getMessage().contains("Parada origen"));
        verify(fareRuleRepository, never()).save(any(FareRule.class));
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando la Parada destino no existe")
    void shouldThrowExceptionWhenToStopDoesNotExist() {
        // Arrange
        Long routeId = 1L;
        Long fromStopId = 1L;

        Route route = createRoute(routeId, "R001", "Ruta Norte", "Bogotá", "Medellín");
        Stop fromStop = createStop(fromStopId, "Bogotá", 1, 4.7110, -74.0721, route);

        FareRuleCreateRequest createRequest = new FareRuleCreateRequest(
                new BigDecimal("50000"),
                DynamicPricing.ON,
                new HashMap<>(),
                routeId,
                fromStopId,
                999L
        );

        when(routeRepository.findById(routeId)).thenReturn(Optional.of(route));
        when(stopRepository.findById(fromStopId)).thenReturn(Optional.of(fromStop));
        when(stopRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> fareRuleService.createFareRule(createRequest));

        assertTrue(exception.getMessage().contains("Parada destino"));
        verify(fareRuleRepository, never()).save(any(FareRule.class));
    }

    @Test
    @DisplayName("Debe validar que ambas paradas pertenecen a la misma ruta")
    void shouldValidateStopsBelongToRoute() {
        // Arrange
        Long routeId = 1L;
        Long differentRouteId = 2L;
        Long fromStopId = 1L;
        Long toStopId = 2L;

        Route route1 = createRoute(routeId, "R001", "Ruta Norte", "Bogotá", "Medellín");
        Route route2 = createRoute(differentRouteId, "R002", "Ruta Sur", "Cali", "Bucaramanga");

        Stop fromStop = createStop(fromStopId, "Bogotá", 1, 4.7110, -74.0721, route1);
        Stop toStop = createStop(toStopId, "Medellín", 2, 6.2442, -75.5812, route2);

        FareRuleCreateRequest createRequest = new FareRuleCreateRequest(
                new BigDecimal("50000"),
                DynamicPricing.ON,
                new HashMap<>(),
                routeId,
                fromStopId,
                toStopId
        );

        when(routeRepository.findById(routeId)).thenReturn(Optional.of(route1));
        when(stopRepository.findById(fromStopId)).thenReturn(Optional.of(fromStop));
        when(stopRepository.findById(toStopId)).thenReturn(Optional.of(toStop));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> fareRuleService.createFareRule(createRequest));

        assertTrue(exception.getMessage().contains("no pertenece a la ruta"));
        verify(fareRuleRepository, never()).save(any(FareRule.class));
    }

    @Test
    @DisplayName("Debe validar que el orden de paradas sea correcto (origen < destino)")
    void shouldValidateStopOrder() {
        // Arrange
        Long routeId = 1L;
        Long fromStopId = 1L;
        Long toStopId = 2L;

        Route route = createRoute(routeId, "R001", "Ruta Norte", "Bogotá", "Medellín");
        Stop fromStop = createStop(fromStopId, "Bogotá", 3, 4.7110, -74.0721, route);
        Stop toStop = createStop(toStopId, "Medellín", 2, 6.2442, -75.5812, route);

        FareRuleCreateRequest createRequest = new FareRuleCreateRequest(
                new BigDecimal("50000"),
                DynamicPricing.ON,
                new HashMap<>(),
                routeId,
                fromStopId,
                toStopId
        );

        when(routeRepository.findById(routeId)).thenReturn(Optional.of(route));
        when(stopRepository.findById(fromStopId)).thenReturn(Optional.of(fromStop));
        when(stopRepository.findById(toStopId)).thenReturn(Optional.of(toStop));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> fareRuleService.createFareRule(createRequest));

        assertTrue(exception.getMessage().contains("orden"));
        verify(fareRuleRepository, never()).save(any(FareRule.class));
    }

    // ======================================================================
    // TESTS - updateFareRule
    // ======================================================================

    @Test
    @DisplayName("Debe actualizar una Regla de Tarifa exitosamente")
    void shouldUpdateFareRuleSuccessfully() {
        // Arrange
        Long fareRuleId = 1L;
        Route route = createRoute(1L, "R001", "Ruta Norte", "Bogotá", "Medellín");
        Stop fromStop = createStop(1L, "Bogotá", 1, 4.7110, -74.0721, route);
        Stop toStop = createStop(2L, "Medellín", 2, 6.2442, -75.5812, route);

        FareRule fareRule = createFareRule(fareRuleId, new BigDecimal("50000"),
                DynamicPricing.ON, new HashMap<>(), route, fromStop, toStop);

        FareRuleUpdateRequest updateRequest = new FareRuleUpdateRequest(
                new BigDecimal("55000"),
                DynamicPricing.OFF,
                new HashMap<>(),
                null, null, null
        );

        when(fareRuleRepository.findById(fareRuleId)).thenReturn(Optional.of(fareRule));
        when(fareRuleRepository.save(any(FareRule.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        fareRuleService.updateFareRule(fareRuleId, updateRequest);

        // Assert
        verify(fareRuleRepository, times(1)).save(any(FareRule.class));
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando la Regla de Tarifa no existe para actualizar")
    void shouldThrowExceptionWhenFareRuleNotFoundForUpdate() {
        // Arrange
        FareRuleUpdateRequest updateRequest = new FareRuleUpdateRequest(
                new BigDecimal("55000"),
                DynamicPricing.OFF,
                new HashMap<>(),
                null, null, null
        );

        when(fareRuleRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> fareRuleService.updateFareRule(999L, updateRequest));

        assertTrue(exception.getMessage().contains("FareRule no encontrada"));
        verify(fareRuleRepository, never()).save(any(FareRule.class));
    }

    // ======================================================================
    // TESTS - deleteFareRule
    // ======================================================================

    @Test
    @DisplayName("Debe eliminar una Regla de Tarifa exitosamente")
    void shouldDeleteFareRuleSuccessfully() {
        // Arrange
        Long fareRuleId = 1L;
        Route route = createRoute(1L, "R001", "Ruta Norte", "Bogotá", "Medellín");
        Stop fromStop = createStop(1L, "Bogotá", 1, 4.7110, -74.0721, route);
        Stop toStop = createStop(2L, "Medellín", 2, 6.2442, -75.5812, route);

        FareRule fareRule = createFareRule(fareRuleId, new BigDecimal("50000"),
                DynamicPricing.ON, new HashMap<>(), route, fromStop, toStop);

        when(fareRuleRepository.findById(fareRuleId)).thenReturn(Optional.of(fareRule));
        doNothing().when(fareRuleRepository).delete(any(FareRule.class));

        // Act
        fareRuleService.deleteFareRule(fareRuleId);

        // Assert
        verify(fareRuleRepository, times(1)).delete(any(FareRule.class));
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando la Regla de Tarifa no existe para eliminar")
    void shouldThrowExceptionWhenFareRuleNotFoundForDelete() {
        // Arrange
        when(fareRuleRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> fareRuleService.deleteFareRule(999L));

        assertTrue(exception.getMessage().contains("FareRule no encontrada"));
        verify(fareRuleRepository, never()).delete(any(FareRule.class));
    }

    // ======================================================================
    // TESTS - getFareRule
    // ======================================================================

    @Test
    @DisplayName("Debe obtener una Regla de Tarifa por ID exitosamente")
    void shouldGetFareRuleSuccessfully() {
        // Arrange
        Long fareRuleId = 1L;
        Route route = createRoute(1L, "R001", "Ruta Norte", "Bogotá", "Medellín");
        Stop fromStop = createStop(1L, "Bogotá", 1, 4.7110, -74.0721, route);
        Stop toStop = createStop(2L, "Medellín", 2, 6.2442, -75.5812, route);

        FareRule fareRule = createFareRule(fareRuleId, new BigDecimal("50000"),
                DynamicPricing.ON, new HashMap<>(), route, fromStop, toStop);

        when(fareRuleRepository.findById(fareRuleId)).thenReturn(Optional.of(fareRule));

        // Act
        FareRuleResponse response = fareRuleService.getFareRule(fareRuleId);

        // Assert
        assertNotNull(response);
        assertEquals(fareRuleId, response.id());
        assertEquals(new BigDecimal("50000"), response.basePrice());
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando la Regla de Tarifa no existe")
    void shouldThrowExceptionWhenFareRuleNotFound() {
        // Arrange
        when(fareRuleRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> fareRuleService.getFareRule(999L));

        assertTrue(exception.getMessage().contains("FareRule no encontrada"));
    }

    // ======================================================================
    // TESTS - getAllFareRules
    // ======================================================================

    @Test
    @DisplayName("Debe obtener todas las Reglas de Tarifa")
    void shouldGetAllFareRulesSuccessfully() {
        // Arrange
        Route route = createRoute(1L, "R001", "Ruta Norte", "Bogotá", "Medellín");
        Stop fromStop = createStop(1L, "Bogotá", 1, 4.7110, -74.0721, route);
        Stop toStop = createStop(2L, "Medellín", 2, 6.2442, -75.5812, route);

        FareRule rule1 = createFareRule(1L, new BigDecimal("50000"),
                DynamicPricing.ON, new HashMap<>(), route, fromStop, toStop);
        FareRule rule2 = createFareRule(2L, new BigDecimal("60000"),
                DynamicPricing.OFF, new HashMap<>(), route, toStop, fromStop);

        when(fareRuleRepository.findAll()).thenReturn(List.of(rule1, rule2));

        // Act
        List<FareRuleResponse> responses = fareRuleService.getAllFareRules();

        // Assert
        assertNotNull(responses);
        assertEquals(2, responses.size());
    }

    @Test
    @DisplayName("Debe retornar lista vacía cuando no hay Reglas de Tarifa")
    void shouldReturnEmptyListWhenNoFareRules() {
        // Arrange
        when(fareRuleRepository.findAll()).thenReturn(new ArrayList<>());

        // Act
        List<FareRuleResponse> responses = fareRuleService.getAllFareRules();

        // Assert
        assertNotNull(responses);
        assertTrue(responses.isEmpty());
    }

    // ======================================================================
    // TESTS - getFareRulesByRouteId
    // ======================================================================

    @Test
    @DisplayName("Debe obtener Reglas de Tarifa filtradas por Ruta")
    void shouldGetFareRulesByRouteSuccessfully() {
        // Arrange
        Long routeId = 1L;
        Route route = createRoute(routeId, "R001", "Ruta Norte", "Bogotá", "Medellín");
        Stop fromStop = createStop(1L, "Bogotá", 1, 4.7110, -74.0721, route);
        Stop toStop = createStop(2L, "Medellín", 2, 6.2442, -75.5812, route);

        FareRule rule1 = createFareRule(1L, new BigDecimal("50000"),
                DynamicPricing.ON, new HashMap<>(), route, fromStop, toStop);

        when(routeRepository.existsById(routeId)).thenReturn(true);
        when(fareRuleRepository.findByRouteId(routeId)).thenReturn(List.of(rule1));

        // Act
        List<FareRuleResponse> responses = fareRuleService.getFareRulesByRouteId(routeId);

        // Assert
        assertNotNull(responses);
        assertEquals(1, responses.size());
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando la Ruta no existe")
    void shouldThrowExceptionWhenRouteNotExistsForFareRules() {
        // Arrange
        when(routeRepository.existsById(999L)).thenReturn(false);

        // Act & Assert
        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> fareRuleService.getFareRulesByRouteId(999L));

        assertTrue(exception.getMessage().contains("Ruta no encontrada"));
    }

    // ======================================================================
    // TESTS - getFinalTicketPrice (Método complejo)
    // ======================================================================

    @Test
    @DisplayName("Debe calcular precio final con descuento por edad (estudiante)")
    void shouldCalculatePriceWithStudentDiscount() {
        // Arrange
        Long routeId = 1L;
        Long fromStopId = 1L;
        Long toStopId = 2L;
        Long passengerId = 1L;
        Long busId = 1L;
        Long tripId = 1L;
        String seatNumber = "1";

        BigDecimal basePrice = new BigDecimal("50000");
        Map<String, Double> discounts = new HashMap<>();
        discounts.put("student", 0.15); // 15% descuento

        Route route = createRoute(routeId, "R001", "Ruta Norte", "Bogotá", "Medellín");
        Stop fromStop = createStop(fromStopId, "Bogotá", 1, 4.7110, -74.0721, route);
        Stop toStop = createStop(toStopId, "Medellín", 2, 6.2442, -75.5812, route);

        FareRule rule = createFareRule(1L, basePrice, DynamicPricing.OFF, discounts, route, fromStop, toStop);

        Bus bus = createBus(busId, "ABC123", 40);
        Seat seat = createSeat(1L, 1, SeatType.STANDARD, bus);

        // Pasajero de 22 años (estudiante)
        LocalDate birthDate = LocalDate.now().minusYears(22);
        Passenger passenger = createPassenger(passengerId, birthDate);

        when(fareRuleRepository.findByRouteIdAndFromStopIdAndToStopId(routeId, fromStopId, toStopId))
                .thenReturn(Optional.of(rule));
        when(passengerRepository.findPassengerById(passengerId)).thenReturn(Optional.of(passenger));
        when(seatRepository.findByBusIdAndNumber(busId, 1)).thenReturn(Optional.of(seat));

        // Act
        BigDecimal price = fareRuleService.getFinalTicketPrice(routeId, fromStopId, toStopId,
                passengerId, busId, seatNumber, tripId);

        // Assert
        assertNotNull(price);
        // Precio = 50000 * (1 - 0.15) = 42500
        assertEquals(new BigDecimal("42500.00"), price);
    }

    @Test
    @DisplayName("Debe calcular precio final con descuento por edad (adulto mayor)")
    void shouldCalculatePriceWithSeniorDiscount() {
        // Arrange
        Long routeId = 1L;
        Long fromStopId = 1L;
        Long toStopId = 2L;
        Long passengerId = 1L;
        Long busId = 1L;
        Long tripId = 1L;

        BigDecimal basePrice = new BigDecimal("50000");
        Map<String, Double> discounts = new HashMap<>();
        discounts.put("senior", 0.20); // 20% descuento

        Route route = createRoute(routeId, "R001", "Ruta Norte", "Bogotá", "Medellín");
        Stop fromStop = createStop(fromStopId, "Bogotá", 1, 4.7110, -74.0721, route);
        Stop toStop = createStop(toStopId, "Medellín", 2, 6.2442, -75.5812, route);

        FareRule rule = createFareRule(1L, basePrice, DynamicPricing.OFF, discounts, route, fromStop, toStop);

        Bus bus = createBus(busId, "ABC123", 40);
        Seat seat = createSeat(1L, 1, SeatType.STANDARD, bus);

        // Pasajero de 65 años (adulto mayor)
        LocalDate birthDate = LocalDate.now().minusYears(65);
        Passenger passenger = createPassenger(passengerId, birthDate);

        when(fareRuleRepository.findByRouteIdAndFromStopIdAndToStopId(routeId, fromStopId, toStopId))
                .thenReturn(Optional.of(rule));
        when(passengerRepository.findPassengerById(passengerId)).thenReturn(Optional.of(passenger));
        when(seatRepository.findByBusIdAndNumber(busId, 1)).thenReturn(Optional.of(seat));

        // Act
        BigDecimal price = fareRuleService.getFinalTicketPrice(routeId, fromStopId, toStopId,
                passengerId, busId, "1", tripId);

        // Assert
        // Precio = 50000 * (1 - 0.20) = 40000
        assertEquals(new BigDecimal("40000.0"), price);
    }

    @Test
    @DisplayName("Debe aplicar recargo por asiento preferencial (15%)")
    void shouldApplySeatSurcharge() {
        // Arrange
        Long routeId = 1L;
        Long fromStopId = 1L;
        Long toStopId = 2L;
        Long passengerId = 1L;
        Long busId = 1L;
        Long tripId = 1L;

        BigDecimal basePrice = new BigDecimal("50000");
        Map<String, Double> discounts = new HashMap<>();

        Route route = createRoute(routeId, "R001", "Ruta Norte", "Bogotá", "Medellín");
        Stop fromStop = createStop(fromStopId, "Bogotá", 1, 4.7110, -74.0721, route);
        Stop toStop = createStop(toStopId, "Medellín", 2, 6.2442, -75.5812, route);

        FareRule rule = createFareRule(1L, basePrice, DynamicPricing.OFF, discounts, route, fromStop, toStop);

        Bus bus = createBus(busId, "ABC123", 40);
        Seat seat = createSeat(1L, 1, SeatType.PREFERENTIAL, bus);

        LocalDate birthDate = LocalDate.now().minusYears(30);
        Passenger passenger = createPassenger(passengerId, birthDate);

        when(fareRuleRepository.findByRouteIdAndFromStopIdAndToStopId(routeId, fromStopId, toStopId))
                .thenReturn(Optional.of(rule));
        when(passengerRepository.findPassengerById(passengerId)).thenReturn(Optional.of(passenger));
        when(seatRepository.findByBusIdAndNumber(busId, 1)).thenReturn(Optional.of(seat));

        // Act
        BigDecimal price = fareRuleService.getFinalTicketPrice(routeId, fromStopId, toStopId,
                passengerId, busId, "1", tripId);

        // Assert
        // Precio = 50000 + (50000 * 0.15) = 57500
        assertEquals(new BigDecimal("57500.00"), price);
    }

    @Test
    @DisplayName("Debe aplicar recargo dinámico cuando ocupancia >= 85%")
    void shouldApplyDynamicSurchargeHighOccupancy() {
        // Arrange
        Long routeId = 1L;
        Long fromStopId = 1L;
        Long toStopId = 2L;
        Long passengerId = 1L;
        Long busId = 1L;
        Long tripId = 1L;

        BigDecimal basePrice = new BigDecimal("50000");
        Map<String, Double> discounts = new HashMap<>();

        Route route = createRoute(routeId, "R001", "Ruta Norte", "Bogotá", "Medellín");
        Stop fromStop = createStop(fromStopId, "Bogotá", 1, 4.7110, -74.0721, route);
        Stop toStop = createStop(toStopId, "Medellín", 2, 6.2442, -75.5812, route);

        FareRule rule = createFareRule(1L, basePrice, DynamicPricing.ON, discounts, route, fromStop, toStop);

        Bus bus = createBus(busId, "ABC123", 40);
        Seat seat = createSeat(1L, 1, SeatType.STANDARD, bus);

        LocalDate birthDate = LocalDate.now().minusYears(30);
        Passenger passenger = createPassenger(passengerId, birthDate);

        // 35 tickets vendidos de 40 = 87.5% ocupancia
        when(fareRuleRepository.findByRouteIdAndFromStopIdAndToStopId(routeId, fromStopId, toStopId))
                .thenReturn(Optional.of(rule));
        when(passengerRepository.findPassengerById(passengerId)).thenReturn(Optional.of(passenger));
        when(seatRepository.findByBusIdAndNumber(busId, 1)).thenReturn(Optional.of(seat));
        when(ticketRepository.countSoldByTrip(tripId)).thenReturn(35L);
        when(busRepository.findById(busId)).thenReturn(Optional.of(bus));

        // Act
        BigDecimal price = fareRuleService.getFinalTicketPrice(routeId, fromStopId, toStopId,
                passengerId, busId, "1", tripId);

        // Assert
        // Precio = 50000 + (50000 * 0.20) = 60000
        assertEquals(new BigDecimal("60000.00"), price);
    }

    @Test
    @DisplayName("Debe aplicar recargo dinámico cuando ocupancia está entre 70% y 85%")
    void shouldApplyDynamicSurchargeMediumOccupancy() {
        // Arrange
        Long routeId = 1L;
        Long fromStopId = 1L;
        Long toStopId = 2L;
        Long passengerId = 1L;
        Long busId = 1L;
        Long tripId = 1L;

        BigDecimal basePrice = new BigDecimal("50000");
        Map<String, Double> discounts = new HashMap<>();

        Route route = createRoute(routeId, "R001", "Ruta Norte", "Bogotá", "Medellín");
        Stop fromStop = createStop(fromStopId, "Bogotá", 1, 4.7110, -74.0721, route);
        Stop toStop = createStop(toStopId, "Medellín", 2, 6.2442, -75.5812, route);

        FareRule rule = createFareRule(1L, basePrice, DynamicPricing.ON, discounts, route, fromStop, toStop);

        Bus bus = createBus(busId, "ABC123", 40);
        Seat seat = createSeat(1L, 1, SeatType.STANDARD, bus);

        LocalDate birthDate = LocalDate.now().minusYears(30);
        Passenger passenger = createPassenger(passengerId, birthDate);

        // 28 tickets vendidos de 40 = 70% ocupancia
        when(fareRuleRepository.findByRouteIdAndFromStopIdAndToStopId(routeId, fromStopId, toStopId))
                .thenReturn(Optional.of(rule));
        when(passengerRepository.findPassengerById(passengerId)).thenReturn(Optional.of(passenger));
        when(seatRepository.findByBusIdAndNumber(busId, 1)).thenReturn(Optional.of(seat));
        when(ticketRepository.countSoldByTrip(tripId)).thenReturn(28L);
        when(busRepository.findById(busId)).thenReturn(Optional.of(bus));

        // Act
        BigDecimal price = fareRuleService.getFinalTicketPrice(routeId, fromStopId, toStopId,
                passengerId, busId, "1", tripId);

        // Assert
        // Precio = 50000 + (50000 * 0.10) = 55000
        assertEquals(new BigDecimal("55000.00"), price);
    }

    @Test
    @DisplayName("Debe no aplicar recargo cuando ocupancia < 70% con precio dinámico")
    void shouldNotApplyDynamicSurchargeWhenLowOccupancy() {
        // Arrange
        Long routeId = 1L;
        Long fromStopId = 1L;
        Long toStopId = 2L;
        Long passengerId = 1L;
        Long busId = 1L;
        Long tripId = 1L;

        BigDecimal basePrice = new BigDecimal("50000");
        Map<String, Double> discounts = new HashMap<>();

        Route route = createRoute(routeId, "R001", "Ruta Norte", "Bogotá", "Medellín");
        Stop fromStop = createStop(fromStopId, "Bogotá", 1, 4.7110, -74.0721, route);
        Stop toStop = createStop(toStopId, "Medellín", 2, 6.2442, -75.5812, route);

        FareRule rule = createFareRule(1L, basePrice, DynamicPricing.ON, discounts, route, fromStop, toStop);

        Bus bus = createBus(busId, "ABC123", 40);
        Seat seat = createSeat(1L, 1, SeatType.STANDARD, bus);

        LocalDate birthDate = LocalDate.now().minusYears(30);
        Passenger passenger = createPassenger(passengerId, birthDate);

        // 25 tickets vendidos de 40 = 62.5% ocupancia
        when(fareRuleRepository.findByRouteIdAndFromStopIdAndToStopId(routeId, fromStopId, toStopId))
                .thenReturn(Optional.of(rule));
        when(passengerRepository.findPassengerById(passengerId)).thenReturn(Optional.of(passenger));
        when(seatRepository.findByBusIdAndNumber(busId, 1)).thenReturn(Optional.of(seat));
        when(ticketRepository.countSoldByTrip(tripId)).thenReturn(25L);
        when(busRepository.findById(busId)).thenReturn(Optional.of(bus));

        // Act
        BigDecimal price = fareRuleService.getFinalTicketPrice(routeId, fromStopId, toStopId,
                passengerId, busId, "1", tripId);

        // Assert
        assertEquals(basePrice, price);
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando la Regla de Tarifa no existe al calcular precio")
    void shouldThrowExceptionWhenFareRuleNotFoundForPrice() {
        // Arrange
        when(fareRuleRepository.findByRouteIdAndFromStopIdAndToStopId(anyLong(), anyLong(), anyLong()))
                .thenReturn(Optional.empty());

        // Act & Assert
        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> fareRuleService.getFinalTicketPrice(1L, 1L, 2L, 1L, 1L, "1", 1L));

        assertTrue(exception.getMessage().contains("No existe FareRule"));
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando el Pasajero no existe al calcular precio")
    void shouldThrowExceptionWhenPassengerNotFoundForPrice() {
        // Arrange
        Long routeId = 1L;
        Route route = createRoute(routeId, "R001", "Ruta Norte", "Bogotá", "Medellín");
        Stop fromStop = createStop(1L, "Bogotá", 1, 4.7110, -74.0721, route);
        Stop toStop = createStop(2L, "Medellín", 2, 6.2442, -75.5812, route);

        FareRule rule = createFareRule(1L, new BigDecimal("50000"), DynamicPricing.OFF,
                new HashMap<>(), route, fromStop, toStop);

        when(fareRuleRepository.findByRouteIdAndFromStopIdAndToStopId(routeId, 1L, 2L))
                .thenReturn(Optional.of(rule));
        when(passengerRepository.findPassengerById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> fareRuleService.getFinalTicketPrice(routeId, 1L, 2L, 999L, 1L, "1", 1L));

        assertTrue(exception.getMessage().contains("Pasajero no encontrado"));
    }

    @Test
    @DisplayName("Debe combinar descuento edad + recargo preferencial correctamente")
    void shouldCombineAgeDiscountAndSeatSurcharge() {
        // Arrange
        Long routeId = 1L;
        Long fromStopId = 1L;
        Long toStopId = 2L;
        Long passengerId = 1L;
        Long busId = 1L;
        Long tripId = 1L;

        BigDecimal basePrice = new BigDecimal("50000");
        Map<String, Double> discounts = new HashMap<>();
        discounts.put("student", 0.15);

        Route route = createRoute(routeId, "R001", "Ruta Norte", "Bogotá", "Medellín");
        Stop fromStop = createStop(fromStopId, "Bogotá", 1, 4.7110, -74.0721, route);
        Stop toStop = createStop(toStopId, "Medellín", 2, 6.2442, -75.5812, route);

        FareRule rule = createFareRule(1L, basePrice, DynamicPricing.OFF, discounts, route, fromStop, toStop);

        Bus bus = createBus(busId, "ABC123", 40);
        Seat seat = createSeat(1L, 1, SeatType.PREFERENTIAL, bus);

        LocalDate birthDate = LocalDate.now().minusYears(22);
        Passenger passenger = createPassenger(passengerId, birthDate);

        when(fareRuleRepository.findByRouteIdAndFromStopIdAndToStopId(routeId, fromStopId, toStopId))
                .thenReturn(Optional.of(rule));
        when(passengerRepository.findPassengerById(passengerId)).thenReturn(Optional.of(passenger));
        when(seatRepository.findByBusIdAndNumber(busId, 1)).thenReturn(Optional.of(seat));

        // Act
        BigDecimal price = fareRuleService.getFinalTicketPrice(routeId, fromStopId, toStopId,
                passengerId, busId, "1", tripId);

        // Assert
        // Precio = [50000 * (1 - 0.15)] + [50000 * 0.15] = 42500 + 7500 = 50000
        assertEquals(new BigDecimal("50000.00"), price);
    }
}