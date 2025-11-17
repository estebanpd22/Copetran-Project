package co.unimagdalena.api;

import co.unimagdalena.api.dto.StopDto.*;
import co.unimagdalena.services.StopService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Validated
@RequestMapping("/api/v1/stops")
public class StopController {
    private final StopService stopService;

    @PostMapping
    public ResponseEntity<StopResponse> create(@Validated @RequestBody StopCreateRequest request,
                                               UriComponentsBuilder uriBuilder) {
        var stopCreated = stopService.createStop(request);
        var location = uriBuilder.path("/api/v1/stops/{id}")
                .buildAndExpand(stopCreated.id())
                .toUri();
        return ResponseEntity.created(location).body(stopCreated);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<StopResponse> update(@PathVariable Long id,
                                               @Validated @RequestBody StopUpdateRequest request) {
        return ResponseEntity.ok(stopService.updateStop(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        stopService.deleteStop(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<StopResponse> get(@PathVariable Long id) {
        return ResponseEntity.ok(stopService.getStopById(id));
    }

    @GetMapping
    public ResponseEntity<List<StopResponse>> getAllActive() {
        return ResponseEntity.ok(stopService.getAllActiveStops());
    }

    @GetMapping("/city/{city}")
    public ResponseEntity<List<StopResponse>> getByCity(@PathVariable String city) {
        return ResponseEntity.ok(stopService.getStopsByCity(city));
    }

    @GetMapping("/near")
    public ResponseEntity<List<StopResponse>> getNearLocation(@RequestParam double latitude,
                                                              @RequestParam double longitude,
                                                              @RequestParam double radiusKm) {
        return ResponseEntity.ok(stopService.findStopsNearLocation(latitude, longitude, radiusKm));
    }
}
