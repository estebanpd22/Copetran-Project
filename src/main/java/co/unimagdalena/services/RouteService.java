package co.unimagdalena.services;

import co.unimagdalena.api.dto.RouteDto.*;
import co.unimagdalena.api.dto.StopDto.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.io.Serializable;
import java.util.List;

public interface RouteService {

    RouteResponse createRoute(RouteCreateRequest request);
    void updateRoute(Long id, RouteUpdateRequest request);
    void deleteRoute(Long id);

    RouteResponse addStopToRoute(Long routeId, Long stopId, int stopOrder);
    void removeStopFromRoute(Long routeId, Long stopId);
    RouteResponse reorderStops(Long routeId, List<StopOrderRequest> stopOrders);
    RouteResponse getRouteById(Long id);

    record StopOrderRequest(
            @NotNull Long stopId,
            @NotNull @PositiveOrZero int stopOrder
    ) implements Serializable {}

    List<RouteResponse> getAllRoutes();
    List<RouteResponse> searchRoutes(String origin, String destination);
    List<StopResponse> getStopsByRouteId(Long id);
}
