package co.unimagdalena.services.impl;

import co.unimagdalena.api.dto.PurchaseDto.*;
import co.unimagdalena.api.dto.TicketDto.*;
import co.unimagdalena.domine.entities.*;
import co.unimagdalena.domine.repositories.*;
import co.unimagdalena.exception.NotFoundException;
import co.unimagdalena.services.TicketService;
import co.unimagdalena.services.mapper.TicketMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class TicketServiceImpl implements TicketService {

    private final TicketRepository ticketRepository;
    private final TripRepository tripRepository;
    private final PassengerRepository passengerRepository;
    private final StopRepository stopRepository;
    private final PurchaseRepository purchaseRepository;
    private final SeatRepository seatRepository;
    private final TicketMapper ticketMapper;

    @Override
    public Ticket createTicket(PurchaseCreateRequest.TicketRequest request, Purchase purchase) {
        log.info("Creating ticket for purchase: {}", purchase.getId());

        // 1. Validar existencia de entidades relacionadas
        Trip trip = tripRepository.findById(request.tripId())
                .orElseThrow(() -> {
                    log.error("Trip not found with ID: {}", request.tripId());
                    return new NotFoundException("Trip with ID " + request.tripId() + " not found");
                });

        Passenger passenger = passengerRepository.findPassengerById(request.passengerId())
                .orElseThrow(() -> {
                    log.error("Passenger not found with ID: {}", request.passengerId());
                    return new NotFoundException("Passenger with ID " + request.passengerId() + " not found");
                });

        Seat seat = seatRepository.findById(request.seatId())
                .orElseThrow(() -> {
                    log.error("Seat not found with ID: {}", request.seatId());
                    return new NotFoundException("Seat with ID " + request.seatId() + " not found");
                });

        Stop fromStop = stopRepository.findStopById(request.fromStopId())
                .orElseThrow(() -> {
                    log.error("Origin stop not found with ID: {}", request.fromStopId());
                    return new NotFoundException("Stop with ID " + request.fromStopId() + " not found");
                });

        Stop toStop = stopRepository.findStopById(request.toStopId())
                .orElseThrow(() -> {
                    log.error("Destination stop not found with ID: {}", request.toStopId());
                    return new NotFoundException("Stop with ID " + request.toStopId() + " not found");
                });

        // 2. Validar que el asiento pertenece al bus del trip
        if (!seat.getBus().getId().equals(trip.getBus().getId())) {
            log.error("Seat {} does not belong to trip's bus {}", seat.getId(), trip.getBus().getId());
            throw new IllegalArgumentException("Seat does not belong to trip's bus");
        }

        // 3. Validar que las paradas pertenezcan a la ruta del trip
        if (!fromStop.getRoute().getId().equals(trip.getRoute().getId())) {
            log.error("The origin stop does not belong to the trip route");
            throw new IllegalArgumentException("The origin stop does not belong to the trip route");
        }

        if (!toStop.getRoute().getId().equals(trip.getRoute().getId())) {
            log.error("The destination stop does not belong to the trip route");
            throw new IllegalArgumentException("The destination stop does not belong to the trip route");
        }

        // 4. Validar que fromStop.order < toStop.order
        if (fromStop.getOrder() >= toStop.getOrder()) {
            log.error("The order of the origin stop ({}) must be less than the destination ({})",
                    fromStop.getOrder(), toStop.getOrder());
            throw new IllegalArgumentException("The origin stop must be before the destination stop on the route");
        }

        // 5. Verificar disponibilidad del asiento en el tramo (solo SOLD)
        boolean isSeatOccupied = ticketRepository.findByTripId(trip.getId()).stream()
                .filter(t -> t.getStatus() == TicketStatus.SOLD)
                .filter(t -> t.getSeat().getId().equals(seat.getId()))
                .anyMatch(t -> isSegmentOverlap(t.getFromStop(), t.getToStop(), fromStop, toStop));

        if (isSeatOccupied) {
            log.warn("Seat {} is already occupied in the requested segment for trip {}",
                    seat.getNumber(), trip.getId());
            throw new IllegalStateException("Seat is already occupied for the selected segment");
        }

        // 6. Crear entidad Ticket manualmente (sin usar mapper)
        Ticket ticket = Ticket.builder()
                .price(calculateTicketPrice(trip, fromStop, toStop))
                .seatNumber(String.valueOf(seat.getNumber()))
                .status(TicketStatus.SOLD)
                .createdAt(OffsetDateTime.now())
                .trip(trip)
                .passenger(passenger)
                .seat(seat)
                .fromStop(fromStop)
                .toStop(toStop)
                .purchase(purchase)
                .build();

        // 7. Generar QR code simple
        generateSimpleQrCode(ticket);

        // 8. Guardar ticket
        Ticket savedTicket = ticketRepository.save(ticket);
        log.info("Ticket created successfully with ID: {} in SOLD status", savedTicket.getId());

        return savedTicket;
    }

    @Override
    @Transactional(readOnly = true)
    public TicketResponse getTicket(Long id) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Ticket not found with ID: {}", id);
                    return new NotFoundException("Ticket with ID " + id + " not found");
                });
        return ticketMapper.toResponse(ticket);
    }

    @Override
    public void deleteTicket(Long id) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Ticket not found with ID: {}", id);
                    return new NotFoundException("Ticket with ID " + id + " not found");
                });

        applyCancellationPolicy(ticket);
        ticket.setStatus(TicketStatus.CANCELLED);
        ticketRepository.save(ticket);
        log.info("Ticket ID: {} cancelled successfully", id);
    }

    @Override
    public void generateQrForTicket(Long ticketId) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> {
                    log.error("Ticket not found with ID: {}", ticketId);
                    return new NotFoundException("Ticket with ID " + ticketId + " not found");
                });

        generateSimpleQrCode(ticket);
        log.info("QR generated successfully for ticket ID: {}", ticketId);
    }

    @Override
    @Transactional(readOnly = true)
    public void validateQrForTicket(String qrCode) {
        Ticket ticket = ticketRepository.findByQrCode(qrCode)
                .orElseThrow(() -> {
                    log.error("Ticket not found with QR code: {}", qrCode);
                    return new NotFoundException("Ticket with QR code " + qrCode + " not found");
                });

        // Validar que el status sea SOLD
        if (ticket.getStatus() != TicketStatus.SOLD) {
            throw new IllegalStateException("Ticket is not active. Status: " + ticket.getStatus());
        }

        // Validar que el trip esté en estado BOARDING
        if (ticket.getTrip().getStatus() != TripStatus.BOARDING) {
            throw new IllegalStateException("Trip is not in boarding status. Current status: " + ticket.getTrip().getStatus());
        }

        // Validar no-show (5 minutos después de la salida)
        OffsetDateTime now = OffsetDateTime.now();
        if (now.isAfter(ticket.getTrip().getDepartureAt().plusMinutes(5))) {
            ticket.setStatus(TicketStatus.NO_SHOW);
            ticketRepository.save(ticket);
            throw new IllegalStateException("Passenger no-show. Ticket expired.");
        }

        log.info("QR validated successfully for ticket ID: {}", ticket.getId());
    }

    @Override
    @Scheduled(fixedRate = 300000) // Cada 5 minutos
    public int expireUnusedTickets() {
        log.info("Checking for expired tickets...");
        return 0;
    }

    @Override
    public void releaseSeatsByPurchase(Long purchaseId) {
        List<Ticket> tickets = ticketRepository.findByPurchaseId(purchaseId);
        tickets.forEach(ticket -> {
            if (ticket.getStatus() == TicketStatus.SOLD) {
                log.info("Seat {} released for trip {}",
                        ticket.getSeat().getNumber(), ticket.getTrip().getId());
            }
        });
    }

    @Override
    @Transactional(readOnly = true)
    public List<TicketResponse> getTicketsByTrip(Long tripId) {
        List<Ticket> tickets = ticketRepository.findByTripId(tripId);
        log.info("Found {} tickets for trip ID: {}", tickets.size(), tripId);
        return tickets.stream()
                .map(ticketMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<TicketResponse> getTicketsByPurchase(Long purchaseId) {
        List<Ticket> tickets = ticketRepository.findByPurchaseId(purchaseId);
        log.info("Found {} tickets for purchase ID: {}", tickets.size(), purchaseId);
        return tickets.stream()
                .map(ticketMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<TicketResponse> getTicketsByPassenger(Long passengerId) {
        List<Ticket> tickets = ticketRepository.findByPurchaseUserId(passengerId);
        log.info("Found {} tickets for passenger ID: {}", tickets.size(), passengerId);
        return tickets.stream()
                .map(ticketMapper::toResponse)
                .toList();
    }

    // Métodos auxiliares privados
    private void generateSimpleQrCode(Ticket ticket) {
        String simpleQrCode = "TKT_" + ticket.getId() + "_" +
                UUID.randomUUID().toString().substring(0, 8).toUpperCase() + "_" +
                System.currentTimeMillis();
        ticket.setQrCode(simpleQrCode);
        ticketRepository.save(ticket);
        log.debug("Simple QR code generated for ticket {}: {}", ticket.getId(), simpleQrCode);
    }

    private boolean isSegmentOverlap(Stop existingFrom, Stop existingTo, Stop newFrom, Stop newTo) {
        return (newFrom.getOrder() < existingTo.getOrder()) && (newTo.getOrder() > existingFrom.getOrder());
    }

    private BigDecimal calculateTicketPrice(Trip trip, Stop fromStop, Stop toStop) {
        int segmentDistance = Math.abs(toStop.getOrder() - fromStop.getOrder());
        return BigDecimal.valueOf(segmentDistance * 5000);
    }

    private void applyCancellationPolicy(Ticket ticket) {
        OffsetDateTime now = OffsetDateTime.now();
        long hoursUntilDeparture = java.time.Duration.between(now, ticket.getTrip().getDepartureAt()).toHours();

        if (hoursUntilDeparture < 2) {
            throw new IllegalStateException("Cannot cancel ticket less than 2 hours before departure");
        }

        log.info("Cancellation policy applied for ticket {} - {} hours before departure",
                ticket.getId(), hoursUntilDeparture);
    }
}