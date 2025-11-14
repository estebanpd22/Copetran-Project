package co.unimagdalena.api.dto;

import co.unimagdalena.domine.entities.PaymentMethod;
import co.unimagdalena.domine.entities.TicketStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

public class TicketDto {

    public record TicketCreateRequest(
            @NotNull BigDecimal price,
            @NotNull PaymentMethod paymentMethod,
            @NotBlank String seatNumber,
            @NotNull Long tripId,
            @NotNull Long passengerId,
            @NotNull Long purchaseId,
            @NotNull Long seatId,
            @NotNull Long fromStopId,
            @NotNull Long toStopId
    ) implements Serializable {}

    public record TicketUpdateRequest(
            BigDecimal price,
            PaymentMethod paymentMethod,
            TicketStatus status
    ) implements Serializable {}

    public record TicketResponse(
            Long id,
            BigDecimal price,
            PaymentMethod paymentMethod,
            TicketStatus status,
            OffsetDateTime createdAt,
            String qrCode,
            TripDto.TripSummary trip,
            UserDto.UserSummary passenger,
            SeatDto.SeatSummary seat,
            StopDto.StopSummary fromStop,
            StopDto.StopSummary toStop
    ) implements Serializable {}

    public record TicketSummary(
            Long id,
            BigDecimal price,
            TicketStatus status,
            String qrCode,
            OffsetDateTime createdAt
    ) implements Serializable {}
}
