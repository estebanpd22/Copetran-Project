package co.unimagdalena.services;

import co.unimagdalena.api.dto.StopDto.*;
import co.unimagdalena.domine.entities.Route;
import co.unimagdalena.domine.entities.Stop;
import co.unimagdalena.domine.entities.FareRule;
import co.unimagdalena.domine.repositories.StopRepository;
<<<<<<< Updated upstream
=======
import co.unimagdalena.exception.NotFoundException;
>>>>>>> Stashed changes
import co.unimagdalena.services.impl.StopServiceImpl;
import co.unimagdalena.services.mapper.StopMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("StopServiceImpl - Tests de la clase StopServiceImpl")
class StopServiceImplTest {

    @Mock
    private StopRepository stopRepository;

    @Mock
    private StopMapper stopMapper;

    @InjectMocks
    private StopServiceImpl stopService;

    private Stop testStop;
    private Route testRoute;
    private StopCreateRequest createRequest;
    private StopUpdateRequest updateRequest;
    private StopResponse stopResponse;

    @BeforeEach
    void setUp() {
        testRoute = createTestRoute();
        testStop = createTestStop();
        createRequest = createTestStopCreateRequest();
        updateRequest = createTestStopUpdateRequest();
        stopResponse = createTestStopResponse();
    }

    // ==================== TESTS: createStop ====================

    @Test
    @DisplayName("Debe crear una parada exitosamente con ruta asociada")
    void shouldCreateStopWithRoute() {
        // Arrange
        Stop mappedStop = createTestStop();
        mappedStop.setRoute(testRoute);
        mappedStop.setOrder(1);

        when(stopMapper.toEntity(createRequest)).thenReturn(mappedStop);
        when(stopRepository.save(mappedStop)).thenReturn(mappedStop);
        when(stopMapper.toResponse(mappedStop)).thenReturn(stopResponse);

        // Act
        StopResponse result = stopService.createStop(createRequest);

        // Assert
        assertNotNull(result);
        assertEquals(stopResponse, result);
        verify(stopRepository, times(1)).save(mappedStop);
        verify(stopMapper, times(1)).toEntity(createRequest);
        verify(stopMapper, times(1)).toResponse(mappedStop);
    }

    @Test
    @DisplayName("Debe resetear el orden a null cuando se crea una parada sin ruta asociada")
    void shouldResetOrderToNullWhenStopCreatedWithoutRoute() {
        // Arrange
        Stop stopWithoutRoute = createTestStop();
        stopWithoutRoute.setRoute(null);
        stopWithoutRoute.setOrder(5); // Orden establecido incorrectamente

        when(stopMapper.toEntity(createRequest)).thenReturn(stopWithoutRoute);
        when(stopRepository.save(stopWithoutRoute)).thenReturn(stopWithoutRoute);
        when(stopMapper.toResponse(stopWithoutRoute)).thenReturn(stopResponse);

        // Act
        StopResponse result = stopService.createStop(createRequest);

        // Assert
        assertNotNull(result);
        assertNull(stopWithoutRoute.getOrder(), "El orden debe ser null cuando no hay ruta asociada");
        verify(stopRepository, times(1)).save(stopWithoutRoute);
    }

    @Test
    @DisplayName("Debe crear una parada con orden null cuando no tiene ruta y orden es null")
    void shouldCreateStopWithoutOrderAndRoute() {
        // Arrange
        Stop stopWithoutOrderAndRoute = createTestStop();
        stopWithoutOrderAndRoute.setRoute(null);
        stopWithoutOrderAndRoute.setOrder(null);

        when(stopMapper.toEntity(createRequest)).thenReturn(stopWithoutOrderAndRoute);
        when(stopRepository.save(stopWithoutOrderAndRoute)).thenReturn(stopWithoutOrderAndRoute);
        when(stopMapper.toResponse(stopWithoutOrderAndRoute)).thenReturn(stopResponse);

        // Act
        StopResponse result = stopService.createStop(createRequest);

        // Assert
        assertNotNull(result);
        assertNull(stopWithoutOrderAndRoute.getRoute());
        assertNull(stopWithoutOrderAndRoute.getOrder());
        verify(stopRepository, times(1)).save(stopWithoutOrderAndRoute);
    }

