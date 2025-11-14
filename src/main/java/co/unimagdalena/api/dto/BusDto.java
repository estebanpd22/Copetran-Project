package co.unimagdalena.api.dto;

import co.unimagdalena.api.dto.AmenityDto.*;
import co.unimagdalena.domine.entities.BusStatus;
import co.unimagdalena.api.dto.SeatDto.*;
import co.unimagdalena.api.dto.TripDto.*;
import org.antlr.v4.runtime.misc.NotNull;
import jakarta.validation.constraints.NotBlank;
import java.io.Serializable;
import java.util.List;
import java.util.Set;

public class BusDto {

    public record BusCreateRequest(
            @NotBlank String plate,
            @NotNull Integer capacity,
            @NotNull BusStatus status,
            Set<AmenityCreateRequest> amenities
    ) implements Serializable {}

    public record BusUpdateRequest(
            Integer capacity,
            BusStatus status,
            String plate,
            Set<AmenityUpdateRequest> amenities
    ) implements Serializable {}

    public record BusResponse(
            Long id,
            String plate,
            Integer capacity,
            BusStatus status,
            Set<AmenityResponse> amenities,
            List<TripSummary> trips,
            List<SeatSummary> seats
    ) implements Serializable {}

    public record BusSummary(
            Long id,
            String plate,
            Integer capacity,
            BusStatus status
    ) implements Serializable {}
}
