package co.unimagdalena.api.dto;

import org.antlr.v4.runtime.misc.NotNull;
import co.unimagdalena.api.dto.TripDto.*;
import co.unimagdalena.api.dto.UserDto.*;
import java.io.Serializable;
import java.time.LocalDate;

public class AssignmentDto {

    public record AssignmentCreateRequest(
            @NotNull Boolean checkListOk,
            @NotNull LocalDate assignedAt,
            @NotNull Long tripId,
            @NotNull Long driverId,
            @NotNull Long dispatcherId
    ) implements Serializable {}

    public record AssignmentUpdateRequest(
            Boolean checkListOk,
            LocalDate assignedAt,
            Long tripId,
            Long driverId,
            Long dispatcherId
    ) implements Serializable {}

    public record AssignmentResponse(
            Long id,
            Boolean checkListOk,
            LocalDate assignedAt,
            TripSummary trip,
            UserSummary driver,
            UserSummary dispatcher
    ) implements Serializable {}
}
