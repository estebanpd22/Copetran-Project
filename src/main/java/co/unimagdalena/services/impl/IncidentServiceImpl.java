package co.unimagdalena.services.impl;

import co.unimagdalena.api.dto.IncidentDto.*;
import co.unimagdalena.domine.entities.*;
import co.unimagdalena.domine.repositories.*;
import co.unimagdalena.exception.NotFoundException;
import co.unimagdalena.services.IncidentService;
import co.unimagdalena.services.mapper.IncidentMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class IncidentServiceImpl implements IncidentService {

    private final IncidentRepository incidentRepository;
    private final TripRepository tripRepository;
    private final ParcelRepository parcelRepository;
    private final TicketRepository ticketRepository;
    private final IncidentMapper mapper;

    @Override
    public IncidentResponse createIncident(IncidentCreateRequest request) {

        // Validación de entidad según el enum TRIP, PARCEL, TICKET
        validateEntityExists(request.entityType(), request.entityId());

        Incident incident = mapper.toEntity(request);
        incident.setCreatedAt(LocalTime.now());

        incidentRepository.save(incident);

        return mapper.toResponse(incident);
    }

    @Override
    public void updateIncident(Long incidentId, IncidentUpdateRequest request) {
        Incident incident = incidentRepository.findById(incidentId)
                .orElseThrow(() -> new NotFoundException(
                        "Incidente con ID " + incidentId + " no existe"
                ));

        mapper.updateEntity(request, incident);
        incidentRepository.save(incident);
    }

    @Override
    public void deleteIncident(Long incidentId) {
        Incident incident = incidentRepository.findById(incidentId)
                .orElseThrow(() -> new NotFoundException(
                        "Incidente con ID " + incidentId + " no existe"
                ));

        incidentRepository.delete(incident);
    }

    @Override
    public void resolveIncident(Long incidentId) {
        // No tienes estado (open/resolved), así que "resolver" = borrar
        Incident incident = incidentRepository.findById(incidentId)
                .orElseThrow(() -> new NotFoundException(
                        "Incidente con ID " + incidentId + " no existe"
                ));

        incidentRepository.delete(incident);
    }

    @Override
    @Transactional(readOnly = true)
    public long getTotalDelayMinutes(Long tripId) {

        // Como no tienes delayMinutes, devolvemos cuántos incidentes DELAY existen para ese trip
        return incidentRepository.findIncidentByEntityId(tripId)
                .filter(i -> i.getIncidentType() == IncidentType.DELIVERY_FAIL)
                .map(i -> 1L)
                .orElse(0L);
    }

    @Override
    @Transactional(readOnly = true)
    public List<IncidentResponse> getActiveIncidentsByTrip(Long tripId) {

        // "Activos" = todos (no existe campo de estado)
        return incidentRepository.findIncidentByEntityId(tripId)
                .stream()
                .filter(i -> i.getEntityType() == EntityType.TRIP)
                .map(mapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<IncidentResponse> getOpenIncidentsByRoute(Long routeId) {
        throw new UnsupportedOperationException(
                "EntityType ROUTE no existe en el modelo actual"
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<IncidentResponse> findByIncidentByEntityType(EntityType type) {
        return incidentRepository.findByEntityType(type)
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<IncidentResponse> findIncidentsRecentByType(IncidentType type) {
        return incidentRepository.findRecentByType(type)
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    private void validateEntityExists(EntityType type, Long entityId) {
        switch (type) {

            case TRIP -> tripRepository.findById(entityId).orElseThrow(() ->
                    new NotFoundException("Trip con ID " + entityId + " no existe"));

            case PARCEL -> parcelRepository.findById(entityId).orElseThrow(() ->
                    new NotFoundException("Parcel con ID " + entityId + " no existe"));

            case TICKET -> ticketRepository.findById(entityId).orElseThrow(() ->
                    new NotFoundException("Ticket con ID " + entityId + " no existe"));

            default -> throw new IllegalArgumentException("EntityType no válido: " + type);
        }
    }
}

