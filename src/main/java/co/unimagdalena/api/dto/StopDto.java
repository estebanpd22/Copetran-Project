package co.unimagdalena.api.dto;

import jakarta.validation.constraints.NotNull;

import java.io.Serializable;

public class StopDto {
    public record StopCreateRequest(
            @NotNull String name,
            @NotNull Integer order,
            @NotNull Double latitude,
            @NotNull Double longitude,
            @NotNull Long routeId
    ) implements Serializable {}

    public record StopUpdateRequest(
            String name,
            Integer order,
            Double latitude,
            Double longitude,
            Long routeId
    ) implements Serializable {}

    public record StopResponse(
            Long id,
            String name,
            Integer order,
            Double latitude,
            Double longitude
    ) implements Serializable {}

    public record StopSummary(
            Long id,
            String name,
            Integer Order
    ) implements Serializable {}
}