    // ==================== TESTS: getStopById ====================

    @Test
    @DisplayName("Debe obtener una parada por ID exitosamente")
    void shouldGetStopByIdSuccessfully() {
        // Arrange
        Long stopId = 1L;
        when(stopRepository.findStopById(stopId)).thenReturn(Optional.of(testStop));
        when(stopMapper.toResponse(testStop)).thenReturn(stopResponse);

        // Act
        StopResponse result = stopService.getStopById(stopId);

        // Assert
        assertNotNull(result);
        assertEquals(stopResponse, result);
        verify(stopRepository, times(1)).findStopById(stopId);
        verify(stopMapper, times(1)).toResponse(testStop);
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando la parada no existe")
    void shouldThrowExceptionWhenStopNotFound() {
        // Arrange
        Long stopId = 999L;
        when(stopRepository.findStopById(stopId)).thenReturn(Optional.empty());

        // Act & Assert
<<<<<<< Updated upstream
        assertThrows(IllegalArgumentException.class, () -> stopService.getStopById(stopId),
                "Debe lanzar IllegalArgumentException cuando la parada no existe");
=======
        assertThrows(NotFoundException.class, () -> stopService.getStopById(stopId),
                "Debe lanzar NotFoundException cuando la parada no existe");
>>>>>>> Stashed changes
        verify(stopRepository, times(1)).findStopById(stopId);
        verify(stopMapper, never()).toResponse(any());
    }

    // ==================== TESTS: updateStop ====================

    @Test
    @DisplayName("Debe actualizar una parada exitosamente")
    void shouldUpdateStopSuccessfully() {
        // Arrange
        Long stopId = 1L;
        Stop updatedStop = createTestStop();
        updatedStop.setId(stopId);
        updatedStop.setName("Parada Actualizada");

        when(stopRepository.findStopById(stopId)).thenReturn(Optional.of(updatedStop));
        doNothing().when(stopMapper).updateEntity(updateRequest, updatedStop);
        when(stopRepository.save(updatedStop)).thenReturn(updatedStop);
        when(stopMapper.toResponse(updatedStop)).thenReturn(stopResponse);

        // Act
        StopResponse result = stopService.updateStop(stopId, updateRequest);

        // Assert
        assertNotNull(result);
        assertEquals(stopResponse, result);
        verify(stopRepository, times(1)).findStopById(stopId);
        verify(stopMapper, times(1)).updateEntity(updateRequest, updatedStop);
        verify(stopRepository, times(1)).save(updatedStop);
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando intenta actualizar una parada inexistente")
    void shouldThrowExceptionWhenUpdatingNonExistentStop() {
        // Arrange
        Long stopId = 999L;
        when(stopRepository.findStopById(stopId)).thenReturn(Optional.empty());

        // Act & Assert
<<<<<<< Updated upstream
        assertThrows(IllegalArgumentException.class, () -> stopService.updateStop(stopId, updateRequest),
                "Debe lanzar IllegalArgumentException cuando la parada no existe");
=======
        assertThrows(NotFoundException.class, () -> stopService.updateStop(stopId, updateRequest),
                "Debe lanzar NotFoundException cuando la parada no existe");
>>>>>>> Stashed changes
        verify(stopRepository, times(1)).findStopById(stopId);
        verify(stopMapper, never()).updateEntity(any(), any());
    }

    @Test
    @DisplayName("Debe actualizar solo los campos proporcionados en la solicitud")
    void shouldUpdateOnlyProvidedFields() {
        // Arrange
        Long stopId = 1L;
        Stop existingStop = createTestStop();
        existingStop.setId(stopId);
        String originalName = existingStop.getName();

        when(stopRepository.findStopById(stopId)).thenReturn(Optional.of(existingStop));
        doNothing().when(stopMapper).updateEntity(updateRequest, existingStop);
        when(stopRepository.save(existingStop)).thenReturn(existingStop);
        when(stopMapper.toResponse(existingStop)).thenReturn(stopResponse);

        // Act
        StopResponse result = stopService.updateStop(stopId, updateRequest);

        // Assert
        assertNotNull(result);
        verify(stopMapper, times(1)).updateEntity(updateRequest, existingStop);
    }

    // ==================== TESTS: deleteStop ====================

    @Test
    @DisplayName("Debe eliminar una parada exitosamente sin reglas de tarifa asociadas")
    void shouldDeleteStopSuccessfully() {
        // Arrange
        Long stopId = 1L;
        Stop stopToDelete = createTestStop();
        stopToDelete.setId(stopId);
        stopToDelete.setFareRulesFrom(new ArrayList<>());
        stopToDelete.setFareRulesTo(new ArrayList<>());

        when(stopRepository.findStopById(stopId)).thenReturn(Optional.of(stopToDelete));
        doNothing().when(stopRepository).delete(stopToDelete);

        // Act
        stopService.deleteStop(stopId);

        // Assert
        verify(stopRepository, times(1)).findStopById(stopId);
        verify(stopRepository, times(1)).delete(stopToDelete);
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando intenta eliminar parada con FareRules \"from\" asociadas")
    void shouldThrowExceptionWhenStopHasFareRulesFrom() {
        // Arrange
        Long stopId = 1L;
        Stop stopWithFareRulesFrom = createTestStop();
        stopWithFareRulesFrom.setId(stopId);
        FareRule fareRule = createTestFareRule();
        stopWithFareRulesFrom.setFareRulesFrom(List.of(fareRule));
        stopWithFareRulesFrom.setFareRulesTo(new ArrayList<>());

        when(stopRepository.findStopById(stopId)).thenReturn(Optional.of(stopWithFareRulesFrom));

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> stopService.deleteStop(stopId),
                "Debe lanzar IllegalStateException cuando hay FareRules asociadas");
        verify(stopRepository, never()).delete(any());
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando intenta eliminar parada con FareRules \"to\" asociadas")
    void shouldThrowExceptionWhenStopHasFareRulesTo() {
        // Arrange
        Long stopId = 1L;
        Stop stopWithFareRulesTo = createTestStop();
        stopWithFareRulesTo.setId(stopId);
        FareRule fareRule = createTestFareRule();
        stopWithFareRulesTo.setFareRulesFrom(new ArrayList<>());
        stopWithFareRulesTo.setFareRulesTo(List.of(fareRule));

        when(stopRepository.findStopById(stopId)).thenReturn(Optional.of(stopWithFareRulesTo));

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> stopService.deleteStop(stopId),
                "Debe lanzar IllegalStateException cuando hay FareRules asociadas");
        verify(stopRepository, never()).delete(any());
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando intenta eliminar parada inexistente")
    void shouldThrowExceptionWhenDeletingNonExistentStop() {
        // Arrange
        Long stopId = 999L;
        when(stopRepository.findStopById(stopId)).thenReturn(Optional.empty());

        // Act & Assert
<<<<<<< Updated upstream
        assertThrows(IllegalArgumentException.class, () -> stopService.deleteStop(stopId),
                "Debe lanzar IllegalArgumentException cuando la parada no existe");
=======
        assertThrows(NotFoundException.class, () -> stopService.deleteStop(stopId),
                "Debe lanzar NotFoundException cuando la parada no existe");
>>>>>>> Stashed changes
        verify(stopRepository, never()).delete(any());
    }

    // ==================== TESTS: getStopsByCity ====================

    @Test
    @DisplayName("Debe obtener paradas filtrando por ciudad (coincidencia en nombre)")
    void shouldGetStopsByCity() {
        // Arrange
        String city = "Cartagena";
        Stop stop1 = createTestStop();
        stop1.setName("Parada Cartagena Centro");
        Stop stop2 = createTestStop();
        stop2.setName("Terminal Cartagena");

        when(stopRepository.findAll()).thenReturn(List.of(stop1, stop2));
        when(stopMapper.toResponse(stop1)).thenReturn(stopResponse);
        when(stopMapper.toResponse(stop2)).thenReturn(stopResponse);

        // Act
        List<StopResponse> result = stopService.getStopsByCity(city);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(stopRepository, times(1)).findAll();
        verify(stopMapper, times(2)).toResponse(any());
    }

    @Test
    @DisplayName("Debe retornar lista vacía cuando no hay paradas en la ciudad")
    void shouldReturnEmptyListWhenNoCityMatches() {
        // Arrange
        String city = "CiudadInexistente";
        when(stopRepository.findAll()).thenReturn(new ArrayList<>());

        // Act
        List<StopResponse> result = stopService.getStopsByCity(city);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(stopRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Debe filtrar paradas por ciudad sin importar mayúsculas/minúsculas")
    void shouldFilterCityIgnoreCase() {
        // Arrange
        String city = "cartagena";
        Stop stop = createTestStop();
        stop.setName("PARADA CARTAGENA");

        when(stopRepository.findAll()).thenReturn(List.of(stop));
        when(stopMapper.toResponse(stop)).thenReturn(stopResponse);

        // Act
        List<StopResponse> result = stopService.getStopsByCity(city);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(stopRepository, times(1)).findAll();
    }

    // ==================== TESTS: findStopsNearLocation ====================

    @Test
    @DisplayName("Debe encontrar paradas dentro del radio especificado")
    void shouldFindStopsNearLocation() {
        // Arrange
        double latitude = 10.3906;
        double longitude = -75.5078; // Cartagena, Colombia
        double radiusKm = 5.0;

        Stop nearbyStop = createTestStop();
        nearbyStop.setLatitude(10.3950);
        nearbyStop.setLongitude(-75.5100);

        when(stopRepository.findAll()).thenReturn(List.of(nearbyStop));
        when(stopMapper.toResponse(nearbyStop)).thenReturn(stopResponse);

        // Act
        List<StopResponse> result = stopService.findStopsNearLocation(latitude, longitude, radiusKm);

        // Assert
        assertNotNull(result);
        assertTrue(result.size() > 0, "Debe encontrar paradas dentro del radio");
        verify(stopRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Debe retornar lista vacía cuando no hay paradas en el radio")
    void shouldReturnEmptyListWhenNoStopsNearLocation() {
        // Arrange
        double latitude = 10.3906;
        double longitude = -75.5078;
        double radiusKm = 0.1; // Radio muy pequeño

        Stop distantStop = createTestStop();
        distantStop.setLatitude(0.0); // Coordenada muy lejana
        distantStop.setLongitude(0.0);

        when(stopRepository.findAll()).thenReturn(List.of(distantStop));

        // Act
        List<StopResponse> result = stopService.findStopsNearLocation(latitude, longitude, radiusKm);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty(), "No debe encontrar paradas fuera del radio");
        verify(stopRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Debe calcular correctamente la distancia Haversine")
    void shouldCalculateHaversineDistanceCorrectly() {
        // Arrange
        double latitude = 0.0;
        double longitude = 0.0;
        double radiusKm = 1.0;

        // Parada a aproximadamente 0.009 km (9 metros) del punto de origen
        Stop closeStop = createTestStop();
        closeStop.setLatitude(0.00008);
        closeStop.setLongitude(0.00008);

        when(stopRepository.findAll()).thenReturn(List.of(closeStop));
        when(stopMapper.toResponse(closeStop)).thenReturn(stopResponse);

        // Act
        List<StopResponse> result = stopService.findStopsNearLocation(latitude, longitude, radiusKm);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size(), "Debe encontrar la parada cercana");
        verify(stopRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Debe manejar coordenadas en diferentes hemisferios")
    void shouldHandleCoordinatesInDifferentHemispheres() {
        // Arrange
        double latitude = 40.7128; // Nueva York
        double longitude = -74.0060;
        double radiusKm = 100.0;

        Stop southernStop = createTestStop();
        southernStop.setLatitude(-33.8688); // Sydney
        southernStop.setLongitude(151.2093);

        when(stopRepository.findAll()).thenReturn(List.of(southernStop));

        // Act
        List<StopResponse> result = stopService.findStopsNearLocation(latitude, longitude, radiusKm);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty(), "Sydney no está dentro de 100km de Nueva York");
        verify(stopRepository, times(1)).findAll();
    }

    // ==================== TESTS: getAllActiveStops ====================

    @Test
    @DisplayName("Debe obtener todas las paradas activas exitosamente")
    void shouldGetAllActiveStops() {
        // Arrange
        Stop stop1 = createTestStop();
        Stop stop2 = createTestStop();

        when(stopRepository.findAll()).thenReturn(List.of(stop1, stop2));
        when(stopMapper.toResponse(stop1)).thenReturn(stopResponse);
        when(stopMapper.toResponse(stop2)).thenReturn(stopResponse);

        // Act
        List<StopResponse> result = stopService.getAllActiveStops();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(stopRepository, times(1)).findAll();
        verify(stopMapper, times(2)).toResponse(any());
    }

    @Test
    @DisplayName("Debe retornar lista vacía cuando no hay paradas")
    void shouldReturnEmptyListWhenNoStops() {
        // Arrange
        when(stopRepository.findAll()).thenReturn(new ArrayList<>());

        // Act
        List<StopResponse> result = stopService.getAllActiveStops();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(stopRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Debe mapear correctamente todas las paradas retornadas")
    void shouldMapAllStopsCorrectly() {
        // Arrange
        Stop stop1 = createTestStop();
        stop1.setName("Parada 1");
        Stop stop2 = createTestStop();
        stop2.setName("Parada 2");

        StopResponse response1 = createTestStopResponse();
        StopResponse response2 = createTestStopResponse();

        when(stopRepository.findAll()).thenReturn(List.of(stop1, stop2));
        when(stopMapper.toResponse(stop1)).thenReturn(response1);
        when(stopMapper.toResponse(stop2)).thenReturn(response2);

        // Act
        List<StopResponse> result = stopService.getAllActiveStops();

        // Assert
        assertEquals(2, result.size());
        assertTrue(result.contains(response1));
        assertTrue(result.contains(response2));
    }

    // ==================== HELPER METHODS ====================

    private Stop createTestStop() {
        return Stop.builder()
                .id(1L)
                .name("Parada Centro")
                .order(1)
                .latitude(10.3906)
                .longitude(-75.5078)
                .route(testRoute)
                .fareRulesFrom(new ArrayList<>())
                .fareRulesTo(new ArrayList<>())
                .build();
    }

    private Route createTestRoute() {
        return Route.builder()
                .id(1L)
                .code("RT001")
                .name("Ruta Principal")
                .origin("Cartagena")
                .destination("Santa Marta")
                .distanceKm(150f)
                .durationMin(180f)
                .stops(new ArrayList<>())
                .build();
    }

    private StopCreateRequest createTestStopCreateRequest() {
        return new StopCreateRequest(
                "Parada Centro",
                1,
                10.3906,
                -75.5078,
                1L
        );
    }

    private StopUpdateRequest createTestStopUpdateRequest() {
        return new StopUpdateRequest(
                "Parada Centro Actualizada",
                1,
                10.3906,
                -75.5078,
                1L
        );
    }

    private StopResponse createTestStopResponse() {
        return new StopResponse(
                1L,
                "Parada Centro",
                1,
                10.3906,
                -75.5078
        );
    }

    private FareRule createTestFareRule() {
        return FareRule.builder()
                .id(1L)
                .basePrice(java.math.BigDecimal.valueOf(50000))
                .dynamicPricing(co.unimagdalena.domine.entities.DynamicPricing.OFF)
                .discounts(new HashMap<>())
                .route(testRoute)
                .fromStop(testStop)
                .toStop(testStop)
                .build();
    }
<<<<<<< Updated upstream
}
=======
}
>>>>>>> Stashed changes
