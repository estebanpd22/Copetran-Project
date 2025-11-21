package co.unimagdalena.api.dto;

import jakarta.validation.constraints.NotNull;
import java.time.OffsetDateTime;

public class ChecklistDto {

    public record ChecklistCreateRequest(
            @NotNull(message = "Trip ID is required")
            Long tripId,
            
            Boolean fuelCheck,
            Boolean tireCheck,
            Boolean brakeCheck,
            Boolean lightsCheck,
            Boolean emergencyEquipmentCheck,
            Boolean documentsCheck,
            Boolean cleanlinessCheck,
            Boolean seatsCheck,
            String notes
    ) {}

    public record ChecklistUpdateRequest(
            Boolean fuelCheck,
            Boolean tireCheck,
            Boolean brakeCheck,
            Boolean lightsCheck,
            Boolean emergencyEquipmentCheck,
            Boolean documentsCheck,
            Boolean cleanlinessCheck,
            Boolean seatsCheck,
            Boolean completed,
            String notes
    ) {}

    public record ChecklistResponse(
            Long id,
            Long tripId,
            Boolean fuelCheck,
            Boolean tireCheck,
            Boolean brakeCheck,
            Boolean lightsCheck,
            Boolean emergencyEquipmentCheck,
            Boolean documentsCheck,
            Boolean cleanlinessCheck,
            Boolean seatsCheck,
            Boolean completed,
            Boolean allChecksComplete,
            Long completedByUserId,
            String completedByUserName,
            OffsetDateTime createdAt,
            OffsetDateTime completedAt,
            String notes
    ) {}
}
