package co.unimagdalena.services.impl;

import co.unimagdalena.api.dto.AssignmentDto.*;
import co.unimagdalena.domine.entities.*;
import co.unimagdalena.domine.repositories.AssignmentRepository;
import co.unimagdalena.domine.repositories.BusRepository;
import co.unimagdalena.domine.repositories.TripRepository;
import co.unimagdalena.domine.repositories.UserRepository;
import co.unimagdalena.exception.NotFoundException;
import co.unimagdalena.services.AssignmentService;
import co.unimagdalena.services.mapper.AssignmentMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class AssignmentServiceImpl implements AssignmentService {

    private final AssignmentRepository assignmentRepository;
    private final TripRepository tripRepository;
    private final UserRepository userRepository;
    private final BusRepository busRepository;
    private final AssignmentMapper mapper;

    // -------------------------------------------------------
    // ASSIGN DRIVER
    // -------------------------------------------------------
    @Override
    public AssignmentResponse assignDriverToTrip(Long tripId, Long driverId) {

        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new NotFoundException("Trip not found"));

        User driver = userRepository.findById(driverId)
                .orElseThrow(() -> new NotFoundException("Driver not found"));

        // Validar que realmente sea un conductor
        if (!driver.getRole().equals(UserRole.DRIVER)) {
            throw new IllegalArgumentException("User is not a driver");
        }

        // Validar conflicto de horarios
        if (checkDriverConflict(driverId, trip.getDepartureAt(), trip.getArrivalAt())) {
            throw new IllegalStateException("Driver has conflicting trip");
        }

        // Ver si ya existe una asignación para este trip
        Assignment assignment = assignmentRepository.findAssignmentByTrip_Id(tripId).orElse(null);

        if (assignment == null) {
            assignment = Assignment.builder()
                    .trip(trip)
                    .driver(driver)
                    .assignedAt(LocalDateTime.now())
                    .dispatcher(null)
                    .checkListOk(false)
                    .build();
        } else {
            assignment.setDriver(driver);
            assignment.setAssignedAt(LocalDateTime.now());
        }

        assignmentRepository.save(assignment);
        return mapper.toResponse(assignment);
    }

    // -------------------------------------------------------
    // ASSIGN BUS
    // -------------------------------------------------------
    @Override
    public AssignmentResponse assignBusToTrip(Long tripId, Long busId) {

        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new NotFoundException("Trip not found"));

        Bus bus = busRepository.findById(busId)
                .orElseThrow(() -> new NotFoundException("Bus not found"));

        // Validar conflicto de horarios del bus
        boolean busConflict = bus.getTrips().stream()
                .anyMatch(t ->
                        t.getDepartureAt().isBefore(trip.getArrivalAt()) &&
                                trip.getDepartureAt().isBefore(t.getArrivalAt())
                );

        if (busConflict) {
            throw new IllegalStateException("Bus has conflicting trip");
        }

        // Asignar el bus al trip
        trip.setBus(bus);
        tripRepository.save(trip);

        // Asignación existente
        Assignment assignment = assignmentRepository.findAssignmentByTrip_Id(tripId).orElse(null);

        if (assignment == null) {
            assignment = Assignment.builder()
                    .trip(trip)
                    .assignedAt(LocalDateTime.now())
                    .dispatcher(null)
                    .driver(null)
                    .checkListOk(false)
                    .build();
        }

        assignmentRepository.save(assignment);
        return mapper.toResponse(assignment);
    }

    // -------------------------------------------------------
    // DRIVER CONFLICT VALIDATION
    // -------------------------------------------------------
    @Override
    @Transactional(readOnly = true)
    public boolean checkDriverConflict(Long driverId,
                                       OffsetDateTime start,
                                       OffsetDateTime end) {

        return assignmentRepository.findByDriver(driverId)
                .stream()
                .anyMatch(a ->
                        a.getTrip().getDepartureAt().isBefore(end) &&
                                start.isBefore(a.getTrip().getArrivalAt())
                );
    }

    // -------------------------------------------------------
    // UPDATE ASSIGNMENT
    // -------------------------------------------------------
    @Override
    public void updateAssignment(Long assignmentId, AssignmentUpdateRequest request) {

        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new NotFoundException("Assignment not found"));

        mapper.updateEntity(request, assignment);

        assignmentRepository.save(assignment);
    }

    // -------------------------------------------------------
    // DELETE
    // -------------------------------------------------------
    @Override
    public void deleteAssignment(Long assignmentId) {
        if (!assignmentRepository.existsById(assignmentId)) {
            throw new NotFoundException("Assignment not found");
        }
        assignmentRepository.deleteById(assignmentId);
    }

    // -------------------------------------------------------
    // GET BY ID
    // -------------------------------------------------------
    @Override
    @Transactional(readOnly = true)
    public AssignmentResponse getAssignment(Long assignmentId) {
        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new NotFoundException("Assignment not found"));
        return mapper.toResponse(assignment);
    }

    // -------------------------------------------------------
    // GET BY TRIP
    // -------------------------------------------------------
    @Override
    @Transactional(readOnly = true)
    public AssignmentResponse getAssignmentByTripId(Long tripId) {
        Assignment assignment = assignmentRepository.findAssignmentByTrip_Id(tripId)
                .orElseThrow(() -> new NotFoundException("Assignment not found for trip"));
        return mapper.toResponse(assignment);
    }

    // -------------------------------------------------------
    // GET BY DRIVER
    // -------------------------------------------------------
    @Override
    @Transactional(readOnly = true)
    public List<AssignmentResponse> getAssignmentByDriverId(Long driverId) {
        return assignmentRepository.findByDriver(driverId)
                .stream()
                .map(mapper::toResponse)
                .toList();
    }
}
