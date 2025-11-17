package co.unimagdalena.api;

import co.unimagdalena.api.dto.IncidentDto.*;
import co.unimagdalena.domine.entities.EntityType;
import co.unimagdalena.domine.entities.IncidentType;
import co.unimagdalena.services.IncidentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Validated
@RequestMapping("/api/v1/incidents")
public class IncidentController {
    private final IncidentService incidentService;

    @PostMapping
    public ResponseEntity<IncidentResponse> create(@Validated @RequestBody IncidentCreateRequest request,
                                                   UriComponentsBuilder uriBuilder) {
        var incidentCreated = incidentService.createIncident(request);
        var location = uriBuilder.path("/api/v1/incidents/{id}")
                .buildAndExpand(incidentCreated.id())
                .toUri();
        return ResponseEntity.created(location).body(incidentCreated);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Void> update(@PathVariable Long id,
                                       @Validated @RequestBody IncidentUpdateRequest request) {
        incidentService.updateIncident(id, request);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        incidentService.deleteIncident(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/resolve")
    public ResponseEntity<Void> resolve(@PathVariable Long id) {
        incidentService.resolveIncident(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/trip/{tripId}/active")
    public ResponseEntity<List<IncidentResponse>> getActiveByTrip(@PathVariable Long tripId) {
        return ResponseEntity.ok(incidentService.getActiveIncidentsByTrip(tripId));
    }

    @GetMapping("/trip/{tripId}/delay")
    public ResponseEntity<Long> getTripDelay(@PathVariable Long tripId) {
        return ResponseEntity.ok(incidentService.getTotalDelayMinutes(tripId));
    }

    @GetMapping("/entity-type/{type}")
    public ResponseEntity<List<IncidentResponse>> getByEntityType(@PathVariable EntityType type) {
        return ResponseEntity.ok(incidentService.findByIncidentByEntityType(type));
    }

    @GetMapping("/type/{type}/recent")
    public ResponseEntity<List<IncidentResponse>> getRecentByType(@PathVariable IncidentType type) {
        return ResponseEntity.ok(incidentService.findIncidentsRecentByType(type));
    }
}
