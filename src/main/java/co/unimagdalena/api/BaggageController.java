package co.unimagdalena.api;

import co.unimagdalena.api.dto.BaggageDto.*;
import co.unimagdalena.services.BaggageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequiredArgsConstructor
@Validated
@RequestMapping("/api/v1/baggages")
public class BaggageController {
    private final BaggageService baggageService;

    @PatchMapping("/{id}")
    public ResponseEntity<BaggageResponse> update(@PathVariable Long id,
                                                  @Validated @RequestBody BaggageUpdateRequest request) {
        return ResponseEntity.ok(baggageService.updateBaggage(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        baggageService.deleteBaggage(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<BaggageResponse> get(@PathVariable Long id) {
        return ResponseEntity.ok(baggageService.getBaggageById(id));
    }

    @GetMapping("/tag/{tagCode}")
    public ResponseEntity<BaggageResponse> getByTag(@PathVariable String tagCode) {
        return ResponseEntity.ok(baggageService.getBaggageByTagCode(tagCode));
    }

    @GetMapping("/ticket/{ticketId}")
    public ResponseEntity<List<BaggageResponse>> getByTicket(@PathVariable Long ticketId) {
        return ResponseEntity.ok(baggageService.getBaggageByTicketId(ticketId));
    }

    @PostMapping("/{id}/assign-tag")
    public ResponseEntity<Void> assignTag(@PathVariable Long id,
                                          @RequestParam String tagCode) {
        baggageService.assignTagCode(id, tagCode);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/calculate-fee")
    public ResponseEntity<BigDecimal> calculateFee(@RequestParam Double weightKg) {
        return ResponseEntity.ok(baggageService.calculateFee(weightKg));
    }
}
