package co.unimagdalena.api.dto;

import co.unimagdalena.domine.entities.PaymentMethod;
import co.unimagdalena.domine.entities.PaymentStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

public class PurchaseDto {

    public record PurchaseCreateRequest(
            @NotNull Long userId,
            @NotNull PaymentMethod paymentMethod,
            @NotEmpty List<TicketRequest> tickets
    ) implements Serializable {
        public record TicketRequest(
                @NotNull Long tripId,
                @NotNull Long passengerId,
                @NotNull Long seatId,
                @NotBlank String seatNumber,
                @NotNull Long fromStopId,
                @NotNull Long toStopId,
                BaggageRequest baggage)
                implements Serializable {}
        public record BaggageRequest(
                @NotNull @Positive Double weightKg,
                String tagCode
        ) {}
    }

    public record PurchaseUpdateRequest(
            PaymentStatus paymentStatus
    ) implements Serializable {}

    public record PurchaseResponse(
            Long id,
            BigDecimal totalAmount,
            PaymentMethod paymentMethod,
            PaymentStatus paymentStatus,
            OffsetDateTime createdAt,
            UserDto.UserSummary user,
            List<TicketDto.TicketSummary> tickets
    ) implements Serializable {}
}
