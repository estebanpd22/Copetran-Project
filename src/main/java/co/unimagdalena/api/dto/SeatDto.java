package co.unimagdalena.api.dto;

import co.unimagdalena.domine.entities.SeatStatus;
import co.unimagdalena.domine.entities.SeatType;
import jakarta.validation.constraints.NotNull;
import co.unimagdalena.api.dto.BusDto.*;
import java.io.Serializable;
import java.math.BigDecimal;

public class SeatDto {
    public record SeatCreateRequest(
            @NotNull BigDecimal price,
            @NotNull Integer number,
            @NotNull SeatType type,
            @NotNull SeatStatus status,
            @NotNull Long busId
    ) implements Serializable {}

    public record SeatUpdateRequest(
            BigDecimal price,
            Integer number,
            SeatType type,
            SeatStatus status,
            Long busId
    ) implements Serializable {}

    public record SeatResponse(
            Long id,
            BigDecimal price,
            Integer number,
            SeatType type,
            SeatStatus status,
            BusSummary bus
    ) implements Serializable {}

    public record SeatSummary(
            Long id,
            Integer number,
            SeatType type,
            BigDecimal price,
            SeatStatus status
    ) implements Serializable {}

}

