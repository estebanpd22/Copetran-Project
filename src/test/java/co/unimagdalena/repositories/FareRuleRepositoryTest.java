package co.unimagdalena.repositories;

import co.unimagdalena.domine.entities.*;
import co.unimagdalena.domine.repositories.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

public class FareRuleRepositoryTest extends AbstractRepositoryTI {
    @Autowired
    private FareRuleRepository fareRuleRepository;
    @Autowired
    private RouteRepository routeRepository;
    @Autowired
    private StopRepository stopRepository;

    private Route createRoute(String code, String name, String origin, String destination, Float distanceKm, Float durationMin) {
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

    private FareRule createFareRule(Route route, Stop fromStop, Stop toStop, BigDecimal basePrice, DynamicPricing dynamicPricing, Map<String, Double> discounts) {
        return fareRuleRepository.save(FareRule.builder()
                .route(route)
                .fromStop(fromStop)
                .toStop(toStop)
                .basePrice(basePrice)
                .dynamicPricing(dynamicPricing)
                .discounts(discounts)
                .build());
    }

    @Test
    @DisplayName("Debe encontrar una FareRule por routeId, fromStopId y toStopId")
    void shouldFindByRouteIdAndFromStopIdAndToStopId() {
        // Given
        Route route = createRoute("R001", "Ruta Norte", "Bogotá", "Medellín", 400.0f, 480.0f);
        Stop stop1 = createStop(route, "Terminal Bogotá", 1, 4.6097, -74.0817);
        Stop stop2 = createStop(route, "Terminal Medellín", 2, 6.2442, -75.5812);

        Map<String, Double> discounts = new HashMap<>();
        discounts.put("student", 0.15);

        FareRule fareRule = createFareRule(route, stop1, stop2, BigDecimal.valueOf(80000), DynamicPricing.ON, discounts);

        // When
        Optional<FareRule> found = fareRuleRepository.findByRouteIdAndFromStopIdAndToStopId(route.getId(), stop1.getId(), stop2.getId());

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getId()).isNotNull();
        assertThat(found.get().getBasePrice()).isEqualByComparingTo(BigDecimal.valueOf(80000));
        assertThat(found.get().getDynamicPricing()).isEqualTo(DynamicPricing.ON);
        assertThat(found.get().getRoute().getId()).isEqualTo(route.getId());
        assertThat(found.get().getFromStop().getId()).isEqualTo(stop1.getId());
        assertThat(found.get().getToStop().getId()).isEqualTo(stop2.getId());
        assertThat(found.get().getDiscounts()).containsEntry("student", 0.15);
    }

    @Test
    @DisplayName("Debe encontrar todas las FareRules de una ruta")
    void shouldFindByRouteId() {
        // Given
        Route route = createRoute("R002", "Ruta Sur", "Cali", "Pasto", 300.0f, 360.0f);
        Stop stop1 = createStop(route, "Terminal Cali", 1, 3.4516, -76.5320);
        Stop stop2 = createStop(route, "Popayán", 2, 2.4448, -76.6147);
        Stop stop3 = createStop(route, "Terminal Pasto", 3, 1.2136, -77.2811);

        createFareRule(route, stop1, stop2, BigDecimal.valueOf(50000), DynamicPricing.OFF, new HashMap<>());
        createFareRule(route, stop2, stop3, BigDecimal.valueOf(40000), DynamicPricing.OFF, new HashMap<>());

        // When
        List<FareRule> fareRules = fareRuleRepository.findByRouteId(route.getId());

        // Then
        assertThat(fareRules).hasSize(2);
        assertThat(fareRules).allMatch(fr -> fr.getRoute().getId().equals(route.getId()));
    }

    @Test
    @DisplayName("Debe encontrar FareRules por routeId y dynamicPricing")
    void shouldFindByRouteIdAndDynamicPricing() {
        // Given
        Route route = createRoute("R003", "Ruta Oriente", "Bucaramanga", "Cúcuta", 200.0f, 240.0f);
        Stop stop1 = createStop(route, "Terminal Bucaramanga", 1, 7.1193, -73.1227);
        Stop stop2 = createStop(route, "Terminal Cúcuta", 2, 7.8939, -72.5078);

        createFareRule(route, stop1, stop2, BigDecimal.valueOf(60000), DynamicPricing.ON, new HashMap<>());
        createFareRule(route, stop2, stop1, BigDecimal.valueOf(55000), DynamicPricing.OFF, new HashMap<>());

        // When
        List<FareRule> withDynamicPricing = fareRuleRepository.findByRouteIdAndDynamicPricing(route.getId(), DynamicPricing.ON);

        // Then
        assertThat(withDynamicPricing).hasSize(1);
        assertThat(withDynamicPricing.get(0).getDynamicPricing()).isEqualTo(DynamicPricing.ON);
        assertThat(withDynamicPricing.get(0).getBasePrice()).isEqualByComparingTo(BigDecimal.valueOf(60000));
    }

    @Test
    @DisplayName("Debe cambiar el estado de dynamicPricing de una FareRule")
    void shouldChangeDynamicPricing() {
        // Given
        Route route = createRoute("R004", "Ruta Costa", "Cartagena", "Barranquilla", 120.0f, 150.0f);
        Stop stop1 = createStop(route, "Terminal Cartagena", 1, 10.3910, -75.4794);
        Stop stop2 = createStop(route, "Terminal Barranquilla", 2, 10.9639, -74.7964);

        FareRule fareRule = createFareRule(route, stop1, stop2, BigDecimal.valueOf(45000), DynamicPricing.OFF, new HashMap<>());

        // When
        fareRuleRepository.changeDynamicPricing(fareRule.getId(), DynamicPricing.ON);
        fareRuleRepository.flush();

        // Then
        Optional<FareRule> updated = fareRuleRepository.findById(fareRule.getId());
        assertThat(updated).isPresent();
        assertThat(updated.get().getDynamicPricing()).isEqualTo(DynamicPricing.ON);
    }

    @Test
    @DisplayName("Debe retornar empty cuando no existe la FareRule")
    void shouldReturnEmptyWhenFareRuleNotFound() {
        // When
        Optional<FareRule> found = fareRuleRepository.findByRouteIdAndFromStopIdAndToStopId(999L, 999L, 999L);

        // Then
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("Debe retornar lista vacía cuando no hay FareRules para la ruta")
    void shouldReturnEmptyListWhenNoFareRulesForRoute() {
        // When
        List<FareRule> fareRules = fareRuleRepository.findByRouteId(999L);

        // Then
        assertThat(fareRules).isEmpty();
    }
}