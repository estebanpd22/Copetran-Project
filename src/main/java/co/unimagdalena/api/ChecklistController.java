package co.unimagdalena.api;

import co.unimagdalena.api.dto.ChecklistDto.*;
import co.unimagdalena.services.ChecklistService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

@RestController
@RequiredArgsConstructor
@Validated
@RequestMapping("/api/v1/checklists")
public class ChecklistController {
    private final ChecklistService checklistService;

    @PostMapping
    @PreAuthorize("hasAnyRole('DISPATCHER', 'DRIVER', 'ADMIN')")
    public ResponseEntity<ChecklistResponse> create(@Validated @RequestBody ChecklistCreateRequest request,
                                                    UriComponentsBuilder uriBuilder) {
        var checklistCreated = checklistService.createChecklist(request);
        var location = uriBuilder.path("/api/v1/checklists/{id}")
                .buildAndExpand(checklistCreated.id())
                .toUri();
        return ResponseEntity.created(location).body(checklistCreated);
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAnyRole('DISPATCHER', 'DRIVER', 'ADMIN')")
    public ResponseEntity<Void> update(@PathVariable Long id,
                                       @Validated @RequestBody ChecklistUpdateRequest request) {
        checklistService.updateChecklist(id, request);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ChecklistResponse> get(@PathVariable Long id) {
        return ResponseEntity.ok(checklistService.getChecklistById(id));
    }

    @GetMapping("/trip/{tripId}")
    public ResponseEntity<ChecklistResponse> getByTrip(@PathVariable Long tripId) {
        return ResponseEntity.ok(checklistService.getChecklistByTripId(tripId));
    }

    @PostMapping("/{id}/complete")
    @PreAuthorize("hasAnyRole('DISPATCHER', 'DRIVER', 'ADMIN')")
    public ResponseEntity<ChecklistResponse> complete(@PathVariable Long id,
                                                       @RequestParam Long userId) {
        return ResponseEntity.ok(checklistService.completeChecklist(id, userId));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        checklistService.deleteChecklist(id);
        return ResponseEntity.noContent().build();
    }
}
