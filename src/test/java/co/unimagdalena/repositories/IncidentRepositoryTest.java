package co.unimagdalena.repositories;

import co.unimagdalena.domine.entities.EntityType;
import co.unimagdalena.domine.entities.Incident;
import co.unimagdalena.domine.entities.IncidentType;
import co.unimagdalena.domine.repositories.IncidentRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

public class IncidentRepositoryTest extends AbstractRepositoryTI {
    @Autowired
    private IncidentRepository incidentRepository;

    private Incident createIncident(EntityType entityType, Long entityId, IncidentType incidentType, String note, LocalTime createdAt) {
        return incidentRepository.save(Incident.builder()
                .entityType(entityType)
                .entityId(entityId)
                .incidentType(incidentType)
                .note(note)
                .createdAt(createdAt)
                .build());
    }

    @Test
    @DisplayName("Debe encontrar incidentes por tipo de entidad")
    void shouldFindByEntityType() {
        // Given
        createIncident(EntityType.TRIP, 1L, IncidentType.VEHICLE, "Problema mecánico", LocalTime.now());
        createIncident(EntityType.TRIP, 2L, IncidentType.OVERBOOK, "Sobreventa de asientos", LocalTime.now());
        createIncident(EntityType.PARCEL, 3L, IncidentType.DELIVERY_FAIL, "No se pudo entregar", LocalTime.now());

        // When
        List<Incident> tripIncidents = incidentRepository.findByEntityType(EntityType.TRIP);

        // Then
        assertThat(tripIncidents).hasSize(2);
        assertThat(tripIncidents).allMatch(i -> i.getEntityType() == EntityType.TRIP);
    }

    @Test
    @DisplayName("Debe encontrar un incidente por ID")
    void shouldFindIncidentById() {
        // Given
        Incident incident = createIncident(EntityType.TICKET, 10L, IncidentType.SECURITY, "Incidente de seguridad", LocalTime.of(14, 30));

        // When
        Optional<Incident> found = incidentRepository.findIncidentById(incident.getId());

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(incident.getId());
        assertThat(found.get().getEntityType()).isEqualTo(EntityType.TICKET);
        assertThat(found.get().getEntityId()).isEqualTo(10L);
        assertThat(found.get().getIncidentType()).isEqualTo(IncidentType.SECURITY);
        assertThat(found.get().getNote()).isEqualTo("Incidente de seguridad");
        assertThat(found.get().getCreatedAt()).isEqualTo(LocalTime.of(14, 30));
    }

    @Test
    @DisplayName("Debe encontrar un incidente por entityId")
    void shouldFindIncidentByEntityId() {
        // Given
        createIncident(EntityType.PARCEL, 25L, IncidentType.DELIVERY_FAIL, "Entrega fallida", LocalTime.now());

        // When
        Optional<Incident> found = incidentRepository.findIncidentByEntityId(25L);

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getEntityId()).isEqualTo(25L);
        assertThat(found.get().getIncidentType()).isEqualTo(IncidentType.DELIVERY_FAIL);
        assertThat(found.get().getNote()).isEqualTo("Entrega fallida");
    }

    @Test
    @DisplayName("Debe encontrar incidentes recientes por tipo ordenados por fecha de creación")
    void shouldFindRecentByType() {
        // Given
        createIncident(EntityType.TRIP, 1L, IncidentType.VEHICLE, "Falla 1", LocalTime.of(10, 0));
        createIncident(EntityType.TRIP, 2L, IncidentType.VEHICLE, "Falla 2", LocalTime.of(12, 0));
        createIncident(EntityType.TRIP, 3L, IncidentType.SECURITY, "Seguridad 1", LocalTime.of(11, 0));

        // When
        List<Incident> vehicleIncidents = incidentRepository.findRecentByType(IncidentType.VEHICLE);

        // Then
        assertThat(vehicleIncidents).hasSize(2);
        assertThat(vehicleIncidents).allMatch(i -> i.getIncidentType() == IncidentType.VEHICLE);
        // Verificar que están ordenados por createdAt DESC
        assertThat(vehicleIncidents.get(0).getCreatedAt()).isAfter(vehicleIncidents.get(1).getCreatedAt());
    }

    @Test
    @DisplayName("Debe retornar empty cuando no existe el incidente")
    void shouldReturnEmptyWhenIncidentNotFound() {
        // When
        Optional<Incident> found = incidentRepository.findIncidentById(999L);

        // Then
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("Debe retornar lista vacía cuando no hay incidentes del tipo especificado")
    void shouldReturnEmptyListWhenNoIncidentsOfType() {
        // Given
        createIncident(EntityType.TRIP, 1L, IncidentType.VEHICLE, "Problema mecánico", LocalTime.now());

        // When
        List<Incident> securityIncidents = incidentRepository.findRecentByType(IncidentType.SECURITY);

        // Then
        assertThat(securityIncidents).isEmpty();
    }
}