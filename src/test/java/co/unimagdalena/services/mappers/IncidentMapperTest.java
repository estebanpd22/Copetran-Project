package co.unimagdalena.services.mappers;

import co.unimagdalena.api.dto.IncidentDto;
import co.unimagdalena.domine.entities.EntityType;
import co.unimagdalena.domine.entities.Incident;
import co.unimagdalena.domine.entities.IncidentType;
import co.unimagdalena.services.mapper.IncidentMapper;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import static org.assertj.core.api.Assertions.assertThat;

public class IncidentMapperTest {
    private final IncidentMapper mapper = Mappers.getMapper(IncidentMapper.class);

    @Test
    void toEntity_shouldMapToCreate() {
        IncidentDto.IncidentCreateRequest request = new IncidentDto.IncidentCreateRequest(
                EntityType.TRIP,
                1L,
                IncidentType.VEHICLE,
                "Engine overheating"
        );

        Incident incident = mapper.toEntity(request);

        assertThat(incident.getEntityType()).isEqualTo(EntityType.TRIP);
        assertThat(incident.getEntityId()).isEqualTo(1L);
        assertThat(incident.getIncidentType()).isEqualTo(IncidentType.VEHICLE);
        assertThat(incident.getNote()).isEqualTo("Engine overheating");
    }

    @Test
    void toResponse_shouldMapEntity() {
        Incident incident = Incident.builder()
                .id(1L)
                .entityType(EntityType.TRIP)
                .entityId(1L)
                .incidentType(IncidentType.VEHICLE)
                .note("Engine overheating")
                .build();

        IncidentDto.IncidentResponse dto = mapper.toResponse(incident);

        assertThat(dto.id()).isEqualTo(1L);
        assertThat(dto.entityType()).isEqualTo(EntityType.TRIP);
        assertThat(dto.entityId()).isEqualTo(1L);
        assertThat(dto.incidentType()).isEqualTo(IncidentType.VEHICLE);
        assertThat(dto.note()).isEqualTo("Engine overheating");
    }

    @Test
    void updateEntity_shouldUpdateFields() {
        Incident incident = Incident.builder()
                .id(1L)
                .entityType(EntityType.PARCEL)
                .entityId(1L)
                .incidentType(IncidentType.VEHICLE)
                .note("Engine overheating")
                .build();

        IncidentDto.IncidentUpdateRequest update = new IncidentDto.IncidentUpdateRequest(
                EntityType.TRIP,
                2L,
                IncidentType.OVERBOOK,
                "We were full"
        );

        mapper.updateEntity(update, incident);

        assertThat(incident.getEntityType()).isEqualTo(EntityType.TRIP);
        assertThat(incident.getEntityId()).isEqualTo(2L);
        assertThat(incident.getIncidentType()).isEqualTo(IncidentType.OVERBOOK);
        assertThat(incident.getNote()).isEqualTo("Traffic delay");
    }
}
