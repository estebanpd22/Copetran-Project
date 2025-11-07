package co.unimagdalena.api.dto;

import org.antlr.v4.runtime.misc.NotNull;
import co.unimagdalena.api.dto.TicketDto.*;
import java.io.Serializable;
import java.math.BigDecimal;

public class BaggageDto {
    public record BaggageCreateRequest(
            @NotNull Float weightKg,
            @NotNull BigDecimal fee,
            String tagCode,
            @NotNull Long ticketId
    ) implements Serializable {}

    public record BaggageUpdateRequest(
            Float weightKg,
            BigDecimal fee,
            String tagCode,
            Long ticketId
    ) implements Serializable {}

    public record BaggageResponse(
            Long id,
            Float weightKg,
            BigDecimal fee,
            String tagCode,
            TicketSummary ticket
    ) implements Serializable {}
}

