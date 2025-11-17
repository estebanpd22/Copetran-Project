package co.unimagdalena.api;

import co.unimagdalena.api.dto.SeatDto.*;
import co.unimagdalena.services.SeatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Validated
@RequestMapping("/api/v1/seats")
public class SeatController {
    private final SeatService seatService;

    @PostMapping
    public ResponseEntity<SeatResponse> create(@Validated @RequestBody SeatCreateRequest request,
                                               UriComponentsBuilder uriBuilder) {
        var seatCreated = seatService.createSeat(request);
        var location = uriBuilder.path("/api/v1/seats/{id}")
                .buildAndExpand(seatCreated.id())
                .toUri();
        return ResponseEntity.created(location).body(seatCreated);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<SeatResponse> update(@PathVariable Long id,
                                               @Validated @RequestBody SeatUpdateRequest request) {
        return ResponseEntity.ok(seatService.updateSeat(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        seatService.deleteSeat(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<SeatResponse> get(@PathVariable Long id) {
        return ResponseEntity.ok(seatService.getSeatById(id));
    }

    @GetMapping("/bus/{busId}/type/{seatType}")
    public ResponseEntity<List<SeatResponse>> getByBusAndType(@PathVariable Long busId,
                                                              @PathVariable String seatType) {
        return ResponseEntity.ok(seatService.getSeatsByBusIdAndType(busId, seatType));
    }

    @GetMapping("/feature/{feature}")
    public ResponseEntity<List<SeatResponse>> getByFeature(@PathVariable String feature) {
        return ResponseEntity.ok(seatService.getSeatsByFeature(feature));
    }
}
