package co.unimagdalena.api;

import co.unimagdalena.api.dto.RouteDto.*;
import co.unimagdalena.api.dto.StopDto;
import co.unimagdalena.services.RouteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Validated
@RequestMapping("/api/v1/routes")
public class RouteController {
    private final RouteService routeService;

    @PostMapping
    public ResponseEntity<RouteResponse> create(@Validated @RequestBody RouteCreateRequest request,
                                                UriComponentsBuilder uriBuilder) {
        var routeCreated = routeService.createRoute(request);
        var location = uriBuilder.path("/api/v1/routes/{id}")
                .buildAndExpand(routeCreated.id())
                .toUri();
        return ResponseEntity.created(location).body(routeCreated);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Void> update(@PathVariable Long id,
                                       @Validated @RequestBody RouteUpdateRequest request) {
        routeService.updateRoute(id, request);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        routeService.deleteRoute(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<RouteResponse> get(@PathVariable Long id) {
        return ResponseEntity.ok(routeService.getRouteById(id));
    }

    @GetMapping
    public ResponseEntity<List<RouteResponse>> getAll() {
        return ResponseEntity.ok(routeService.getAllRoutes());
    }

    @GetMapping("/search")
    public ResponseEntity<List<RouteResponse>> search(@RequestParam String origin,
                                                      @RequestParam String destination) {
        return ResponseEntity.ok(routeService.searchRoutes(origin, destination));
    }

    @GetMapping("/{id}/stops")
    public ResponseEntity<List<StopDto.StopResponse>> getStops(@PathVariable Long id) {
        return ResponseEntity.ok(routeService.getStopsByRouteId(id));
    }

    @PostMapping("/{routeId}/stops/{stopId}")
    public ResponseEntity<RouteResponse> addStop(@PathVariable Long routeId,
                                                 @PathVariable Long stopId,
                                                 @RequestParam int stopOrder) {
        return ResponseEntity.ok(routeService.addStopToRoute(routeId, stopId, stopOrder));
    }

    @DeleteMapping("/{routeId}/stops/{stopId}")
    public ResponseEntity<Void> removeStop(@PathVariable Long routeId,
                                           @PathVariable Long stopId) {
        routeService.removeStopFromRoute(routeId, stopId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{routeId}/stops/reorder")
    public ResponseEntity<RouteResponse> reorderStops(@PathVariable Long routeId,
                                                      @Validated @RequestBody List<RouteService.StopOrderRequest> stopOrders) {
        return ResponseEntity.ok(routeService.reorderStops(routeId, stopOrders));
    }
}
