package co.unimagdalena.services;

import co.unimagdalena.api.dto.IncidentDto.*;
import co.unimagdalena.domine.entities.EntityType;
import co.unimagdalena.domine.entities.IncidentType;

import java.util.List;

public interface IncidentService {

    IncidentResponse createIncident(IncidentCreateRequest request);
    void updateIncident(Long incidentId, IncidentUpdateRequest request);
    void deleteIncident(Long incidentId);

    void resolveIncident(Long incidentId);
    long getTotalDelayMinutes(Long tripId);

    List<IncidentResponse> getActiveIncidentsByTrip(Long tripId);
    List<IncidentResponse> getOpenIncidentsByRoute(Long routeId);

    List<IncidentResponse> findByIncidentByEntityType(EntityType type);
    List<IncidentResponse> findIncidentsRecentByType(IncidentType type);
}
