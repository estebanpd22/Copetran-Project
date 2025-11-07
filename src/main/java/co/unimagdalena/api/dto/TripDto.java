package co.unimagdalena.api.dto;

import co.unimagdalena.domine.entities.TripStatus;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.OffsetDateTime;

public class TripDto {

    public record TripCreateRequest(
            @NotNull LocalDate date,
            @NotNull OffsetDateTime departureAt,
            @NotNull OffsetDateTime arrivalAt,
            @NotNull TripStatus status,
            @NotNull Long routeId, // ID de la Ruta
            @NotNull Long busId    // ID del Bus
    ) implements Serializable {}

    public record TripUpdateRequest(
            LocalDate date,
            OffsetDateTime departureAt,
            OffsetDateTime arrivalAt,
            TripStatus status,
            Long routeId,
            Long busId
    ) implements Serializable {}

    public record TripResponse(
            Long id,
            LocalDate date,
            OffsetDateTime departureAt,
            OffsetDateTime arrivalAt,
            TripStatus status,
            RouteDto.RouteSummary route,
            BusDto.BusSummary bus
    ) implements Serializable {}

    public record TripSummary(
            Long id,
            LocalDate date,
            OffsetDateTime departureAt,
            OffsetDateTime arrivalAt,
            TripStatus status,
            RouteDto.RouteSummary route
    ) implements Serializable {}
}