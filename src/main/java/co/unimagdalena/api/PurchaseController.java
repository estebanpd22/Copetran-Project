package co.unimagdalena.api;

import co.unimagdalena.api.dto.PurchaseDto.*;
import co.unimagdalena.services.PurchaseService;
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
@RequestMapping("/api/v1/purchases")
public class PurchaseController {
    private final PurchaseService purchaseService;

    @PostMapping
    public ResponseEntity<PurchaseResponse> create(@Validated @RequestBody PurchaseCreateRequest request,
                                                   UriComponentsBuilder uriBuilder) {
        var purchaseCreated = purchaseService.createPurchase(request);
        var location = uriBuilder.path("/api/v1/purchases/{id}")
                .buildAndExpand(purchaseCreated.id())
                .toUri();
        return ResponseEntity.created(location).body(purchaseCreated);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PurchaseResponse> get(@PathVariable Long id) {
        return ResponseEntity.ok(purchaseService.getPurchase(id));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<PurchaseResponse>> getByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(purchaseService.getPurchasesByUserId(userId));
    }

    @GetMapping("/date-range")
    public ResponseEntity<List<PurchaseResponse>> getByDateRange(@RequestParam OffsetDateTime start,
                                                                 @RequestParam OffsetDateTime end) {
        return ResponseEntity.ok(purchaseService.getPurchasesByDateRange(start, end));
    }

    @PostMapping("/{id}/confirm")
    public ResponseEntity<Void> confirm(@PathVariable Long id,
                                        @RequestParam String paymentReference) {
        purchaseService.confirmPurchase(id, paymentReference);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<Void> cancel(@PathVariable Long id) {
        purchaseService.cancelPurchase(id);
        return ResponseEntity.noContent().build();
    }
}
