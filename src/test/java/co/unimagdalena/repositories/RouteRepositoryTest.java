package co.unimagdalena.repositories;

import co.unimagdalena.domine.entities.Route;
import co.unimagdalena.domine.repositories.RouteRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

public class RouteRepositoryTest extends AbstractRepositoryTI {
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

    @Test
    @DisplayName("Debe encontrar una ruta por ID")
    void shouldFindRouteById() {
        // Given
        Route route = createRoute("R001", "Ruta Norte", "Bogotá", "Medellín", 400.0f, 480.0f);

        // When
        Optional<Route> found = routeRepository.findRouteById(route.getId());

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(route.getId());
        assertThat(found.get().getCode()).isEqualTo("R001");
        assertThat(found.get().getName()).isEqualTo("Ruta Norte");
        assertThat(found.get().getOrigin()).isEqualTo("Bogotá");
        assertThat(found.get().getDestination()).isEqualTo("Medellín");
        assertThat(found.get().getDistanceKm()).isEqualTo(400.0f);
        assertThat(found.get().getDurationMin()).isEqualTo(480.0f);
    }

    @Test
    @DisplayName("Debe encontrar una ruta por código")
    void shouldFindByCode() {
        // Given
        createRoute("R002", "Ruta Sur", "Cali", "Pasto", 300.0f, 360.0f);

        // When
        Optional<Route> found = routeRepository.findByCode("R002");

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getCode()).isEqualTo("R002");
        assertThat(found.get().getName()).isEqualTo("Ruta Sur");
    }

    @Test
    @DisplayName("Debe encontrar rutas por nombre")
    void shouldFindRoutesByName() {
        // Given
        createRoute("R003", "Ruta Oriente", "Bucaramanga", "Cúcuta", 200.0f, 240.0f);
        createRoute("R004", "Ruta Oriente", "Bogotá", "Villavicencio", 120.0f, 150.0f);

        // When
        List<Route> routes = routeRepository.findRoutesByName("Ruta Oriente");

        // Then
        assertThat(routes).hasSize(2);
        assertThat(routes).allMatch(r -> r.getName().equals("Ruta Oriente"));
    }

    @Test
    @DisplayName("Debe encontrar rutas por origen y destino")
    void shouldFindByOriginAndDestination() {
        // Given
        createRoute("R005", "Ruta Costa 1", "Barranquilla", "Cartagena", 120.0f, 150.0f);
        createRoute("R006", "Ruta Costa 2", "Barranquilla", "Cartagena", 130.0f, 160.0f);
        createRoute("R007", "Ruta Eje", "Pereira", "Armenia", 50.0f, 60.0f);

        // When
        List<Route> routes = routeRepository.findByOriginAndDestination("Barranquilla", "Cartagena");

        // Then
        assertThat(routes).hasSize(2);
        assertThat(routes).allMatch(r -> r.getOrigin().equals("Barranquilla") &&
                r.getDestination().equals("Cartagena"));
    }

    @Test
    @DisplayName("Debe encontrar rutas por origen y destino ordenadas por distancia ascendente")
    void shouldFindRoutesByOriginAndDestinationOrderByDistanceKmAsc() {
        // Given
        createRoute("R008", "Ruta Rápida", "Bogotá", "Girardot", 150.0f, 180.0f);
        createRoute("R009", "Ruta Panorámica", "Bogotá", "Girardot", 180.0f, 240.0f);
        createRoute("R010", "Ruta Directa", "Bogotá", "Girardot", 140.0f, 170.0f);

        // When
        List<Route> routes = routeRepository.findRoutesByOriginAndDestinationOrderByDistanceKmAsc("Bogotá", "Girardot");

        // Then
        assertThat(routes).hasSize(3);
        assertThat(routes.get(0).getDistanceKm()).isEqualTo(140.0f);
        assertThat(routes.get(1).getDistanceKm()).isEqualTo(150.0f);
        assertThat(routes.get(2).getDistanceKm()).isEqualTo(180.0f);
    }

    @Test
    @DisplayName("Debe encontrar rutas por origen y destino ordenadas por duración ascendente")
    void shouldFindRoutesByOriginAndDestinationOrderByDurationMinAsc() {
        // Given
        createRoute("R011", "Ruta Express", "Medellín", "Manizales", 180.0f, 210.0f);
        createRoute("R012", "Ruta Normal", "Medellín", "Manizales", 190.0f, 240.0f);
        createRoute("R013", "Ruta Económica", "Medellín", "Manizales", 200.0f, 270.0f);

        // When
        List<Route> routes = routeRepository.findRoutesByOriginAndDestinationOrderByDurationMinAsc("Medellín", "Manizales");

        // Then
        assertThat(routes).hasSize(3);
        assertThat(routes.get(0).getDurationMin()).isEqualTo(210.0f);
        assertThat(routes.get(1).getDurationMin()).isEqualTo(240.0f);
        assertThat(routes.get(2).getDurationMin()).isEqualTo(270.0f);
    }

    @Test
    @DisplayName("Debe retornar empty cuando no existe la ruta")
    void shouldReturnEmptyWhenRouteNotFound() {
        // When
        Optional<Route> found = routeRepository.findByCode("NONEXISTENT");

        // Then
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("Debe retornar lista vacía cuando no hay rutas con origen y destino especificados")
    void shouldReturnEmptyListWhenNoRoutesWithOriginAndDestination() {
        // Given
        createRoute("R014", "Ruta Cualquiera", "Popayán", "Neiva", 250.0f, 300.0f);

        // When
        List<Route> routes = routeRepository.findByOriginAndDestination("Tunja", "Duitama");

        // Then
        assertThat(routes).isEmpty();
    }
}
