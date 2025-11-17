package co.unimagdalena.api;

import co.unimagdalena.api.dto.ParcelDto.*;
import co.unimagdalena.services.ParcelService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Validated
@RequestMapping("/api/v1/parcels")
public class ParcelController {
    private final ParcelService parcelService;

    @PostMapping
    public ResponseEntity<ParcelResponse> create(@Validated @RequestBody ParcelCreateRequest request,
                                                 UriComponentsBuilder uriBuilder) {
        var parcelCreated = parcelService.createParcel(request);
        var location = uriBuilder.path("/api/v1/parcels/{code}")
                .buildAndExpand(parcelCreated.code())
                .toUri();
        return ResponseEntity.created(location).body(parcelCreated);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Void> update(@PathVariable Long id,
                                       @Validated @RequestBody ParcelUpdateRequest request) {
        parcelService.updateParcel(id, request);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/code/{code}")
    public ResponseEntity<ParcelResponse> getByCode(@PathVariable String code) {
        return ResponseEntity.ok(parcelService.getParcelByCode(code));
    }

    @GetMapping("/trip/{tripId}")
    public ResponseEntity<List<ParcelResponse>> getByTrip(@PathVariable Long tripId) {
        return ResponseEntity.ok(parcelService.getParcelsByTrip(tripId));
    }

    @GetMapping("/sender/{senderPhone}")
    public ResponseEntity<List<ParcelResponse>> getBySender(@PathVariable String senderPhone) {
        return ResponseEntity.ok(parcelService.getParcelsBySender(senderPhone));
    }

    @GetMapping("/receiver/{receiverPhone}")
    public ResponseEntity<List<ParcelResponse>> getByReceiver(@PathVariable String receiverPhone) {
        return ResponseEntity.ok(parcelService.getParcelsByReceiver(receiverPhone));
    }

    @PostMapping("/{id}/assign-trip")
    public ResponseEntity<Void> assignTrip(@PathVariable Long id,
                                           @RequestParam Long tripId) {
        parcelService.assignTrip(id, tripId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/confirm-delivery")
    public ResponseEntity<Void> confirmDelivery(@PathVariable Long id,
                                                @RequestParam String otp,
                                                @RequestParam String proofPhotoUrl) {
        parcelService.confirmDelivery(id, otp, proofPhotoUrl);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/mark-failed")
    public ResponseEntity<Void> markFailed(@PathVariable Long id,
                                           @RequestParam String failureNote) {
        parcelService.markDeliveryFailed(id, failureNote);
        return ResponseEntity.noContent().build();
    }
}
