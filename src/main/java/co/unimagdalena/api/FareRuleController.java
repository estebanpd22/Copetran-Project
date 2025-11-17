package co.unimagdalena.api;

import co.unimagdalena.api.dto.FareRuleDto.*;
import co.unimagdalena.services.FareRuleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequiredArgsConstructor
@Validated
@RequestMapping("/api/v1/fareRules")
public class FareRuleController {
    private final FareRuleService fareRuleService;

    @PostMapping
    public ResponseEntity<FareRuleResponse> create(@Validated @RequestBody FareRuleCreateRequest request,
                                                   UriComponentsBuilder uriBuilder) {
        var fareRuleCreated = fareRuleService.createFareRule(request);
        var location = uriBuilder.path("/api/v1/fareRules/{id}")
                .buildAndExpand(fareRuleCreated.id())
                .toUri();
        return ResponseEntity.created(location).body(fareRuleCreated);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Void> update(@PathVariable Long id,
                                       @Validated @RequestBody FareRuleUpdateRequest request) {
        fareRuleService.updateFareRule(id, request);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        fareRuleService.deleteFareRule(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<FareRuleResponse> get(@PathVariable Long id) {
        return ResponseEntity.ok(fareRuleService.getFareRule(id));
    }

    @GetMapping
    public ResponseEntity<List<FareRuleResponse>> getAll() {
        return ResponseEntity.ok(fareRuleService.getAllFareRules());
    }

    @GetMapping("/route/{routeId}")
    public ResponseEntity<List<FareRuleResponse>> getByRoute(@PathVariable Long routeId) {
        return ResponseEntity.ok(fareRuleService.getFareRulesByRouteId(routeId));
    }

    @GetMapping("/calculate-price")
    public ResponseEntity<BigDecimal> calculatePrice(@RequestParam Long routeId,
                                                     @RequestParam Long fromStopId,
                                                     @RequestParam Long toStopId,
                                                     @RequestParam Long passengerId,
                                                     @RequestParam Long busId,
                                                     @RequestParam String seatNumber,
                                                     @RequestParam Long tripId) {
        return ResponseEntity.ok(fareRuleService.getFinalTicketPrice(
                routeId, fromStopId, toStopId, passengerId, busId, seatNumber, tripId));
    }
}
