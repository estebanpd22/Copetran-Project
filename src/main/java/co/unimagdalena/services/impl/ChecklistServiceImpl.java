package co.unimagdalena.services.impl;

import co.unimagdalena.api.dto.ChecklistDto.*;
import co.unimagdalena.domine.entities.Checklist;
import co.unimagdalena.domine.entities.Trip;
import co.unimagdalena.domine.entities.User;
import co.unimagdalena.domine.repositories.ChecklistRepository;
import co.unimagdalena.domine.repositories.TripRepository;
import co.unimagdalena.domine.repositories.UserRepository;
import co.unimagdalena.exception.NotFoundException;
import co.unimagdalena.services.ChecklistService;
import co.unimagdalena.services.mapper.ChecklistMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ChecklistServiceImpl implements ChecklistService {

    private final ChecklistRepository checklistRepository;
    private final TripRepository tripRepository;
    private final UserRepository userRepository;
    private final ChecklistMapper checklistMapper;

    @Override
    public ChecklistResponse createChecklist(ChecklistCreateRequest request) {
        log.info("Creating checklist for trip: {}", request.tripId());

        Trip trip = tripRepository.findById(request.tripId())
                .orElseThrow(() -> new NotFoundException("Trip not found with id: " + request.tripId()));

        if (checklistRepository.existsByTripId(request.tripId())) {
            throw new IllegalStateException("Checklist already exists for trip: " + request.tripId());
        }

        Checklist checklist = checklistMapper.toEntity(request);
        checklist.setTrip(trip);
        
        // Set default values for unchecked items
        if (checklist.getFuelCheck() == null) checklist.setFuelCheck(false);
        if (checklist.getTireCheck() == null) checklist.setTireCheck(false);
        if (checklist.getBrakeCheck() == null) checklist.setBrakeCheck(false);
        if (checklist.getLightsCheck() == null) checklist.setLightsCheck(false);
        if (checklist.getEmergencyEquipmentCheck() == null) checklist.setEmergencyEquipmentCheck(false);
        if (checklist.getDocumentsCheck() == null) checklist.setDocumentsCheck(false);
        if (checklist.getCleanlinessCheck() == null) checklist.setCleanlinessCheck(false);
        if (checklist.getSeatsCheck() == null) checklist.setSeatsCheck(false);

        Checklist savedChecklist = checklistRepository.save(checklist);
        log.info("Checklist created with id: {}", savedChecklist.getId());

        return checklistMapper.toResponse(savedChecklist);
    }

    @Override
    public ChecklistResponse updateChecklist(Long checklistId, ChecklistUpdateRequest request) {
        log.info("Updating checklist: {}", checklistId);

        Checklist checklist = checklistRepository.findById(checklistId)
                .orElseThrow(() -> new NotFoundException("Checklist not found with id: " + checklistId));

        if (Boolean.TRUE.equals(checklist.getCompleted())) {
            throw new IllegalStateException("Cannot update a completed checklist");
        }

        checklistMapper.updateEntity(request, checklist);

        Checklist updatedChecklist = checklistRepository.save(checklist);
        log.info("Checklist updated: {}", checklistId);

        return checklistMapper.toResponse(updatedChecklist);
    }

    @Override
    @Transactional(readOnly = true)
    public ChecklistResponse getChecklistById(Long checklistId) {
        log.info("Fetching checklist: {}", checklistId);

        Checklist checklist = checklistRepository.findById(checklistId)
                .orElseThrow(() -> new NotFoundException("Checklist not found with id: " + checklistId));

        return checklistMapper.toResponse(checklist);
    }

    @Override
    @Transactional(readOnly = true)
    public ChecklistResponse getChecklistByTripId(Long tripId) {
        log.info("Fetching checklist for trip: {}", tripId);

        Checklist checklist = checklistRepository.findByTripId(tripId)
                .orElseThrow(() -> new NotFoundException("Checklist not found for trip: " + tripId));

        return checklistMapper.toResponse(checklist);
    }

    @Override
    public void deleteChecklist(Long checklistId) {
        log.info("Deleting checklist: {}", checklistId);

        if (!checklistRepository.existsById(checklistId)) {
            throw new NotFoundException("Checklist not found with id: " + checklistId);
        }

        checklistRepository.deleteById(checklistId);
        log.info("Checklist deleted: {}", checklistId);
    }

    @Override
    public ChecklistResponse completeChecklist(Long checklistId, Long userId) {
        log.info("Completing checklist: {} by user: {}", checklistId, userId);

        Checklist checklist = checklistRepository.findById(checklistId)
                .orElseThrow(() -> new NotFoundException("Checklist not found with id: " + checklistId));

        if (Boolean.TRUE.equals(checklist.getCompleted())) {
            throw new IllegalStateException("Checklist already completed");
        }

        if (!checklist.isAllChecksComplete()) {
            throw new IllegalStateException("All checks must be completed before marking checklist as complete");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found with id: " + userId));

        checklist.setCompleted(true);
        checklist.setCompletedBy(user);
        checklist.setCompletedAt(OffsetDateTime.now());

        Checklist savedChecklist = checklistRepository.save(checklist);
        log.info("Checklist completed: {}", checklistId);

        return checklistMapper.toResponse(savedChecklist);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isChecklistComplete(Long tripId) {
        return checklistRepository.findByTripId(tripId)
                .map(checklist -> Boolean.TRUE.equals(checklist.getCompleted()) && checklist.isAllChecksComplete())
                .orElse(false);
    }
}
