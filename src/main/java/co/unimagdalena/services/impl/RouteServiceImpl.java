package co.unimagdalena.services.impl;

import co.unimagdalena.api.dto.RouteDto.*;
import co.unimagdalena.api.dto.StopDto;
import co.unimagdalena.domine.entities.Route;
import co.unimagdalena.domine.entities.Stop;
import co.unimagdalena.domine.repositories.RouteRepository;
import co.unimagdalena.domine.repositories.StopRepository;
import co.unimagdalena.exception.NotFoundException;
import co.unimagdalena.services.RouteService;
import co.unimagdalena.services.mapper.RouteMapper;
import co.unimagdalena.services.mapper.StopMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class RouteServiceImpl implements RouteService {

    private final RouteRepository routeRepository;
    private final RouteMapper routeMapper;
    private final StopRepository stopRepository;
    private final StopMapper stopMapper; // Para getStopsByRouteId

    @Override
    @Transactional
    public RouteResponse createRoute(RouteCreateRequest request) {
        // Validación de unicidad de código
        if (routeRepository.findByCode(request.code()).isPresent()) {
            throw new IllegalStateException("El código de ruta " + request.code() + " ya existe.");
        }

        Route route = routeMapper.toEntity(request);
        Route savedRoute = routeRepository.save(route);
        log.info("Ruta creada con ID: {}", savedRoute.getId());

        return routeMapper.toResponse(savedRoute);
    }

    @Override
    @Transactional
    public void updateRoute(Long id, RouteUpdateRequest request) {
        Route route = findRouteById(id);

        // Validación de unicidad de código (si el código cambia)
        if (request.code() != null && !request.code().equals(route.getCode())) {
            Optional<Route> existingRoute = routeRepository.findByCode(request.code());
            if (existingRoute.isPresent() && !existingRoute.get().getId().equals(id)) {
                throw new IllegalStateException("El código de ruta " + request.code() + " ya está en uso por otra ruta.");
            }
        }

        routeMapper.updateEntity(request, route);
        routeRepository.save(route);
        log.info("Ruta ID {} actualizada.", id);
    }

    @Override
    @Transactional
    public void deleteRoute(Long id) {
        Route route = findRouteById(id);
        if (!route.getTrips().isEmpty()) {
            log.warn("Intento de eliminar ruta {} con {} viajes asociados.", id, route.getTrips().size());
            throw new IllegalStateException("No se puede eliminar la ruta ID " + id + " porque tiene viajes asociados.");
        }
        routeRepository.delete(route);
        log.info("Ruta ID {} eliminada.", id);
    }

    @Override
    @Transactional
    public RouteResponse addStopToRoute(Long routeId, Long stopId, int stopOrder) {
        Route route = findRouteById(routeId);
        Stop stop = stopRepository.findById(stopId)
                .orElseThrow(() -> new NotFoundException("Parada no encontrada con ID: " + stopId));

        if (route.getStops().stream().anyMatch(s -> s.getId().equals(stopId))) {
            throw new IllegalStateException("La parada ID " + stopId + " ya existe en la ruta ID " + routeId);
        }

        stop.setOrder(stopOrder);

        route.addStop(stop);

        route.getStops().stream()
                .filter(s -> !s.getId().equals(stopId) && s.getOrder() >= stopOrder)
                .forEach(s -> s.setOrder(s.getOrder() + 1));

        Route savedRoute = routeRepository.save(route);
        log.info("Parada ID {} añadida a ruta ID {} en orden {}", stopId, routeId, stopOrder);

        return routeMapper.toResponse(savedRoute);
    }

    @Override
    @Transactional
    public void removeStopFromRoute(Long routeId, Long stopId) {
        Route route = findRouteById(routeId);

        Optional<Stop> stopToRemoveOpt = route.getStops().stream()
                .filter(s -> s.getId().equals(stopId))
                .findFirst();

        if (stopToRemoveOpt.isEmpty()) {
            throw new NotFoundException("La parada ID " + stopId + " no pertenece a la ruta ID " + routeId);
        }

        Stop stopToRemove = stopToRemoveOpt.get();
        route.getStops().remove(stopToRemove);
        stopToRemove.setRoute(null); // Romper la relación bidireccional

        // Reajustar el orden de las paradas restantes
        final int removedOrder = stopToRemove.getOrder();
        route.getStops().stream()
                .filter(s -> s.getOrder() > removedOrder)
                .forEach(s -> s.setOrder(s.getOrder() - 1));

        routeRepository.save(route);
        stopRepository.save(stopToRemove); // Persistir el cambio de Route a null
        log.info("Parada ID {} eliminada de la ruta ID {}.", stopId, routeId);
    }

    @Override
    @Transactional
    public RouteResponse reorderStops(Long routeId, List<StopOrderRequest> stopOrders) {
        Route route = findRouteById(routeId);

        // 1. Crear un mapa de las paradas que se van a reordenar para validación y acceso rápido
        Map<Long, Stop> currentStopsMap = route.getStops().stream()
                .collect(Collectors.toMap(Stop::getId, s -> s));

        if (stopOrders.size() != route.getStops().size()) {
            throw new IllegalArgumentException("La lista de reordenamiento debe incluir todas las " + route.getStops().size() + " paradas de la ruta.");
        }

        // 2. Validación de unicidad de orden (el orden no debe repetirse)
        long distinctOrders = stopOrders.stream().map(StopOrderRequest::stopOrder).distinct().count();
        if (distinctOrders != stopOrders.size()) {
            throw new IllegalArgumentException("Los valores de orden (stopOrder) deben ser únicos.");
        }

        // 3. Aplicar el nuevo orden
        for (StopOrderRequest orderRequest : stopOrders) {
            Stop stopToUpdate = currentStopsMap.get(orderRequest.stopId());
            if (stopToUpdate == null) {
                throw new NotFoundException("La parada ID " + orderRequest.stopId() + " no pertenece a la ruta ID " + routeId);
            }
            stopToUpdate.setOrder(orderRequest.stopOrder());
        }

        Route savedRoute = routeRepository.save(route);
        log.info("Orden de paradas actualizado en ruta ID {}.", routeId);

        return routeMapper.toResponse(savedRoute);
    }

    // --- BÚSQUEDAS (Finders) ---

    @Override
    @Transactional(readOnly = true)
    public RouteResponse getRouteById(Long id) {
        Route route = findRouteById(id);
        return routeMapper.toResponse(route);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RouteResponse> getAllRoutes() {
        return routeRepository.findAll().stream()
                .map(routeMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<RouteResponse> searchRoutes(String origin, String destination) {

        List<Route> routes = routeRepository.findRoutesByOriginAndDestinationOrderByDistanceKmAsc(origin, destination);

        if (routes.isEmpty()) {
            // Si no hay rutas ordenadas por distancia, probamos solo por origen/destino
            routes = routeRepository.findByOriginAndDestination(origin, destination);
        }

        return routes.stream()
                .map(routeMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<StopDto.StopResponse> getStopsByRouteId(Long id) {
        Route route = findRouteById(id);

        return route.getStops().stream()
                // Aseguramos que las paradas se devuelvan en el orden correcto
                .sorted(Comparator.comparing(Stop::getOrder))
                .map(stopMapper::toResponse)
                .collect(Collectors.toList());
    }

    private Route findRouteById(Long id) {
        return routeRepository.findRouteById(id)
                .orElseThrow(() -> new NotFoundException("Ruta no encontrada con ID: " + id));
    }
}
