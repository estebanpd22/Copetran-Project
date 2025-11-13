package co.unimagdalena.services;

import co.unimagdalena.api.dto.AssignmentDto.*;

import java.time.OffsetDateTime;
import java.util.List;

public interface AssignmentService {

    AssignmentResponse assignDriverToTrip(Long tripId, Long driverId);
    AssignmentResponse assignBusToTrip(Long tripId, Long busId);

    boolean checkDriverConflict(Long driverId, OffsetDateTime startTime, OffsetDateTime endTime);
    void updateAssignment(Long assignmentId, AssignmentUpdateRequest request);
    void deleteAssignment(Long assignmentId);

    AssignmentResponse getAssignment(Long assignmentId);
    AssignmentResponse getAssignmentByTripId(Long tripId);
    List<AssignmentResponse> getAssignmentByDriverId(Long driverId);
}