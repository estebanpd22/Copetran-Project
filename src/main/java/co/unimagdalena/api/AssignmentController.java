package co.unimagdalena.api;

import co.unimagdalena.api.dto.AssignmentDto.*;
import co.unimagdalena.services.AssignmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.List;

@RestController
@RequiredArgsConstructor
@Validated
@RequestMapping("/api/v1/assignments")
public class AssignmentController {
    private final AssignmentService assignmentService;

    @PostMapping("/driver")
    public ResponseEntity<AssignmentResponse> assignDriver(@RequestParam Long tripId,
                                                           @RequestParam Long driverId) {
        return ResponseEntity.ok(assignmentService.assignDriverToTrip(tripId, driverId));
    }

    @PostMapping("/bus")
    public ResponseEntity<AssignmentResponse> assignBus(@RequestParam Long tripId,
                                                        @RequestParam Long busId) {
        return ResponseEntity.ok(assignmentService.assignBusToTrip(tripId, busId));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Void> update(@PathVariable Long id,
                                       @Validated @RequestBody AssignmentUpdateRequest request) {
        assignmentService.updateAssignment(id, request);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        assignmentService.deleteAssignment(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<AssignmentResponse> get(@PathVariable Long id) {
        return ResponseEntity.ok(assignmentService.getAssignment(id));
    }

    @GetMapping("/trip/{tripId}")
    public ResponseEntity<AssignmentResponse> getByTrip(@PathVariable Long tripId) {
        return ResponseEntity.ok(assignmentService.getAssignmentByTripId(tripId));
    }

    @GetMapping("/driver/{driverId}")
    public ResponseEntity<List<AssignmentResponse>> getByDriver(@PathVariable Long driverId) {
        return ResponseEntity.ok(assignmentService.getAssignmentByDriverId(driverId));
    }

    @GetMapping("/check-conflict")
    public ResponseEntity<Boolean> checkDriverConflict(@RequestParam Long driverId,
                                                       @RequestParam OffsetDateTime start,
                                                       @RequestParam OffsetDateTime end) {
        return ResponseEntity.ok(assignmentService.checkDriverConflict(driverId, start, end));
    }
}
