package co.unimagdalena.services;

import co.unimagdalena.api.dto.RouteDto.*;
import co.unimagdalena.api.dto.StopDto.*;
import co.unimagdalena.domine.entities.*;
import co.unimagdalena.domine.repositories.RouteRepository;
import co.unimagdalena.domine.repositories.StopRepository;
import co.unimagdalena.exception.NotFoundException;
import co.unimagdalena.services.impl.RouteServiceImpl;
import co.unimagdalena.services.RouteService.*;
import co.unimagdalena.services.mapper.RouteMapper;
import co.unimagdalena.services.mapper.StopMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RouteServiceImplTest {

    @Mock
    private RouteRepository routeRepository;

    @Mock
    private StopRepository stopRepository;

    @Spy
    private RouteMapper routeMapper = Mappers.getMapper(RouteMapper.class);

    @Spy
    private StopMapper stopMapper = Mappers.getMapper(StopMapper.class);

    @InjectMocks
    private RouteServiceImpl routeService;

    // ======================================================================
    // HELPER METHODS - Creación de entidades de prueba
    // ======================================================================

    private Route createRoute(Long id, String code, String name, String origin,
                              String destination, Float distanceKm, Float durationMin) {
        return Route.builder()
                .id(id)
                .code(code)
                .name(name)
                .origin(origin)
                .destination(destination)
                .distanceKm(distanceKm)
                .durationMin(durationMin)
                .stops(new ArrayList<>())
                .trips(new ArrayList<>())
                .fareRules(new ArrayList<>())
                .build();
    }

    private Stop createStop(Long id, String name, Integer order, Double latitude,
                            Double longitude, Route route) {
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
    // TESTS - createRoute
    // ======================================================================

    @Test
    @DisplayName("Debe crear una Ruta de manera exitosa con código único")
    void shouldCreateRouteSuccessfully() {
        // Arrange
        RouteCreateRequest request = new RouteCreateRequest(
                "R001", "Ruta Norte", "Bogotá", "Medellín", 300.5f, 450.0f
        );

        Route routeToSave = routeMapper.toEntity(request);
        Route savedRoute = createRoute(1L, "R001", "Ruta Norte", "Bogotá", "Medellín", 300.5f, 450.0f);

        when(routeRepository.findByCode("R001")).thenReturn(Optional.empty());
        when(routeRepository.save(any(Route.class))).thenReturn(savedRoute);

        // Act
        RouteResponse response = routeService.createRoute(request);

        // Assert
        assertNotNull(response);
        assertEquals("R001", response.code());
        assertEquals("Ruta Norte", response.name());
        assertEquals(1L, response.id());
        verify(routeRepository, times(1)).findByCode("R001");
        verify(routeRepository, times(1)).save(any(Route.class));
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando el código de ruta ya existe")
    void shouldThrowExceptionWhenRouteCodeAlreadyExists() {
        // Arrange
        RouteCreateRequest request = new RouteCreateRequest(
                "R001", "Ruta Nueva", "Bogotá", "Medellín", 300.5f, 450.0f
        );

        Route existingRoute = createRoute(1L, "R001", "Ruta Existente", "Bogotá", "Cali", 200.0f, 300.0f);

        when(routeRepository.findByCode("R001")).thenReturn(Optional.of(existingRoute));

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> routeService.createRoute(request));

        assertEquals("El código de ruta R001 ya existe.", exception.getMessage());
        verify(routeRepository, never()).save(any(Route.class));
    }

    // ======================================================================
    // TESTS - updateRoute
    // ======================================================================

    @Test
    @DisplayName("Debe actualizar una Ruta exitosamente con cambios parciales")
    void shouldUpdateRouteSuccessfully() {
        // Arrange
        Long routeId = 1L;
        Route existingRoute = createRoute(1L, "R001", "Ruta Norte", "Bogotá", "Medellín", 300.5f, 450.0f);

        RouteUpdateRequest updateRequest = new RouteUpdateRequest(
                null, "Ruta Norte Actualizada", null, null, null, 480.0f
        );

        when(routeRepository.findRouteById(routeId)).thenReturn(Optional.of(existingRoute));
        when(routeRepository.save(any(Route.class))).thenReturn(existingRoute);

        // Act
        routeService.updateRoute(routeId, updateRequest);

        // Assert
        verify(routeRepository, times(1)).findRouteById(routeId);
        verify(routeRepository, times(1)).save(existingRoute);
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando se intenta cambiar el código a uno que ya existe")
    void shouldThrowExceptionWhenUpdatingToExistingCode() {
        // Arrange
        Long routeId = 1L;
        Route existingRoute1 = createRoute(1L, "R001", "Ruta Norte", "Bogotá", "Medellín", 300.5f, 450.0f);
        Route existingRoute2 = createRoute(2L, "R002", "Ruta Centro", "Bogotá", "Cali", 200.0f, 300.0f);

        RouteUpdateRequest updateRequest = new RouteUpdateRequest(
                "R002", null, null, null, null, null
        );

        when(routeRepository.findRouteById(routeId)).thenReturn(Optional.of(existingRoute1));
        when(routeRepository.findByCode("R002")).thenReturn(Optional.of(existingRoute2));

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> routeService.updateRoute(routeId, updateRequest));

        assertEquals("El código de ruta R002 ya está en uso por otra ruta.", exception.getMessage());
        verify(routeRepository, never()).save(any(Route.class));
    }

    @Test
    @DisplayName("Debe permitir actualizar el código si el código nuevo no existe")
    void shouldAllowUpdatingCodeWhenNewCodeDoesNotExist() {
        // Arrange
        Long routeId = 1L;
        Route existingRoute = createRoute(1L, "R001", "Ruta Norte", "Bogotá", "Medellín", 300.5f, 450.0f);

        RouteUpdateRequest updateRequest = new RouteUpdateRequest(
                "R003", null, null, null, null, null
        );

        when(routeRepository.findRouteById(routeId)).thenReturn(Optional.of(existingRoute));
        when(routeRepository.findByCode("R003")).thenReturn(Optional.empty());
        when(routeRepository.save(any(Route.class))).thenReturn(existingRoute);

        // Act
        routeService.updateRoute(routeId, updateRequest);

        // Assert
        verify(routeRepository, times(1)).save(existingRoute);
    }

    @Test
    @DisplayName("Debe lanzar excepción NotFoundException cuando la Ruta no existe")
    void shouldThrowNotFoundExceptionWhenRouteNotExists() {
        // Arrange
        Long routeId = 999L;
        RouteUpdateRequest updateRequest = new RouteUpdateRequest(
                null, "Nueva Ruta", null, null, null, null
        );

        when(routeRepository.findRouteById(routeId)).thenReturn(Optional.empty());

        // Act & Assert
        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> routeService.updateRoute(routeId, updateRequest));

        assertEquals("Ruta no encontrada con ID: 999", exception.getMessage());
        verify(routeRepository, never()).save(any(Route.class));
    }

    // ======================================================================
    // TESTS - deleteRoute
    // ======================================================================

    @Test
    @DisplayName("Debe eliminar una Ruta exitosamente cuando no tiene viajes asociados")
    void shouldDeleteRouteSuccessfully() {
        // Arrange
        Long routeId = 1L;
        Route route = createRoute(1L, "R001", "Ruta Norte", "Bogotá", "Medellín", 300.5f, 450.0f);

        when(routeRepository.findRouteById(routeId)).thenReturn(Optional.of(route));
        doNothing().when(routeRepository).delete(route);

        // Act
        routeService.deleteRoute(routeId);

        // Assert
        verify(routeRepository, times(1)).delete(route);
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando se intenta eliminar una Ruta con viajes asociados")
    void shouldThrowExceptionWhenDeletingRouteWithTrips() {
        // Arrange
        Long routeId = 1L;
        Route route = createRoute(1L, "R001", "Ruta Norte", "Bogotá", "Medellín", 300.5f, 450.0f);
        Bus bus = createBus(1L, "ABC123", 40, BusStatus.AVAILABLE);

        Trip trip1 = createTrip(1L, LocalDate.now().plusDays(1),
                OffsetDateTime.now().plusDays(1), OffsetDateTime.now().plusDays(1).plusHours(8),
                TripStatus.SCHEDULED, route, bus);

        route.getTrips().add(trip1);

        when(routeRepository.findRouteById(routeId)).thenReturn(Optional.of(route));

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> routeService.deleteRoute(routeId));

        assertEquals("No se puede eliminar la ruta ID 1 porque tiene viajes asociados.", exception.getMessage());
        verify(routeRepository, never()).delete(any(Route.class));
    }

    @Test
    @DisplayName("Debe lanzar NotFoundException cuando se intenta eliminar una Ruta inexistente")
    void shouldThrowNotFoundExceptionWhenDeletingNonExistentRoute() {
        // Arrange
        Long routeId = 999L;

        when(routeRepository.findRouteById(routeId)).thenReturn(Optional.empty());

        // Act & Assert
        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> routeService.deleteRoute(routeId));

        assertEquals("Ruta no encontrada con ID: 999", exception.getMessage());
        verify(routeRepository, never()).delete(any(Route.class));
    }

    // ======================================================================
    // TESTS - addStopToRoute
    // ======================================================================

    @Test
    @DisplayName("Debe agregar una parada a una Ruta exitosamente")
    void shouldAddStopToRouteSuccessfully() {
        // Arrange
        Long routeId = 1L;
        Long stopId = 1L;
        int stopOrder = 1;

        Route route = createRoute(1L, "R001", "Ruta Norte", "Bogotá", "Medellín", 300.5f, 450.0f);
        Stop stop = createStop(stopId, "Bogotá", null, 4.7110, -74.0721, null);

        when(routeRepository.findRouteById(routeId)).thenReturn(Optional.of(route));
        when(stopRepository.findById(stopId)).thenReturn(Optional.of(stop));
        when(routeRepository.save(any(Route.class))).thenReturn(route);

        // Act
        RouteResponse response = routeService.addStopToRoute(routeId, stopId, stopOrder);

        // Assert
        assertNotNull(response);
        assertEquals(1L, response.id());
        verify(routeRepository, times(1)).save(any(Route.class));
        verify(stopRepository, times(1)).findById(stopId);
    }

    @Test
    @DisplayName("Debe incrementar el orden de las paradas cuando se agrega una parada en el medio")
    void shouldIncrementStopsOrderWhenAddingInMiddle() {
        // Arrange
        Long routeId = 1L;
        Long stopId = 2L;
        int insertOrder = 1;

        Route route = createRoute(1L, "R001", "Ruta Norte", "Bogotá", "Medellín", 300.5f, 450.0f);

        Stop stop1 = createStop(1L, "Bogotá", 1, 4.7110, -74.0721, route);
        Stop stop3 = createStop(3L, "Armenia", 2, 4.5333, -75.7333, route);
        route.getStops().addAll(List.of(stop1, stop3));

        Stop stop2 = createStop(stopId, "Medellín", null, 6.2442, -75.5812, null);

        when(routeRepository.findRouteById(routeId)).thenReturn(Optional.of(route));
        when(stopRepository.findById(stopId)).thenReturn(Optional.of(stop2));
        when(routeRepository.save(any(Route.class))).thenReturn(route);

        // Act
        RouteResponse response = routeService.addStopToRoute(routeId, stopId, insertOrder);

        // Assert
        assertNotNull(response);
        verify(routeRepository, times(1)).save(any(Route.class));
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando la parada ya existe en la ruta")
    void shouldThrowExceptionWhenStopAlreadyExistsInRoute() {
        // Arrange
        Long routeId = 1L;
        Long stopId = 1L;

        Route route = createRoute(1L, "R001", "Ruta Norte", "Bogotá", "Medellín", 300.5f, 450.0f);
        Stop stop = createStop(stopId, "Bogotá", 1, 4.7110, -74.0721, route);
        route.getStops().add(stop);

        when(routeRepository.findRouteById(routeId)).thenReturn(Optional.of(route));

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> routeService.addStopToRoute(routeId, stopId, 1));

        assertEquals("La parada ID 1 ya existe en la ruta ID 1", exception.getMessage());
        verify(routeRepository, never()).save(any(Route.class));
    }

    @Test
    @DisplayName("Debe lanzar NotFoundException cuando la parada no existe")
    void shouldThrowNotFoundExceptionWhenStopNotExists() {
        // Arrange
        Long routeId = 1L;
        Long stopId = 999L;

        Route route = createRoute(1L, "R001", "Ruta Norte", "Bogotá", "Medellín", 300.5f, 450.0f);

        when(routeRepository.findRouteById(routeId)).thenReturn(Optional.of(route));
        when(stopRepository.findById(stopId)).thenReturn(Optional.empty());

        // Act & Assert
        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> routeService.addStopToRoute(routeId, stopId, 1));

        assertEquals("Parada no encontrada con ID: 999", exception.getMessage());
        verify(routeRepository, never()).save(any(Route.class));
    }

    @Test
    @DisplayName("Debe lanzar NotFoundException cuando la ruta no existe")
    void shouldThrowNotFoundExceptionWhenRouteNotExistsForAddStop() {
        // Arrange
        Long routeId = 999L;
        Long stopId = 1L;

        when(routeRepository.findRouteById(routeId)).thenReturn(Optional.empty());

        // Act & Assert
        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> routeService.addStopToRoute(routeId, stopId, 1));

        assertEquals("Ruta no encontrada con ID: 999", exception.getMessage());
    }

    // ======================================================================
    // TESTS - removeStopFromRoute
    // ======================================================================

    @Test
    @DisplayName("Debe remover una parada de la ruta exitosamente")
    void shouldRemoveStopFromRouteSuccessfully() {
        // Arrange
        Long routeId = 1L;
        Long stopId = 1L;

        Route route = createRoute(1L, "R001", "Ruta Norte", "Bogotá", "Medellín", 300.5f, 450.0f);
        Stop stop1 = createStop(1L, "Bogotá", 1, 4.7110, -74.0721, route);
        Stop stop2 = createStop(2L, "Medellín", 2, 6.2442, -75.5812, route);
        route.getStops().addAll(List.of(stop1, stop2));

        when(routeRepository.findRouteById(routeId)).thenReturn(Optional.of(route));
        when(routeRepository.save(any(Route.class))).thenReturn(route);
        when(stopRepository.save(any(Stop.class))).thenReturn(stop1);

        // Act
        routeService.removeStopFromRoute(routeId, stopId);

        // Assert
        verify(routeRepository, times(1)).save(any(Route.class));
        verify(stopRepository, times(1)).save(any(Stop.class));
    }

    @Test
    @DisplayName("Debe reajustar el orden de paradas después de remover una parada")
    void shouldReorderStopsAfterRemoval() {
        // Arrange
        Long routeId = 1L;
        Long stopId = 2L;

        Route route = createRoute(1L, "R001", "Ruta Norte", "Bogotá", "Medellín", 300.5f, 450.0f);
        Stop stop1 = createStop(1L, "Bogotá", 1, 4.7110, -74.0721, route);
        Stop stop2 = createStop(2L, "Armenia", 2, 4.5333, -75.7333, route);
        Stop stop3 = createStop(3L, "Medellín", 3, 6.2442, -75.5812, route);
        route.getStops().addAll(List.of(stop1, stop2, stop3));

        when(routeRepository.findRouteById(routeId)).thenReturn(Optional.of(route));
        when(routeRepository.save(any(Route.class))).thenReturn(route);
        when(stopRepository.save(any(Stop.class))).thenReturn(stop2);

        // Act
        routeService.removeStopFromRoute(routeId, stopId);

        // Assert
        verify(routeRepository, times(1)).save(any(Route.class));
        verify(stopRepository, times(1)).save(any(Stop.class));
    }

    @Test
    @DisplayName("Debe lanzar NotFoundException cuando la parada no pertenece a la ruta")
    void shouldThrowNotFoundExceptionWhenStopNotBelongsToRoute() {
        // Arrange
        Long routeId = 1L;
        Long stopId = 999L;

        Route route = createRoute(1L, "R001", "Ruta Norte", "Bogotá", "Medellín", 300.5f, 450.0f);
        Stop stop = createStop(1L, "Bogotá", 1, 4.7110, -74.0721, route);
        route.getStops().add(stop);

        when(routeRepository.findRouteById(routeId)).thenReturn(Optional.of(route));

        // Act & Assert
        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> routeService.removeStopFromRoute(routeId, stopId));

        assertEquals("La parada ID 999 no pertenece a la ruta ID 1", exception.getMessage());
        verify(routeRepository, never()).save(any(Route.class));
    }

    // ======================================================================
    // TESTS - reorderStops
    // ======================================================================

    @Test
    @DisplayName("Debe reordenar las paradas exitosamente con órdenes válidos")
    void shouldReorderStopsSuccessfully() {
        // Arrange
        Long routeId = 1L;

        Route route = createRoute(1L, "R001", "Ruta Norte", "Bogotá", "Medellín", 300.5f, 450.0f);
        Stop stop1 = createStop(1L, "Bogotá", 1, 4.7110, -74.0721, route);
        Stop stop2 = createStop(2L, "Armenia", 2, 4.5333, -75.7333, route);
        Stop stop3 = createStop(3L, "Medellín", 3, 6.2442, -75.5812, route);
        route.getStops().addAll(List.of(stop1, stop2, stop3));

        List<RouteService.StopOrderRequest> stopOrders = List.of(
                new StopOrderRequest(2L, 1),
                new StopOrderRequest(1L, 2),
                new StopOrderRequest(3L, 3)
        );

        when(routeRepository.findRouteById(routeId)).thenReturn(Optional.of(route));
        when(routeRepository.save(any(Route.class))).thenReturn(route);

        // Act
        RouteResponse response = routeService.reorderStops(routeId, stopOrders);

        // Assert
        assertNotNull(response);
        verify(routeRepository, times(1)).save(any(Route.class));
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando el tamaño de la lista no coincide con el número de paradas")
    void shouldThrowExceptionWhenStopOrderListSizeMismatch() {
        // Arrange
        Long routeId = 1L;

        Route route = createRoute(1L, "R001", "Ruta Norte", "Bogotá", "Medellín", 300.5f, 450.0f);
        Stop stop1 = createStop(1L, "Bogotá", 1, 4.7110, -74.0721, route);
        Stop stop2 = createStop(2L, "Armenia", 2, 4.5333, -75.7333, route);
        route.getStops().addAll(List.of(stop1, stop2));

        // Solo una parada en la lista cuando hay dos
        List<StopOrderRequest> stopOrders = List.of(
                new StopOrderRequest(1L, 1)
        );

        when(routeRepository.findRouteById(routeId)).thenReturn(Optional.of(route));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> routeService.reorderStops(routeId, stopOrders));

        assertEquals("La lista de reordenamiento debe incluir todas las 2 paradas de la ruta.", exception.getMessage());
        verify(routeRepository, never()).save(any(Route.class));
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando hay órdenes duplicados")
    void shouldThrowExceptionWhenStopOrdersAreDuplicated() {
        // Arrange
        Long routeId = 1L;

        Route route = createRoute(1L, "R001", "Ruta Norte", "Bogotá", "Medellín", 300.5f, 450.0f);
        Stop stop1 = createStop(1L, "Bogotá", 1, 4.7110, -74.0721, route);
        Stop stop2 = createStop(2L, "Armenia", 2, 4.5333, -75.7333, route);
        route.getStops().addAll(List.of(stop1, stop2));

        // Órdenes duplicados
        List<StopOrderRequest> stopOrders = List.of(
                new StopOrderRequest(1L, 1),
                new StopOrderRequest(2L, 1)  // Mismo orden que la anterior
        );

        when(routeRepository.findRouteById(routeId)).thenReturn(Optional.of(route));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> routeService.reorderStops(routeId, stopOrders));

        assertEquals("Los valores de orden (stopOrder) deben ser únicos.", exception.getMessage());
        verify(routeRepository, never()).save(any(Route.class));
    }

    @Test
    @DisplayName("Debe lanzar NotFoundException cuando una parada en reordenamiento no pertenece a la ruta")
    void shouldThrowNotFoundExceptionWhenStopNotInRouteForReorder() {
        // Arrange
        Long routeId = 1L;

        Route route = createRoute(1L, "R001", "Ruta Norte", "Bogotá", "Medellín", 300.5f, 450.0f);
        Stop stop1 = createStop(1L, "Bogotá", 1, 4.7110, -74.0721, route);
        route.getStops().add(stop1);

        List<StopOrderRequest> stopOrders = List.of(
                new StopOrderRequest(999L, 1)  // Stop que no existe en la ruta
        );

        when(routeRepository.findRouteById(routeId)).thenReturn(Optional.of(route));

        // Act & Assert
        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> routeService.reorderStops(routeId, stopOrders));

        assertEquals("La parada ID 999 no pertenece a la ruta ID 1", exception.getMessage());
        verify(routeRepository, never()).save(any(Route.class));
    }

    // ======================================================================
    // TESTS - getRouteById
    // ======================================================================

    @Test
    @DisplayName("Debe obtener una Ruta por ID exitosamente")
    void shouldGetRouteByIdSuccessfully() {
        // Arrange
        Long routeId = 1L;
        Route route = createRoute(1L, "R001", "Ruta Norte", "Bogotá", "Medellín", 300.5f, 450.0f);

        when(routeRepository.findRouteById(routeId)).thenReturn(Optional.of(route));

        // Act
        RouteResponse response = routeService.getRouteById(routeId);

        // Assert
        assertNotNull(response);
        assertEquals(routeId, response.id());
        assertEquals("R001", response.code());
        verify(routeRepository, times(1)).findRouteById(routeId);
    }

    @Test
    @DisplayName("Debe lanzar NotFoundException cuando la Ruta no existe")
    void shouldThrowNotFoundExceptionWhenGettingNonExistentRoute() {
        // Arrange
        Long routeId = 999L;

        when(routeRepository.findRouteById(routeId)).thenReturn(Optional.empty());

        // Act & Assert
        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> routeService.getRouteById(routeId));

        assertEquals("Ruta no encontrada con ID: 999", exception.getMessage());
    }

    // ======================================================================
    // TESTS - getAllRoutes
    // ======================================================================

    @Test
    @DisplayName("Debe obtener todas las Rutas exitosamente")
    void shouldGetAllRoutesSuccessfully() {
        // Arrange
        Route route1 = createRoute(1L, "R001", "Ruta Norte", "Bogotá", "Medellín", 300.5f, 450.0f);
        Route route2 = createRoute(2L, "R002", "Ruta Centro", "Bogotá", "Cali", 200.0f, 300.0f);

        when(routeRepository.findAll()).thenReturn(List.of(route1, route2));

        // Act
        List<RouteResponse> responses = routeService.getAllRoutes();

        // Assert
        assertNotNull(responses);
        assertEquals(2, responses.size());
        verify(routeRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Debe retornar lista vacía cuando no hay Rutas")
    void shouldReturnEmptyListWhenNoRoutes() {
        // Arrange
        when(routeRepository.findAll()).thenReturn(new ArrayList<>());

        // Act
        List<RouteResponse> responses = routeService.getAllRoutes();

        // Assert
        assertNotNull(responses);
        assertTrue(responses.isEmpty());
        verify(routeRepository, times(1)).findAll();
    }

    // ======================================================================
    // TESTS - searchRoutes
    // ======================================================================

    @Test
    @DisplayName("Debe buscar Rutas por origen y destino ordenadas por distancia")
    void shouldSearchRoutesByOriginAndDestination() {
        // Arrange
        Route route1 = createRoute(1L, "R001", "Ruta Norte", "Bogotá", "Medellín", 300.5f, 450.0f);
        Route route2 = createRoute(2L, "R002", "Ruta Centro", "Bogotá", "Medellín", 320.0f, 480.0f);

        when(routeRepository.findRoutesByOriginAndDestinationOrderByDistanceKmAsc("Bogotá", "Medellín"))
                .thenReturn(List.of(route1, route2));

        // Act
        List<RouteResponse> responses = routeService.searchRoutes("Bogotá", "Medellín");

        // Assert
        assertNotNull(responses);
        assertEquals(2, responses.size());
        verify(routeRepository, times(1)).findRoutesByOriginAndDestinationOrderByDistanceKmAsc("Bogotá", "Medellín");
    }

    @Test
    @DisplayName("Debe buscar Rutas por origen y destino cuando no hay rutas ordenadas por distancia")
    void shouldSearchRoutesByOriginAndDestinationWhenNoOrderedRoutes() {
        // Arrange
        Route route = createRoute(1L, "R001", "Ruta Norte", "Bogotá", "Medellín", 300.5f, 450.0f);

        when(routeRepository.findRoutesByOriginAndDestinationOrderByDistanceKmAsc("Bogotá", "Medellín"))
                .thenReturn(new ArrayList<>());
        when(routeRepository.findByOriginAndDestination("Bogotá", "Medellín"))
                .thenReturn(List.of(route));

        // Act
        List<RouteResponse> responses = routeService.searchRoutes("Bogotá", "Medellín");

        // Assert
        assertNotNull(responses);
        assertEquals(1, responses.size());
        verify(routeRepository, times(1)).findByOriginAndDestination("Bogotá", "Medellín");
    }

    @Test
    @DisplayName("Debe retornar lista vacía cuando no hay Rutas para origen y destino")
    void shouldReturnEmptyListWhenNoRoutesForSearch() {
        // Arrange
        when(routeRepository.findRoutesByOriginAndDestinationOrderByDistanceKmAsc("Bogotá", "Medellín"))
                .thenReturn(new ArrayList<>());
        when(routeRepository.findByOriginAndDestination("Bogotá", "Medellín"))
                .thenReturn(new ArrayList<>());

        // Act
        List<RouteResponse> responses = routeService.searchRoutes("Bogotá", "Medellín");

        // Assert
        assertNotNull(responses);
        assertTrue(responses.isEmpty());
    }

    // ======================================================================
    // TESTS - getStopsByRouteId
    // ======================================================================

    @Test
    @DisplayName("Debe obtener las paradas de una Ruta ordenadas por orden")
    void shouldGetStopsByRouteIdSuccessfully() {
        // Arrange
        Long routeId = 1L;
        Route route = createRoute(1L, "R001", "Ruta Norte", "Bogotá", "Medellín", 300.5f, 450.0f);

        Stop stop1 = createStop(1L, "Bogotá", 1, 4.7110, -74.0721, route);
        Stop stop2 = createStop(2L, "Armenia", 2, 4.5333, -75.7333, route);
        Stop stop3 = createStop(3L, "Medellín", 3, 6.2442, -75.5812, route);

        // Agregar en orden diferente para probar el ordenamiento
        route.getStops().addAll(List.of(stop3, stop1, stop2));

        when(routeRepository.findRouteById(routeId)).thenReturn(Optional.of(route));

        // Act
        List<StopResponse> responses = routeService.getStopsByRouteId(routeId);

        // Assert
        assertNotNull(responses);
        assertEquals(3, responses.size());
        // Verificar que están ordenados por orden ascendente
        assertEquals(1, responses.get(0).stopOrder());
        assertEquals(2, responses.get(1).stopOrder());
        assertEquals(3, responses.get(2).stopOrder());
        verify(routeRepository, times(1)).findRouteById(routeId);
    }

    @Test
    @DisplayName("Debe retornar lista vacía cuando la Ruta no tiene paradas")
    void shouldReturnEmptyListWhenRouteHasNoStops() {
        // Arrange
        Long routeId = 1L;
        Route route = createRoute(1L, "R001", "Ruta Norte", "Bogotá", "Medellín", 300.5f, 450.0f);

        when(routeRepository.findRouteById(routeId)).thenReturn(Optional.of(route));

        // Act
        List<StopResponse> responses = routeService.getStopsByRouteId(routeId);

        // Assert
        assertNotNull(responses);
        assertTrue(responses.isEmpty());
        verify(routeRepository, times(1)).findRouteById(routeId);
    }

    @Test
    @DisplayName("Debe lanzar NotFoundException cuando la Ruta no existe para obtener paradas")
    void shouldThrowNotFoundExceptionWhenRouteNotExistsForStops() {
        // Arrange
        Long routeId = 999L;

        when(routeRepository.findRouteById(routeId)).thenReturn(Optional.empty());

        // Act & Assert
        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> routeService.getStopsByRouteId(routeId));

        assertEquals("Ruta no encontrada con ID: 999", exception.getMessage());
    }
}
