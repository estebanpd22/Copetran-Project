package co.unimagdalena.api;

import co.unimagdalena.api.dto.TripDto.*;
import co.unimagdalena.domine.entities.TripStatus;
import co.unimagdalena.services.TripService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
@Validated
@RequestMapping("/api/v1/trips")
public class TripController {
    private final TripService tripService;

    @PostMapping
    public ResponseEntity<TripResponse> create(@Validated @RequestBody TripCreateRequest request,
                                               UriComponentsBuilder uriBuilder) {
        var tripCreated = tripService.createTrip(request);
        var location = uriBuilder.path("/api/v1/trips/{id}")
                .buildAndExpand(tripCreated.id())
                .toUri();
        return ResponseEntity.created(location).body(tripCreated);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Void> update(@PathVariable Long id,
                                       @Validated @RequestBody TripUpdateRequest request) {
        tripService.updateTrip(id, request);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        tripService.deleteTrip(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<TripResponse> get(@PathVariable Long id) {
        return ResponseEntity.ok(tripService.getTripDetails(id));
    }

    @GetMapping("/search")
    public ResponseEntity<List<TripResponse>> search(@RequestParam String origin,
                                                     @RequestParam String destination,
                                                     @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(tripService.getTrips(origin, destination, date));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<Void> updateStatus(@PathVariable Long id,
                                             @RequestParam TripStatus status) {
        tripService.updateTripStatus(id, status);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/statistics")
    public ResponseEntity<Long> getStatistics(@PathVariable Long id) {
        return ResponseEntity.ok(tripService.getTripStatistics(id));
    }

    @GetMapping("/{id}/check-overbooking")
    public ResponseEntity<Boolean> checkOverbooking(@PathVariable Long id) {
        return ResponseEntity.ok(tripService.checkOverbookingConditions(id));
    }
}
