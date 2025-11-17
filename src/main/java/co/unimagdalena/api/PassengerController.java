package co.unimagdalena.api;

import co.unimagdalena.api.dto.PassengerDto.*;
import co.unimagdalena.services.PassengerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Validated
@RequestMapping("/api/v1/passengers")
public class PassengerController {
    private final PassengerService passengerService;

    @PostMapping
    public ResponseEntity<PassengerResponse> create(@Validated @RequestBody PassengerCreateRequest request,
                                                    UriComponentsBuilder uriBuilder) {
        var passengerCreated = passengerService.createPassenger(request);
        var location = uriBuilder.path("/api/v1/passengers/{id}")
                .buildAndExpand(passengerCreated.id())
                .toUri();
        return ResponseEntity.created(location).body(passengerCreated);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Void> update(@PathVariable Long id,
                                       @Validated @RequestBody PassengerUpdateRequest request) {
        passengerService.updatePassenger(id, request);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        passengerService.deletePassenger(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<PassengerResponse> get(@PathVariable Long id) {
        return ResponseEntity.ok(passengerService.getPassengerById(id));
    }

    @GetMapping("/document/{documentNumber}")
    public ResponseEntity<PassengerResponse> getByDocument(@PathVariable String documentNumber) {
        return ResponseEntity.ok(passengerService.finByDocumentNumber(documentNumber));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<PassengerResponse>> getByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(passengerService.getPassengerByUser(userId));
    }
}
