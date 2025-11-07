package co.unimagdalena.api.dto;

import jakarta.validation.constraints.NotNull;
import co.unimagdalena.api.dto.StopDto.*;
import co.unimagdalena.api.dto.FareRuleDto.*;
import java.io.Serializable;
import java.util.List;

public class RouteDto {

    public record RouteCreateRequest(
            @NotNull String code,
            @NotNull String name,
            @NotNull String origin,
            @NotNull String destination,
            @NotNull Float distanceKm,
            @NotNull Float durationMin
    ) implements Serializable {}

    public record RouteUpdateRequest(
            String code,
            String name,
            String origin,
            String destination,
            Float distanceKm,
            Float durationMin
    ) implements Serializable {}

    public record RouteResponse(
            Long id,
            String code,
            String name,
            String origin,
            String destination,
            Float distanceKm,
            Float durationMin,
            List<StopResponse> stops,
            List<FareRuleResponse> fareRules
    ) implements Serializable {}

    public record RouteSummary(
            Long id,
            String code,
            String name
    ) implements Serializable {}
}