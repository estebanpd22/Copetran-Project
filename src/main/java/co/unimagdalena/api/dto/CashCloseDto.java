package co.unimagdalena.api.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

public class CashCloseDto {

    public record CashCloseRequest(
            @NotNull(message = "User ID is required")
            Long userId,
            
            Long tripId,
            
            @NotNull(message = "Expected amount is required")
            @Positive(message = "Expected amount must be positive")
            BigDecimal expectedAmount,
            
            @NotNull(message = "Actual amount is required")
            @Positive(message = "Actual amount must be positive")
            BigDecimal actualAmount,
            
            @NotNull(message = "Cash sales is required")
            BigDecimal cashSales,
            
            @NotNull(message = "Card sales is required")
            BigDecimal cardSales,
            
            @NotNull(message = "Transfer sales is required")
            BigDecimal transferSales,
            
            String notes
    ) {}

    public record CashCloseResponse(
            Long id,
            Long userId,
            String userName,
            Long tripId,
            String tripRoute,
            BigDecimal expectedAmount,
            BigDecimal actualAmount,
            BigDecimal discrepancy,
            BigDecimal cashSales,
            BigDecimal cardSales,
            BigDecimal transferSales,
            OffsetDateTime closedAt,
            String status
    ) {}
}
