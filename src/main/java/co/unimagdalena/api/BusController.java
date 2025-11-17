package co.unimagdalena.api;

import co.unimagdalena.api.dto.BusDto.*;
import co.unimagdalena.domine.entities.BusStatus;
import co.unimagdalena.services.BusService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Validated
@RequestMapping("/api/v1/buses")
public class BusController {
    private final BusService busService;

    @PostMapping
    public ResponseEntity<BusResponse> create(@Validated @RequestBody BusCreateRequest request,
                                              UriComponentsBuilder uriBuilder) {
        var busCreated = busService.createBus(request);
        var location = uriBuilder.path("/api/v1/buses/{id}")
                .buildAndExpand(busCreated.id())
                .toUri();
        return ResponseEntity.created(location).body(busCreated);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<BusResponse> update(@PathVariable Long id,
                                              @Validated @RequestBody BusUpdateRequest request) {
        return ResponseEntity.ok(busService.updateBus(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        busService.deleteBus(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<BusResponse> get(@PathVariable Long id) {
        return ResponseEntity.ok(busService.getBusById(id));
    }

    @GetMapping("/plate/{licensePlate}")
    public ResponseEntity<BusResponse> getByPlate(@PathVariable String licensePlate) {
        return ResponseEntity.ok(busService.getBusByLicensePlate(licensePlate));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<BusResponse>> getByStatus(@PathVariable BusStatus status) {
        return ResponseEntity.ok(busService.getBusesByStatus(status));
    }

    @GetMapping("/capacity/{requiredCapacity}")
    public ResponseEntity<List<BusResponse>> getByCapacity(@PathVariable int requiredCapacity) {
        return ResponseEntity.ok(busService.findBusesByCapacity(requiredCapacity));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<Void> updateStatus(@PathVariable Long id,
                                             @RequestParam BusStatus status) {
        busService.updateBusStatus(id, status);
        return ResponseEntity.noContent().build();
    }
}
