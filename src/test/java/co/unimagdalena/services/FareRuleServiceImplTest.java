package co.unimagdalena.services;

import co.unimagdalena.api.dto.FareRuleDto.*;
import co.unimagdalena.services.mapper.FareRuleMapper;
import co.unimagdalena.domine.entities.*;
import co.unimagdalena.exception.NotFoundException;
import co.unimagdalena.domine.repositories.*;
import co.unimagdalena.services.impl.FareRuleServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FareRuleServiceImplTest {

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

    @Mock
    private FareRuleMapper mapper;

    @InjectMocks
    private FareRuleServiceImpl fareRuleService;

    private Route route;
    private Stop fromStop;
    private Stop toStop;
    private FareRule fareRule;
    private FareRuleCreateRequest createRequest;
    private FareRuleUpdateRequest updateRequest;
    private FareRuleResponse response;
    private Map<String, Double> discounts;

    @BeforeEach
    void setUp() {
        // Setup basic entities
        route = createRoute();
        fromStop = createStop(1L, "Origin Stop", 1, route);
        toStop = createStop(2L, "Destination Stop", 5, route);
        discounts = createDiscounts();

        // Setup FareRule
        fareRule = createFareRule(route, fromStop, toStop, discounts);

        // Setup DTOs
        createRequest = createFareRuleCreateRequest();
        updateRequest = createFareRuleUpdateRequest();
        response = createFareRuleResponse();
    }

    // ============================================================
    //                  CREATE FARE RULE TESTS
    // ============================================================

    @Test
    @DisplayName("Should create fare rule successfully")
    void shouldCreateFareRuleSuccessfully() {
        // Given
        when(routeRepository.findById(1L)).thenReturn(Optional.of(route));
        when(stopRepository.findById(1L)).thenReturn(Optional.of(fromStop));
        when(stopRepository.findById(2L)).thenReturn(Optional.of(toStop));
        when(mapper.toEntity(createRequest)).thenReturn(fareRule);
        when(fareRuleRepository.save(any(FareRule.class))).thenReturn(fareRule);
        when(mapper.toResponse(fareRule)).thenReturn(response);

        // When
        FareRuleResponse result = fareRuleService.createFareRule(createRequest);

        // Then
        assertNotNull(result);
        assertEquals(response.id(), result.id());
        verify(routeRepository).findById(1L);
        verify(stopRepository, times(3)).findById(anyLong());
        verify(fareRuleRepository).save(any(FareRule.class));
    }

    @Test
    @DisplayName("Should throw exception when route not found during creation")
    void shouldThrowExceptionWhenRouteNotFoundDuringCreation() {
        // Given
        when(routeRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(NotFoundException.class, () -> fareRuleService.createFareRule(createRequest));
        verify(routeRepository).findById(1L);
        verify(fareRuleRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception when from stop not found during creation")
    void shouldThrowExceptionWhenFromStopNotFoundDuringCreation() {
        // Given
        when(routeRepository.findById(1L)).thenReturn(Optional.of(route));
        when(stopRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(NotFoundException.class, () -> fareRuleService.createFareRule(createRequest));
        verify(stopRepository).findById(1L);
        verify(fareRuleRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception when to stop not found during creation")
    void shouldThrowExceptionWhenToStopNotFoundDuringCreation() {
        // Given
        when(routeRepository.findById(1L)).thenReturn(Optional.of(route));
        when(stopRepository.findById(1L)).thenReturn(Optional.of(fromStop));
        when(stopRepository.findById(2L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(NotFoundException.class, () -> fareRuleService.createFareRule(createRequest));
        verify(stopRepository).findById(2L);
        verify(fareRuleRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception when from stop does not belong to route")
    void shouldThrowExceptionWhenFromStopDoesNotBelongToRoute() {
        // Given
        Route differentRoute = Route.builder().id(99L).code("ROUTE99").build();
        Stop invalidFromStop = createStop(1L, "Invalid Stop", 1, differentRoute);

        when(routeRepository.findById(1L)).thenReturn(Optional.of(route));
        when(stopRepository.findById(1L)).thenReturn(Optional.of(invalidFromStop));
        when(stopRepository.findById(2L)).thenReturn(Optional.of(toStop));

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> fareRuleService.createFareRule(createRequest));
        verify(fareRuleRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception when to stop does not belong to route")
    void shouldThrowExceptionWhenToStopDoesNotBelongToRoute() {
        // Given
        Route differentRoute = Route.builder().id(99L).code("ROUTE99").build();
        Stop invalidToStop = createStop(2L, "Invalid Stop", 5, differentRoute);

        when(routeRepository.findById(1L)).thenReturn(Optional.of(route));
        when(stopRepository.findById(1L)).thenReturn(Optional.of(fromStop));
        when(stopRepository.findById(2L)).thenReturn(Optional.of(invalidToStop));

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> fareRuleService.createFareRule(createRequest));
        verify(fareRuleRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception when stop order is invalid (from >= to)")
    void shouldThrowExceptionWhenStopOrderIsInvalid() {
        // Given
        Stop invalidToStop = createStop(2L, "Invalid Stop", 1, route); // Same order as fromStop

        when(routeRepository.findById(1L)).thenReturn(Optional.of(route));
        when(stopRepository.findById(1L)).thenReturn(Optional.of(fromStop));
        when(stopRepository.findById(2L)).thenReturn(Optional.of(invalidToStop));

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> fareRuleService.createFareRule(createRequest));
        verify(fareRuleRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should create fare rule without discounts")
    void shouldCreateFareRuleWithoutDiscounts() {
        // Given
        FareRuleCreateRequest requestWithoutDiscounts = new FareRuleCreateRequest(
                new BigDecimal("50000"),
                DynamicPricing.OFF,
                null, // No discounts
                1L,
                1L,
                2L
        );

        FareRule fareRuleNoDiscounts = FareRule.builder()
                .basePrice(new BigDecimal("50000"))
                .dynamicPricing(DynamicPricing.OFF)
                .discounts(null)
                .build();

        when(routeRepository.findById(1L)).thenReturn(Optional.of(route));
        when(stopRepository.findById(1L)).thenReturn(Optional.of(fromStop));
        when(stopRepository.findById(2L)).thenReturn(Optional.of(toStop));
        when(mapper.toEntity(requestWithoutDiscounts)).thenReturn(fareRuleNoDiscounts);
        when(fareRuleRepository.save(any(FareRule.class))).thenReturn(fareRuleNoDiscounts);
        when(mapper.toResponse(fareRuleNoDiscounts)).thenReturn(response);

        // When
        FareRuleResponse result = fareRuleService.createFareRule(requestWithoutDiscounts);

        // Then
        assertNotNull(result);
        verify(fareRuleRepository).save(any(FareRule.class));
    }

    // ============================================================
    //                  UPDATE FARE RULE TESTS
    // ============================================================

    @Test
    @DisplayName("Should update fare rule successfully")
    void shouldUpdateFareRuleSuccessfully() {
        // Given
        when(fareRuleRepository.findById(1L)).thenReturn(Optional.of(fareRule));
        doNothing().when(mapper).updateEntity(updateRequest, fareRule);
        when(fareRuleRepository.save(fareRule)).thenReturn(fareRule);

        // When
        fareRuleService.updateFareRule(1L, updateRequest);

        // Then
        verify(fareRuleRepository).findById(1L);
        verify(mapper).updateEntity(updateRequest, fareRule);
        verify(fareRuleRepository).save(fareRule);
    }

    @Test
    @DisplayName("Should throw exception when fare rule not found during update")
    void shouldThrowExceptionWhenFareRuleNotFoundDuringUpdate() {
        // Given
        when(fareRuleRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(NotFoundException.class, () -> fareRuleService.updateFareRule(1L, updateRequest));
        verify(fareRuleRepository).findById(1L);
        verify(fareRuleRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should update only base price")
    void shouldUpdateOnlyBasePrice() {
        // Given
        FareRuleUpdateRequest partialUpdate = new FareRuleUpdateRequest(
                new BigDecimal("60000"),
                null,
                null,
                null,
                null,
                null
        );

        when(fareRuleRepository.findById(1L)).thenReturn(Optional.of(fareRule));
        doNothing().when(mapper).updateEntity(partialUpdate, fareRule);
        when(fareRuleRepository.save(fareRule)).thenReturn(fareRule);

        // When
        fareRuleService.updateFareRule(1L, partialUpdate);

        // Then
        verify(mapper).updateEntity(partialUpdate, fareRule);
        verify(fareRuleRepository).save(fareRule);
    }

    // ============================================================
    //                  DELETE FARE RULE TESTS
    // ============================================================

    @Test
    @DisplayName("Should delete fare rule successfully")
    void shouldDeleteFareRuleSuccessfully() {
        // Given
        when(fareRuleRepository.findById(1L)).thenReturn(Optional.of(fareRule));
        doNothing().when(fareRuleRepository).delete(fareRule);

        // When
        fareRuleService.deleteFareRule(1L);

        // Then
        verify(fareRuleRepository).findById(1L);
        verify(fareRuleRepository).delete(fareRule);
    }

    @Test
    @DisplayName("Should throw exception when fare rule not found during deletion")
    void shouldThrowExceptionWhenFareRuleNotFoundDuringDeletion() {
        // Given
        when(fareRuleRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(NotFoundException.class, () -> fareRuleService.deleteFareRule(1L));
        verify(fareRuleRepository).findById(1L);
        verify(fareRuleRepository, never()).delete(any());
    }

    // ============================================================
    //                  GET FARE RULE TESTS
    // ============================================================

    @Test
    @DisplayName("Should get fare rule by id successfully")
    void shouldGetFareRuleByIdSuccessfully() {
        // Given
        when(fareRuleRepository.findById(1L)).thenReturn(Optional.of(fareRule));
        when(mapper.toResponse(fareRule)).thenReturn(response);

        // When
        FareRuleResponse result = fareRuleService.getFareRule(1L);

        // Then
        assertNotNull(result);
        assertEquals(response.id(), result.id());
        verify(fareRuleRepository).findById(1L);
        verify(mapper).toResponse(fareRule);
    }

    @Test
    @DisplayName("Should throw exception when fare rule not found by id")
    void shouldThrowExceptionWhenFareRuleNotFoundById() {
        // Given
        when(fareRuleRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(NotFoundException.class, () -> fareRuleService.getFareRule(1L));
        verify(fareRuleRepository).findById(1L);
        verify(mapper, never()).toResponse(any());
    }

    // ============================================================
    //                  GET ALL FARE RULES TESTS
    // ============================================================

    @Test
    @DisplayName("Should get all fare rules successfully")
    void shouldGetAllFareRulesSuccessfully() {
        // Given
        List<FareRule> fareRules = Arrays.asList(fareRule, createAnotherFareRule());
        when(fareRuleRepository.findAll()).thenReturn(fareRules);
        when(mapper.toResponse(any(FareRule.class))).thenReturn(response);

        // When
        List<FareRuleResponse> result = fareRuleService.getAllFareRules();

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(fareRuleRepository).findAll();
        verify(mapper, times(2)).toResponse(any(FareRule.class));
    }

    @Test
    @DisplayName("Should return empty list when no fare rules exist")
    void shouldReturnEmptyListWhenNoFareRulesExist() {
        // Given
        when(fareRuleRepository.findAll()).thenReturn(Collections.emptyList());

        // When
        List<FareRuleResponse> result = fareRuleService.getAllFareRules();

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(fareRuleRepository).findAll();
    }

    // ============================================================
    //                  GET FARE RULES BY ROUTE TESTS
    // ============================================================

    @Test
    @DisplayName("Should get fare rules by route id successfully")
    void shouldGetFareRulesByRouteIdSuccessfully() {
        // Given
        when(routeRepository.existsById(1L)).thenReturn(true);
        when(fareRuleRepository.findByRouteId(1L)).thenReturn(Arrays.asList(fareRule));
        when(mapper.toResponse(any(FareRule.class))).thenReturn(response);

        // When
        List<FareRuleResponse> result = fareRuleService.getFareRulesByRouteId(1L);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(routeRepository).existsById(1L);
        verify(fareRuleRepository).findByRouteId(1L);
    }

    @Test
    @DisplayName("Should throw exception when route not found for fare rules")
    void shouldThrowExceptionWhenRouteNotFoundForFareRules() {
        // Given
        when(routeRepository.existsById(1L)).thenReturn(false);

        // When & Then
        assertThrows(NotFoundException.class, () -> fareRuleService.getFareRulesByRouteId(1L));
        verify(routeRepository).existsById(1L);
        verify(fareRuleRepository, never()).findByRouteId(anyLong());
    }

    @Test
    @DisplayName("Should return empty list when no fare rules for route")
    void shouldReturnEmptyListWhenNoFareRulesForRoute() {
        // Given
        when(routeRepository.existsById(1L)).thenReturn(true);
        when(fareRuleRepository.findByRouteId(1L)).thenReturn(Collections.emptyList());

        // When
        List<FareRuleResponse> result = fareRuleService.getFareRulesByRouteId(1L);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(fareRuleRepository).findByRouteId(1L);
    }

    // ============================================================
    //                  GET FINAL TICKET PRICE TESTS
    // ============================================================

    @Test
    @DisplayName("Should calculate final ticket price successfully without dynamic pricing")
    void shouldCalculateFinalTicketPriceSuccessfullyWithoutDynamicPricing() {
        // Given
        fareRule.setDynamicPricing(DynamicPricing.OFF);
        Passenger passenger = createPassenger(LocalDate.of(1990, 5, 15)); // Adult
        Seat seat = createSeat(1L, SeatType.STANDARD);
        Bus bus = createBus();

        when(fareRuleRepository.findByRouteIdAndFromStopIdAndToStopId(1L, 1L, 2L))
                .thenReturn(Optional.of(fareRule));
        when(passengerRepository.findById(1L)).thenReturn(Optional.of(passenger));
        when(seatRepository.findByBusIdAndNumber(1L, 10)).thenReturn(Optional.of(seat));

        // When
        BigDecimal result = fareRuleService.getFinalTicketPrice(1L, 1L, 2L, 1L, 1L, "10", 1L);

        // Then
        assertNotNull(result);
        assertEquals(new BigDecimal("50000"), result); // Base price without discounts or surcharges
        verify(fareRuleRepository).findByRouteIdAndFromStopIdAndToStopId(1L, 1L, 2L);
        verify(passengerRepository).findById(1L);
        verify(seatRepository).findByBusIdAndNumber(1L, 10);
    }

    @Test
    @DisplayName("Should apply child discount to ticket price")
    void shouldApplyChildDiscountToTicketPrice() {
        // Given
        fareRule.setDynamicPricing(DynamicPricing.OFF);
        Passenger child = createPassenger(LocalDate.now().minusYears(8)); // 8 years old
        Seat seat = createSeat(1L, SeatType.STANDARD);

        when(fareRuleRepository.findByRouteIdAndFromStopIdAndToStopId(1L, 1L, 2L))
                .thenReturn(Optional.of(fareRule));
        when(passengerRepository.findById(1L)).thenReturn(Optional.of(child));
        when(seatRepository.findByBusIdAndNumber(1L, 10)).thenReturn(Optional.of(seat));

        // When
        BigDecimal result = fareRuleService.getFinalTicketPrice(1L, 1L, 2L, 1L, 1L, "10", 1L);

        // Then
        assertNotNull(result);
        // Base price 50000 - 25% child discount = 37500
        assertEquals(new BigDecimal("37500.00"), result);
    }

    @Test
    @DisplayName("Should apply senior discount to ticket price")
    void shouldApplySeniorDiscountToTicketPrice() {
        // Given
        fareRule.setDynamicPricing(DynamicPricing.OFF);
        Passenger senior = createPassenger(LocalDate.now().minusYears(65)); // 65 years old
        Seat seat = createSeat(1L, SeatType.STANDARD);

        when(fareRuleRepository.findByRouteIdAndFromStopIdAndToStopId(1L, 1L, 2L))
                .thenReturn(Optional.of(fareRule));
        when(passengerRepository.findById(1L)).thenReturn(Optional.of(senior));
        when(seatRepository.findByBusIdAndNumber(1L, 10)).thenReturn(Optional.of(seat));

        // When
        BigDecimal result = fareRuleService.getFinalTicketPrice(1L, 1L, 2L, 1L, 1L, "10", 1L);

        // Then
        assertNotNull(result);
        // Base price 50000 - 20% senior discount = 40000
        assertEquals(new BigDecimal("40000.0"), result);
    }

    @Test
    @DisplayName("Should apply student discount to ticket price")
    void shouldApplyStudentDiscountToTicketPrice() {
        // Given
        fareRule.setDynamicPricing(DynamicPricing.OFF);
        Passenger student = createPassenger(LocalDate.now().minusYears(20)); // 20 years old
        Seat seat = createSeat(1L, SeatType.STANDARD);

        when(fareRuleRepository.findByRouteIdAndFromStopIdAndToStopId(1L, 1L, 2L))
                .thenReturn(Optional.of(fareRule));
        when(passengerRepository.findById(1L)).thenReturn(Optional.of(student));
        when(seatRepository.findByBusIdAndNumber(1L, 10)).thenReturn(Optional.of(seat));

        // When
        BigDecimal result = fareRuleService.getFinalTicketPrice(1L, 1L, 2L, 1L, 1L, "10", 1L);

        // Then
        assertNotNull(result);
        // Base price 50000 - 15% student discount = 42500
        assertEquals(new BigDecimal("42500.00"), result);
    }

    @Test
    @DisplayName("Should apply preferential seat surcharge")
    void shouldApplyPreferentialSeatSurcharge() {
        // Given
        fareRule.setDynamicPricing(DynamicPricing.OFF);
        Passenger passenger = createPassenger(LocalDate.of(1990, 5, 15)); // Adult
        Seat preferentialSeat = createSeat(1L, SeatType.PREFERENTIAL);

        when(fareRuleRepository.findByRouteIdAndFromStopIdAndToStopId(1L, 1L, 2L))
                .thenReturn(Optional.of(fareRule));
        when(passengerRepository.findById(1L)).thenReturn(Optional.of(passenger));
        when(seatRepository.findByBusIdAndNumber(1L, 10)).thenReturn(Optional.of(preferentialSeat));

        // When
        BigDecimal result = fareRuleService.getFinalTicketPrice(1L, 1L, 2L, 1L, 1L, "10", 1L);

        // Then
        assertNotNull(result);
        // Base price 50000 + 15% surcharge = 57500
        assertEquals(new BigDecimal("57500.00"), result);
    }

    @Test
    @DisplayName("Should apply dynamic pricing when occupancy is 85% or more")
    void shouldApplyDynamicPricingWhenOccupancyIs85OrMore() {
        // Given
        fareRule.setDynamicPricing(DynamicPricing.ON);
        Passenger passenger = createPassenger(LocalDate.of(1990, 5, 15)); // Adult
        Seat seat = createSeat(1L, SeatType.STANDARD);
        Bus bus = createBus();

        when(fareRuleRepository.findByRouteIdAndFromStopIdAndToStopId(1L, 1L, 2L))
                .thenReturn(Optional.of(fareRule));
        when(passengerRepository.findById(1L)).thenReturn(Optional.of(passenger));
        when(seatRepository.findByBusIdAndNumber(1L, 10)).thenReturn(Optional.of(seat));
        when(busRepository.findById(1L)).thenReturn(bus);
        when(ticketRepository.countSoldByTrip(1L)).thenReturn(43L); // 43/50 = 86% occupancy

        // When
        BigDecimal result = fareRuleService.getFinalTicketPrice(1L, 1L, 2L, 1L, 1L, "10", 1L);

        // Then
        assertNotNull(result);
        // Base price 50000 + 20% dynamic surcharge = 60000
        assertEquals(new BigDecimal("60000.00"), result);
        verify(ticketRepository).countSoldByTrip(1L);
    }

    @Test
    @DisplayName("Should apply dynamic pricing when occupancy is between 70% and 85%")
    void shouldApplyDynamicPricingWhenOccupancyIsBetween70And85() {
        // Given
        fareRule.setDynamicPricing(DynamicPricing.ON);
        Passenger passenger = createPassenger(LocalDate.of(1990, 5, 15)); // Adult
        Seat seat = createSeat(1L, SeatType.STANDARD);
        Bus bus = createBus();

        when(fareRuleRepository.findByRouteIdAndFromStopIdAndToStopId(1L, 1L, 2L))
                .thenReturn(Optional.of(fareRule));
        when(passengerRepository.findById(1L)).thenReturn(Optional.of(passenger));
        when(seatRepository.findByBusIdAndNumber(1L, 10)).thenReturn(Optional.of(seat));
        when(busRepository.findById(1L)).thenReturn(bus);
        when(ticketRepository.countSoldByTrip(1L)).thenReturn(36L); // 36/50 = 72% occupancy

        // When
        BigDecimal result = fareRuleService.getFinalTicketPrice(1L, 1L, 2L, 1L, 1L, "10", 1L);

        // Then
        assertNotNull(result);
        // Base price 50000 + 10% dynamic surcharge = 55000
        assertEquals(new BigDecimal("55000.00"), result);
    }

    @Test
    @DisplayName("Should not apply dynamic pricing when occupancy is below 70%")
    void shouldNotApplyDynamicPricingWhenOccupancyIsBelow70() {
        // Given
        fareRule.setDynamicPricing(DynamicPricing.ON);
        Passenger passenger = createPassenger(LocalDate.of(1990, 5, 15)); // Adult
        Seat seat = createSeat(1L, SeatType.STANDARD);
        Bus bus = createBus();

        when(fareRuleRepository.findByRouteIdAndFromStopIdAndToStopId(1L, 1L, 2L))
                .thenReturn(Optional.of(fareRule));
        when(passengerRepository.findById(1L)).thenReturn(Optional.of(passenger));
        when(seatRepository.findByBusIdAndNumber(1L, 10)).thenReturn(Optional.of(seat));
        when(busRepository.findById(1L)).thenReturn(bus);
        when(ticketRepository.countSoldByTrip(1L)).thenReturn(30L); // 30/50 = 60% occupancy

        // When
        BigDecimal result = fareRuleService.getFinalTicketPrice(1L, 1L, 2L, 1L, 1L, "10", 1L);

        // Then
        assertNotNull(result);
        // Base price 50000 with no dynamic surcharge = 50000
        assertEquals(new BigDecimal("50000.00"), result);
    }

    @Test
    @DisplayName("Should calculate complex price with all factors combined")
    void shouldCalculateComplexPriceWithAllFactorsCombined() {
        // Given
        fareRule.setDynamicPricing(DynamicPricing.ON);
        Passenger student = createPassenger(LocalDate.now().minusYears(20)); // Student
        Seat preferentialSeat = createSeat(1L, SeatType.PREFERENTIAL);
        Bus bus = createBus();

        when(fareRuleRepository.findByRouteIdAndFromStopIdAndToStopId(1L, 1L, 2L))
                .thenReturn(Optional.of(fareRule));
        when(passengerRepository.findById(1L)).thenReturn(Optional.of(student));
        when(seatRepository.findByBusIdAndNumber(1L, 10)).thenReturn(Optional.of(preferentialSeat));
        when(busRepository.findById(1L)).thenReturn(bus);
        when(ticketRepository.countSoldByTrip(1L)).thenReturn(43L); // 86% occupancy

        // When
        BigDecimal result = fareRuleService.getFinalTicketPrice(1L, 1L, 2L, 1L, 1L, "10", 1L);

        // Then
        assertNotNull(result);
        // Base: 50000
        // - 15% student discount = 42500
        // + 15% preferential seat = 50000 * 0.15 = 7500
        // + 20% dynamic pricing = 50000 * 0.20 = 10000
        // Total = 42500 + 7500 + 10000 = 60000
        assertEquals(new BigDecimal("60000.00"), result);
    }

    @Test
    @DisplayName("Should throw exception when fare rule not found for price calculation")
    void shouldThrowExceptionWhenFareRuleNotFoundForPriceCalculation() {
        // Given
        when(fareRuleRepository.findByRouteIdAndFromStopIdAndToStopId(1L, 1L, 2L))
                .thenReturn(Optional.empty());

        // When & Then
        assertThrows(NotFoundException.class, () ->
                fareRuleService.getFinalTicketPrice(1L, 1L, 2L, 1L, 1L, "10", 1L));
        verify(fareRuleRepository).findByRouteIdAndFromStopIdAndToStopId(1L, 1L, 2L);
    }

    @Test
    @DisplayName("Should throw exception when passenger not found for price calculation")
    void shouldThrowExceptionWhenPassengerNotFoundForPriceCalculation() {
        // Given
        when(fareRuleRepository.findByRouteIdAndFromStopIdAndToStopId(1L, 1L, 2L))
                .thenReturn(Optional.of(fareRule));
        when(passengerRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(NotFoundException.class, () ->
                fareRuleService.getFinalTicketPrice(1L, 1L, 2L, 1L, 1L, "10", 1L));
        verify(passengerRepository).findById(1L);
    }

    @Test
    @DisplayName("Should throw exception when seat not found for price calculation")
    void shouldThrowExceptionWhenSeatNotFoundForPriceCalculation() {
        // Given
        Passenger passenger = createPassenger(LocalDate.of(1990, 5, 15));

        when(fareRuleRepository.findByRouteIdAndFromStopIdAndToStopId(1L, 1L, 2L))
                .thenReturn(Optional.of(fareRule));
        when(passengerRepository.findById(1L)).thenReturn(Optional.of(passenger));
        when(seatRepository.findByBusIdAndNumber(1L, 10)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(NotFoundException.class, () ->
                fareRuleService.getFinalTicketPrice(1L, 1L, 2L, 1L, 1L, "10", 1L));
        verify(seatRepository).findByBusIdAndNumber(1L, 10);
    }

    @Test
    @DisplayName("Should throw exception when bus not found for dynamic pricing")
    void shouldThrowExceptionWhenBusNotFoundForDynamicPricing() {
        // Given
        fareRule.setDynamicPricing(DynamicPricing.ON);
        Passenger passenger = createPassenger(LocalDate.of(1990, 5, 15));
        Seat seat = createSeat(1L, SeatType.STANDARD);

        when(fareRuleRepository.findByRouteIdAndFromStopIdAndToStopId(1L, 1L, 2L))
                .thenReturn(Optional.of(fareRule));
        when(passengerRepository.findById(1L)).thenReturn(Optional.of(passenger));
        when(seatRepository.findByBusIdAndNumber(1L, 10)).thenReturn(Optional.of(seat));
        when(busRepository.findById(1L)).thenReturn(null);

        // When & Then
        assertThrows(NotFoundException.class, () ->
                fareRuleService.getFinalTicketPrice(1L, 1L, 2L, 1L, 1L, "10", 1L));
        verify(busRepository).findById(1L);
    }

    @Test
    @DisplayName("Should calculate price with no discounts when discounts map is null")
    void shouldCalculatePriceWithNoDiscountsWhenDiscountsMapIsNull() {
        // Given
        fareRule.setDiscounts(null);
        fareRule.setDynamicPricing(DynamicPricing.OFF);
        Passenger child = createPassenger(LocalDate.now().minusYears(8));
        Seat seat = createSeat(1L, SeatType.STANDARD);

        when(fareRuleRepository.findByRouteIdAndFromStopIdAndToStopId(1L, 1L, 2L))
                .thenReturn(Optional.of(fareRule));
        when(passengerRepository.findById(1L)).thenReturn(Optional.of(child));
        when(seatRepository.findByBusIdAndNumber(1L, 10)).thenReturn(Optional.of(seat));

        // When
        BigDecimal result = fareRuleService.getFinalTicketPrice(1L, 1L, 2L, 1L, 1L, "10", 1L);

        // Then
        assertNotNull(result);
        assertEquals(new BigDecimal("50000"), result); // Full price, no discount applied
    }

    @Test
    @DisplayName("Should calculate price with no discounts when discounts map is empty")
    void shouldCalculatePriceWithNoDiscountsWhenDiscountsMapIsEmpty() {
        // Given
        fareRule.setDiscounts(new HashMap<>());
        fareRule.setDynamicPricing(DynamicPricing.OFF);
        Passenger senior = createPassenger(LocalDate.now().minusYears(65));
        Seat seat = createSeat(1L, SeatType.STANDARD);

        when(fareRuleRepository.findByRouteIdAndFromStopIdAndToStopId(1L, 1L, 2L))
                .thenReturn(Optional.of(fareRule));
        when(passengerRepository.findById(1L)).thenReturn(Optional.of(senior));
        when(seatRepository.findByBusIdAndNumber(1L, 10)).thenReturn(Optional.of(seat));

        // When
        BigDecimal result = fareRuleService.getFinalTicketPrice(1L, 1L, 2L, 1L, 1L, "10", 1L);

        // Then
        assertNotNull(result);
        assertEquals(new BigDecimal("50000"), result); // Full price, no discount applied
    }

    // ============================================================
    //                  HELPER METHODS
    // ============================================================

    private Route createRoute() {
        return Route.builder()
                .id(1L)
                .code("ROUTE001")
                .name("Main Route")
                .origin("City A")
                .destination("City B")
                .distanceKm(100.0f)
                .durationMin(120.0f)
                .build();
    }

    private Stop createStop(Long id, String name, Integer order, Route route) {
        return Stop.builder()
                .id(id)
                .name(name)
                .order(order)
                .latitude(4.60971)
                .longitude(-74.08175)
                .route(route)
                .build();
    }

    private Map<String, Double> createDiscounts() {
        Map<String, Double> discounts = new HashMap<>();
        discounts.put("child", 0.25);    // 25% discount for children
        discounts.put("student", 0.15);  // 15% discount for students
        discounts.put("senior", 0.20);   // 20% discount for seniors
        return discounts;
    }

    private FareRule createFareRule(Route route, Stop fromStop, Stop toStop, Map<String, Double> discounts) {
        return FareRule.builder()
                .id(1L)
                .basePrice(new BigDecimal("50000"))
                .dynamicPricing(DynamicPricing.OFF)
                .discounts(discounts)
                .route(route)
                .fromStop(fromStop)
                .toStop(toStop)
                .build();
    }

    private FareRule createAnotherFareRule() {
        return FareRule.builder()
                .id(2L)
                .basePrice(new BigDecimal("35000"))
                .dynamicPricing(DynamicPricing.ON)
                .discounts(discounts)
                .route(route)
                .fromStop(fromStop)
                .toStop(toStop)
                .build();
    }

    private FareRuleCreateRequest createFareRuleCreateRequest() {
        return new FareRuleCreateRequest(
                new BigDecimal("50000"),
                DynamicPricing.OFF,
                discounts,
                1L,
                1L,
                2L
        );
    }

    private FareRuleUpdateRequest createFareRuleUpdateRequest() {
        return new FareRuleUpdateRequest(
                new BigDecimal("55000"),
                DynamicPricing.ON,
                discounts,
                null,
                null,
                null
        );
    }

    private FareRuleResponse createFareRuleResponse() {
        return new FareRuleResponse(
                1L,
                new BigDecimal("50000"),
                DynamicPricing.OFF,
                discounts,
                null,
                null,
                null
        );
    }

    private Passenger createPassenger(LocalDate birthDate) {
        return Passenger.builder()
                .id(1L)
                .fullName("John Doe")
                .documentType("CC")
                .documentNumber("123456789")
                .birthDate(birthDate)
                .phoneNumber("3001234567")
                .createdAt(OffsetDateTime.now())
                .build();
    }

    private Seat createSeat(Long id, SeatType type) {
        return Seat.builder()
                .id(id)
                .number(10)
                .type(type)
                .price(new BigDecimal("50000"))
                .status(SeatStatus.AVAILABLE)
                .build();
    }

    private Bus createBus() {
        return Bus.builder()
                .id(1L)
                .plate("ABC123")
                .capacity(50)
                .status(BusStatus.AVAILABLE)
                .build();
    }
}
