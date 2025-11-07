package co.unimagdalena.api.dto;

import co.unimagdalena.domine.entities.SeatHoldStatus;
import jakarta.validation.constraints.NotNull;
import co.unimagdalena.api.dto.TripDto.*;
import co.unimagdalena.api.dto.UserDto.*;
import java.io.Serializable;
import java.time.OffsetDateTime;

public class SeatHoldDto {
    public record SeatHoldCreateRequest(
            @NotNull String seatNumber,
            OffsetDateTime expiresAt,
            @NotNull SeatHoldStatus status,
            @NotNull Long seatId,
            @NotNull Long tripId,
            @NotNull Long userId
    ) implements Serializable {}

    public record SeatHoldUpdateRequest(
            String seatNumber,
            OffsetDateTime expiresAt,
            SeatHoldStatus status,
            Long seatId,
            Long tripId,
            Long userId
    ) implements Serializable {}

    public record SeatHoldResponse(
            Long id,
            String seatNumber,
            OffsetDateTime expiresAt,
            SeatHoldStatus status,
            SeatDto.SeatSummary seat,
            TripSummary trip,
            UserSummary user
    ) implements Serializable {}
}
