package co.unimagdalena.services.impl;

import co.unimagdalena.api.dto.MetricsDto.*;
import co.unimagdalena.domine.entities.*;
import co.unimagdalena.domine.repositories.*;
import co.unimagdalena.services.MetricsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MetricsServiceImpl implements MetricsService {

    private final TripRepository tripRepository;
    private final TicketRepository ticketRepository;
    private final ParcelRepository parcelRepository;
    private final PurchaseRepository purchaseRepository;

    @Override
    public MetricsResponse getMetrics(LocalDate startDate, LocalDate endDate) {
        log.info("Calculating metrics from {} to {}", startDate, endDate);

        // Set default dates if not provided
        if (startDate == null) {
            startDate = LocalDate.now().minusMonths(1);
        }
        if (endDate == null) {
            endDate = LocalDate.now();
        }

        OccupancyMetrics occupancy = calculateOccupancyMetrics(startDate, endDate);
        CancellationMetrics cancellations = calculateCancellationMetrics(startDate, endDate);
        PunctualityMetrics punctuality = calculatePunctualityMetrics(startDate, endDate);
        RevenueMetrics revenue = calculateRevenueMetrics(startDate, endDate);

        return new MetricsResponse(
                occupancy,
                cancellations,
                punctuality,
                revenue,
                startDate,
                endDate
        );
    }

    private OccupancyMetrics calculateOccupancyMetrics(LocalDate startDate, LocalDate endDate) {
        List<Trip> trips = tripRepository.findAll().stream()
                .filter(t -> !t.getDate().isBefore(startDate) && !t.getDate().isAfter(endDate))
                .collect(Collectors.toList());

        int totalTrips = trips.size();
        if (totalTrips == 0) {
            return new OccupancyMetrics(0, 0, 0, 0.0, 0.0, null);
        }

        int totalSeatsAvailable = 0;
        int totalSeatsSold = 0;
        double peakOccupancyRate = 0.0;
        String peakRoute = null;

        // Calculate by route
        Map<String, List<Trip>> tripsByRoute = trips.stream()
                .filter(t -> t.getRoute() != null)
                .collect(Collectors.groupingBy(t -> t.getRoute().getName()));

        for (Map.Entry<String, List<Trip>> entry : tripsByRoute.entrySet()) {
            String routeName = entry.getKey();
            List<Trip> routeTrips = entry.getValue();

            int routeSeatsAvailable = 0;
            int routeSeatsSold = 0;

            for (Trip trip : routeTrips) {
                if (trip.getBus() != null) {
                    int busCapacity = trip.getBus().getCapacity();
                    routeSeatsAvailable += busCapacity;
                    routeSeatsSold += (int) trip.getTickets().stream()
                            .filter(t -> t.getStatus() != TicketStatus.CANCELLED)
                            .count();
                }
            }

            totalSeatsAvailable += routeSeatsAvailable;
            totalSeatsSold += routeSeatsSold;

            if (routeSeatsAvailable > 0) {
                double routeOccupancy = ((double) routeSeatsSold / routeSeatsAvailable) * 100;
                if (routeOccupancy > peakOccupancyRate) {
                    peakOccupancyRate = routeOccupancy;
                    peakRoute = routeName;
                }
            }
        }

        double averageOccupancyRate = totalSeatsAvailable > 0
                ? ((double) totalSeatsSold / totalSeatsAvailable) * 100
                : 0.0;

        return new OccupancyMetrics(
                totalTrips,
                totalSeatsAvailable,
                totalSeatsSold,
                Math.round(averageOccupancyRate * 100.0) / 100.0,
                Math.round(peakOccupancyRate * 100.0) / 100.0,
                peakRoute
        );
    }

    private CancellationMetrics calculateCancellationMetrics(LocalDate startDate, LocalDate endDate) {
        List<Ticket> tickets = ticketRepository.findAll().stream()
                .filter(t -> t.getCreatedAt() != null)
                .filter(t -> !t.getCreatedAt().toLocalDate().isBefore(startDate))
                .filter(t -> !t.getCreatedAt().toLocalDate().isAfter(endDate))
                .collect(Collectors.toList());

        long ticketCancellations = tickets.stream()
                .filter(t -> t.getStatus() == TicketStatus.CANCELLED)
                .count();

        List<Trip> trips = tripRepository.findAll().stream()
                .filter(t -> !t.getDate().isBefore(startDate) && !t.getDate().isAfter(endDate))
                .collect(Collectors.toList());

        long tripCancellations = trips.stream()
                .filter(t -> t.getStatus() == TripStatus.CANCELLED)
                .count();

        int totalCancellations = (int) (ticketCancellations + tripCancellations);

        // Calculate cancellation rate
        int totalTickets = tickets.size();
        double cancellationRate = totalTickets > 0
                ? ((double) ticketCancellations / totalTickets) * 100
                : 0.0;

        // Calculate total refunded (simplified - would need payment data)
        BigDecimal totalRefunded = tickets.stream()
                .filter(t -> t.getStatus() == TicketStatus.CANCELLED)
                .map(Ticket::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new CancellationMetrics(
                totalCancellations,
                (int) ticketCancellations,
                (int) tripCancellations,
                Math.round(cancellationRate * 100.0) / 100.0,
                totalRefunded
        );
    }

    private PunctualityMetrics calculatePunctualityMetrics(LocalDate startDate, LocalDate endDate) {
        List<Trip> completedTrips = tripRepository.findAll().stream()
                .filter(t -> t.getStatus() == TripStatus.ARRIVED)
                .filter(t -> !t.getDate().isBefore(startDate) && !t.getDate().isAfter(endDate))
                .collect(Collectors.toList());

        int totalCompletedTrips = completedTrips.size();
        if (totalCompletedTrips == 0) {
            return new PunctualityMetrics(0, 0, 0, 0.0, 0L);
        }

        long totalDelayMinutes = 0;
        int onTimeTrips = 0;
        int delayedTrips = 0;

        for (Trip trip : completedTrips) {
            if (trip.getActualDepartureAt() != null) {
                Duration delay = Duration.between(trip.getDepartureAt(), trip.getActualDepartureAt());
                long delayMinutes = delay.toMinutes();

                if (delayMinutes <= 10) { // Consider on-time if delayed 10 minutes or less
                    onTimeTrips++;
                } else {
                    delayedTrips++;
                    totalDelayMinutes += delayMinutes;
                }
            } else {
                // Assume on time if actual departure not recorded
                onTimeTrips++;
            }
        }

        double onTimeRate = ((double) onTimeTrips / totalCompletedTrips) * 100;
        long averageDelayMinutes = delayedTrips > 0 ? totalDelayMinutes / delayedTrips : 0;

        return new PunctualityMetrics(
                totalCompletedTrips,
                onTimeTrips,
                delayedTrips,
                Math.round(onTimeRate * 100.0) / 100.0,
                averageDelayMinutes
        );
    }

    private RevenueMetrics calculateRevenueMetrics(LocalDate startDate, LocalDate endDate) {
        List<Ticket> tickets = ticketRepository.findAll().stream()
                .filter(t -> t.getCreatedAt() != null)
                .filter(t -> !t.getCreatedAt().toLocalDate().isBefore(startDate))
                .filter(t -> !t.getCreatedAt().toLocalDate().isAfter(endDate))
                .filter(t -> t.getStatus() != TicketStatus.CANCELLED)
                .collect(Collectors.toList());

        BigDecimal ticketRevenue = tickets.stream()
                .map(Ticket::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<Parcel> parcels = parcelRepository.findAll().stream()
                .filter(p -> p.getCreatedAt() != null)
                .filter(p -> !p.getCreatedAt().toLocalDate().isBefore(startDate))
                .filter(p -> !p.getCreatedAt().toLocalDate().isAfter(endDate))
                .filter(p -> p.getStatus() != ParcelStatus.FAILED)
                .collect(Collectors.toList());

        BigDecimal parcelRevenue = parcels.stream()
                .map(Parcel::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalRevenue = ticketRevenue.add(parcelRevenue);

        int totalTicketsSold = tickets.size();
        BigDecimal averageTicketPrice = totalTicketsSold > 0
                ? ticketRevenue.divide(BigDecimal.valueOf(totalTicketsSold), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        return new RevenueMetrics(
                totalRevenue,
                ticketRevenue,
                parcelRevenue,
                averageTicketPrice,
                totalTicketsSold,
                parcels.size()
        );
    }
}
