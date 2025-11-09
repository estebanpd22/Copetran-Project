package co.unimagdalena.repositories;

import co.unimagdalena.domine.entities.Route;
import co.unimagdalena.domine.entities.Stop;
import co.unimagdalena.domine.repositories.RouteRepository;
import co.unimagdalena.domine.repositories.StopRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

public class StopRepositoryTest extends AbstractRepositoryTI {
    @Autowired
    private StopRepository stopRepository;
    @Autowired
    private RouteRepository routeRepository;

    private Route createRoute(String code, String name, String origin, String destination,
                              Float distanceKm, Float durationMin) {
        return routeRepository.save(Route.builder()
                .code(code)
                .name(name)
                .origin(origin)
                .destination(destination)
                .distanceKm(distanceKm)
                .durationMin(durationMin)
                .build());
    }

    private Stop createStop(Route route, String name, Integer order, double latitude, double longitude) {
        return stopRepository.save(Stop.builder()
                .route(route)
                .name(name)
                .order(order)
                .latitude(latitude)
                .longitude(longitude)
                .build());
    }

    @Test
    @DisplayName("Debe encontrar una parada por ID")
    void shouldFindStopById() {
        // Given
        Route route = createRoute("R001", "Ruta Norte", "Bogotá", "Medellín", 400.0f, 480.0f);
        Stop stop = createStop(route, "Terminal Bogotá", 1, 4.6097, -74.0817);

        // When
        Optional<Stop> found = stopRepository.findStopById(stop.getId());

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(stop.getId());
        assertThat(found.get().getName()).isEqualTo("Terminal Bogotá");
        assertThat(found.get().getOrder()).isEqualTo(1);
        assertThat(found.get().getLatitude()).isEqualTo(4.6097);
        assertThat(found.get().getLongitude()).isEqualTo(-74.0817);
    }

    @Test
    @DisplayName("Debe encontrar paradas por routeId ordenadas por orden ascendente")
    void shouldFindByRouteIdOrderByOrderAsc() {
        // Given
        Route route = createRoute("R002", "Ruta Sur", "Cali", "Pasto", 300.0f, 360.0f);
        createStop(route, "Terminal Cali", 1, 3.4516, -76.5320);
        createStop(route, "Popayán", 2, 2.4448, -76.6147);
        createStop(route, "Terminal Pasto", 3, 1.2136, -77.2811);

        // When
        List<Stop> stops = stopRepository.findByRouteIdOrderByOrderAsc(route.getId());

        // Then
        assertThat(stops).hasSize(3);
        assertThat(stops.get(0).getOrder()).isEqualTo(1);
        assertThat(stops.get(1).getOrder()).isEqualTo(2);
        assertThat(stops.get(2).getOrder()).isEqualTo(3);
        assertThat(stops.get(0).getName()).isEqualTo("Terminal Cali");
        assertThat(stops.get(2).getName()).isEqualTo("Terminal Pasto");
    }

    @Test
    @DisplayName("Debe encontrar una parada por routeId y order")
    void shouldFindByRouteIdAndOrder() {
        // Given
        Route route = createRoute("R003", "Ruta Oriente", "Bucaramanga", "Cúcuta", 200.0f, 240.0f);
        createStop(route, "Terminal Bucaramanga", 1, 7.1193, -73.1227);
        Stop stop2 = createStop(route, "Pamplona", 2, 7.3756, -72.6483);

        // When
        Optional<Stop> found = stopRepository.findByRouteIdAndOrder(route.getId(), 2);

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Pamplona");
        assertThat(found.get().getOrder()).isEqualTo(2);
    }

    @Test
    @DisplayName("Debe verificar si existe una parada por routeId y stopId")
    void shouldExistsByRouteIdAndId() {
        // Given
        Route route = createRoute("R004", "Ruta Costa", "Barranquilla", "Cartagena", 120.0f, 150.0f);
        Stop stop = createStop(route, "Terminal Barranquilla", 1, 10.9639, -74.7964);

        // When
        boolean exists = stopRepository.existsByRouteIdAndId(route.getId(), stop.getId());

        // Then
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("Debe encontrar paradas entre dos órdenes")
    void shouldFindStopsBetween() {
        // Given
        Route route = createRoute("R005", "Ruta Eje", "Pereira", "Cali", 200.0f, 240.0f);
        createStop(route, "Terminal Pereira", 1, 4.8133, -75.6961);
        createStop(route, "Armenia", 2, 4.5339, -75.6811);
        createStop(route, "Cartago", 3, 4.7469, -75.9114);
        createStop(route, "Buga", 4, 3.9006, -76.2989);
        createStop(route, "Terminal Cali", 5, 3.4516, -76.5320);

        // When
        List<Stop> stops = stopRepository.findStopsBetween(route.getId(), 2, 4);

        // Then
        assertThat(stops).hasSize(3);
        assertThat(stops.get(0).getOrder()).isEqualTo(2);
        assertThat(stops.get(1).getOrder()).isEqualTo(3);
        assertThat(stops.get(2).getOrder()).isEqualTo(4);
        assertThat(stops.get(0).getName()).isEqualTo("Armenia");
        assertThat(stops.get(2).getName()).isEqualTo("Buga");
    }

    @Test
    @DisplayName("Debe retornar empty cuando no existe la parada")
    void shouldReturnEmptyWhenStopNotFound() {
        // When
        Optional<Stop> found = stopRepository.findStopById(999L);

        // Then
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("Debe retornar lista vacía cuando no hay paradas para la ruta")
    void shouldReturnEmptyListWhenNoStopsForRoute() {
        // Given
        Route route = createRoute("R006", "Ruta Vacía", "Origen", "Destino", 100.0f, 120.0f);

        // When
        List<Stop> stops = stopRepository.findByRouteIdOrderByOrderAsc(route.getId());

        // Then
        assertThat(stops).isEmpty();
    }

    @Test
    @DisplayName("Debe retornar false cuando no existe la parada para la ruta")
    void shouldReturnFalseWhenStopDoesNotExistForRoute() {
        // Given
        Route route = createRoute("R007", "Ruta Cualquiera", "A", "B", 50.0f, 60.0f);

        // When
        boolean exists = stopRepository.existsByRouteIdAndId(route.getId(), 999L);

        // Then
        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("Debe retornar lista vacía cuando no hay paradas en el rango especificado")
    void shouldReturnEmptyListWhenNoStopsInRange() {
        // Given
        Route route = createRoute("R008", "Ruta Corta", "X", "Y", 30.0f, 40.0f);
        createStop(route, "Stop 1", 1, 5.0, -75.0);

        // When
        List<Stop> stops = stopRepository.findStopsBetween(route.getId(), 5, 10);

        // Then
        assertThat(stops).isEmpty();
    }
}
