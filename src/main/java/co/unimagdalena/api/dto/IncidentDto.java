package co.unimagdalena.api.dto;

import co.unimagdalena.domine.entities.EntityType;
import co.unimagdalena.domine.entities.IncidentType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;
import java.time.LocalTime;

public class IncidentDto {

    public record IncidentCreateRequest(
            @NotNull EntityType entityType,
            @NotNull Long entityId,
            @NotNull IncidentType incidentType,
            @NotBlank String note
    ) implements Serializable {}

    public record IncidentUpdateRequest(
            EntityType entityType,
            Long entityId,
            IncidentType incidentType,
            String note
    ) implements Serializable {}

    public record IncidentResponse(
            Long id,
            EntityType entityType,
            Long entityId,
            IncidentType incidentType,
            String note,
            LocalTime createdAt
    ) implements Serializable {}
}
