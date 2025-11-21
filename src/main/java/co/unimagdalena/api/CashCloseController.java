package co.unimagdalena.api;

import co.unimagdalena.api.dto.CashCloseDto.*;
import co.unimagdalena.services.CashCloseService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.OffsetDateTime;
import java.util.List;

@RestController
@RequiredArgsConstructor
@Validated
@RequestMapping("/api/v1/cash")
public class CashCloseController {
    private final CashCloseService cashCloseService;

    @PostMapping("/close")
    @PreAuthorize("hasAnyRole('CLERK', 'DRIVER', 'ADMIN')")
    public ResponseEntity<CashCloseResponse> closeCash(@Validated @RequestBody CashCloseRequest request,
                                                        UriComponentsBuilder uriBuilder) {
        var cashCloseCreated = cashCloseService.closeCash(request);
        var location = uriBuilder.path("/api/v1/cash/{id}")
                .buildAndExpand(cashCloseCreated.id())
                .toUri();
        return ResponseEntity.created(location).body(cashCloseCreated);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('CLERK', 'DRIVER', 'ADMIN')")
    public ResponseEntity<CashCloseResponse> get(@PathVariable Long id) {
        return ResponseEntity.ok(cashCloseService.getCashCloseById(id));
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasAnyRole('CLERK', 'DRIVER', 'ADMIN')")
    public ResponseEntity<List<CashCloseResponse>> getByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(cashCloseService.getCashClosesByUser(userId));
    }

    @GetMapping("/trip/{tripId}")
    @PreAuthorize("hasAnyRole('CLERK', 'DRIVER', 'ADMIN')")
    public ResponseEntity<List<CashCloseResponse>> getByTrip(@PathVariable Long tripId) {
        return ResponseEntity.ok(cashCloseService.getCashClosesByTrip(tripId));
    }

    @GetMapping("/date-range")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<CashCloseResponse>> getByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime endDate) {
        return ResponseEntity.ok(cashCloseService.getCashClosesByDateRange(startDate, endDate));
    }
}
