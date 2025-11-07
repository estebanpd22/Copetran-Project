package co.unimagdalena.api.dto;

import co.unimagdalena.domine.entities.ParcelStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import co.unimagdalena.api.dto.TripDto.*;
import co.unimagdalena.api.dto.StopDto.*;
import java.io.Serializable;
import java.math.BigDecimal;

public class ParcelDto {

    public record ParcelCreateRequest(
            @NotBlank String senderName,
            @NotBlank String senderPhone,
            @NotBlank String receiverName,
            @NotBlank String receiverPhone,
            @NotNull BigDecimal price,
            @NotNull ParcelStatus status,
            String proofPhotoUrl,
            String deliveryOTP,
            @NotNull Long fromStopId,
            @NotNull Long toStopId,
            Long tripId
    ) implements Serializable {}

    public record ParcelUpdateRequest(
            String senderName,
            String senderPhone,
            String receiverName,
            String receiverPhone,
            BigDecimal price,
            ParcelStatus status,
            String proofPhotoUrl,
            String deliveryOTP,
            Long fromStopId,
            Long toStopId,
            Long tripId
    ) implements Serializable {}

    public record ParcelResponse(
            Long id,
            String code,
            String senderName,
            String senderPhone,
            String receiverName,
            String receiverPhone,
            BigDecimal price,
            ParcelStatus status,
            String proofPhotoUrl,
            String deliveryOTP,
            StopSummary fromStop,
            StopSummary toStop,
            TripSummary trip
    ) implements Serializable {}
}
