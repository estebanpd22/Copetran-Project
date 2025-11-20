package co.unimagdalena.services;

import co.unimagdalena.api.dto.FareRuleDto.*;
<<<<<<< Updated upstream
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
=======
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
>>>>>>> Stashed changes
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
<<<<<<< Updated upstream
import java.time.OffsetDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FareRuleServiceImplTest {
=======
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class FareRuleServiceImplTest {
>>>>>>> Stashed changes

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

<<<<<<< Updated upstream
    @Mock
    private FareRuleMapper mapper;
=======
    @Spy
    private FareRuleMapper fareRuleMapper = Mappers.getMapper(FareRuleMapper.class);
>>>>>>> Stashed changes

    @InjectMocks
    private FareRuleServiceImpl fareRuleService;

<<<<<<< Updated upstream
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
=======
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
>>>>>>> Stashed changes
        return Stop.builder()
                .id(id)
                .name(name)
                .order(order)
<<<<<<< Updated upstream
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
=======
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
>>>>>>> Stashed changes
