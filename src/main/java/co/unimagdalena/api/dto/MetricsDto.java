package co.unimagdalena.api.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class MetricsDto {

    public record MetricsResponse(
            // Ocupación
            OccupancyMetrics occupancy,
            
            // Cancelaciones
            CancellationMetrics cancellations,
            
            // Puntualidad
            PunctualityMetrics punctuality,
            
            // Ingresos
            RevenueMetrics revenue,
            
            // Período
            LocalDate startDate,
            LocalDate endDate
    ) {}

    public record OccupancyMetrics(
            Integer totalTrips,
            Integer totalSeatsAvailable,
            Integer totalSeatsSold,
            Double averageOccupancyRate,
            Double peakOccupancyRate,
            String peakRoute
    ) {}

    public record CancellationMetrics(
            Integer totalCancellations,
            Integer ticketCancellations,
            Integer tripCancellations,
            Double cancellationRate,
            BigDecimal totalRefunded
    ) {}

    public record PunctualityMetrics(
            Integer totalCompletedTrips,
            Integer onTimeTrips,
            Integer delayedTrips,
            Double onTimeRate,
            Long averageDelayMinutes
    ) {}

    public record RevenueMetrics(
            BigDecimal totalRevenue,
            BigDecimal ticketRevenue,
            BigDecimal parcelRevenue,
            BigDecimal averageTicketPrice,
            Integer totalTicketsSold,
            Integer totalParcelsSent
    ) {}
}
