package co.unimagdalena.api;

import co.unimagdalena.api.dto.SeatHoldDto.*;
import co.unimagdalena.services.SeatHoldService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.OffsetDateTime;
import java.util.List;

@RestController
@RequiredArgsConstructor
@Validated
@RequestMapping("/api/v1/seat-holds")
public class SeatHoldController {
    private final SeatHoldService seatHoldService;

    @PostMapping
    public ResponseEntity<SeatHoldResponse> create(@Validated @RequestBody SeatHoldCreateRequest request,
                                                   UriComponentsBuilder uriBuilder) {
        var holdCreated = seatHoldService.createSeatHold(request);
        var location = uriBuilder.path("/api/v1/seat-holds/{id}")
                .buildAndExpand(holdCreated.id())
                .toUri();
        return ResponseEntity.created(location).body(holdCreated);
    }

    @GetMapping("/{id}")
    public ResponseEntity<SeatHoldResponse> get(@PathVariable Long id) {
        return ResponseEntity.ok(seatHoldService.getHoldById(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> release(@PathVariable Long id) {
        seatHoldService.releaseSeatHold(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/trip/{tripId}")
    public ResponseEntity<List<SeatHoldResponse>> getByTrip(@PathVariable Long tripId) {
        return ResponseEntity.ok(seatHoldService.getActiveHoldsByTrip(tripId));
    }

    @GetMapping("/trip/{tripId}/user/{userId}")
    public ResponseEntity<List<SeatHoldResponse>> getByTripAndUser(@PathVariable Long tripId,
                                                                   @PathVariable Long userId) {
        return ResponseEntity.ok(seatHoldService.getActiveHoldsByTripAndUser(tripId, userId));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<SeatHoldResponse>> getByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(seatHoldService.getHoldsByUser(userId));
    }

    @GetMapping("/check")
    public ResponseEntity<Boolean> checkSeatOnHold(@RequestParam Long tripId,
                                                   @RequestParam String seatNumber) {
        return ResponseEntity.ok(seatHoldService.isSeatOnHold(tripId, seatNumber));
    }

    @GetMapping("/expiration-time")
    public ResponseEntity<OffsetDateTime> getExpirationTime() {
        return ResponseEntity.ok(seatHoldService.calculateExpirationTime());
    }
}
