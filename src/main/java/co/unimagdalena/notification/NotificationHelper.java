package co.unimagdalena.notification;

import co.unimagdalena.domine.entities.Purchase;
import co.unimagdalena.domine.entities.Ticket;
import co.unimagdalena.domine.entities.Trip;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationHelper {

    private final NotificationFactory notificationFactory;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    public void sendPurchaseConfirmation(Purchase purchase, NotificationType type) {
        try {
            if (purchase.getTickets().isEmpty()) {
                log.warn("Purchase {} has no tickets, skipping notification", purchase.getId());
                return;
            }

            if (purchase.getUser().getPhone() == null || purchase.getUser().getPhone().isBlank()) {
                log.warn("User {} has no phone number, skipping notification", purchase.getUser().getId());
                return;
            }

            Ticket firstTicket = purchase.getTickets().get(0);
            Trip trip = firstTicket.getTrip();

            if (trip == null || trip.getRoute() == null) {
                log.error("Invalid trip data for purchase {}", purchase.getId());
                return;
            }

            String seats = purchase.getTickets().stream()
                    .map(Ticket::getSeatNumber)
                    .filter(seat -> seat != null && !seat.isBlank())
                    .collect(Collectors.joining(", "));

            String message = NotificationTemplate.PURCHASE_CONFIRMED.format(
                    trip.getRoute().getOrigin(),
                    trip.getRoute().getDestination(),
                    trip.getDate().format(DATE_FORMATTER),
                    trip.getDepartureAt().format(TIME_FORMATTER),
                    seats,
                    purchase.getTotalAmount(),
                    "PUR-" + purchase.getId()
            );

            NotificationRequest request = new NotificationRequest(
                    purchase.getUser().getPhone(),
                    message,
                    type
            );

            notificationFactory.send(request);
            log.info("Purchase confirmation sent to user {}", purchase.getUser().getId());

        } catch (Exception e) {
            log.error("Failed to send purchase confirmation for purchase {}: {}",
                    purchase.getId(), e.getMessage(), e);
        }
    }

    public void sendPlatformChange(Trip trip, String newPlatform, List<String> phones, NotificationType type) {
        try {
            String message = NotificationTemplate.PLATFORM_CHANGE.format(
                    trip.getRoute().getOrigin(),
                    trip.getRoute().getDestination(),
                    trip.getDate().format(DATE_FORMATTER),
                    trip.getDepartureAt().format(TIME_FORMATTER),
                    newPlatform
            );

            for (String phone : phones) {
                try {
                    if (phone != null && !phone.isBlank()) {
                        NotificationRequest request = new NotificationRequest(phone, message, type);
                        notificationFactory.send(request);
                        log.info("Platform change notification sent to {}", phone);
                    }
                } catch (Exception e) {
                    log.error("Failed to send platform change notification to {}: {}",
                            phone, e.getMessage());
                }
            }
        } catch (Exception e) {
            log.error("Failed to send platform change notifications: {}", e.getMessage(), e);
        }
    }

    public void sendArrivalSoon(Ticket ticket, String platform, NotificationType type) {
        try {
            if (ticket == null || ticket.getTrip() == null) {
                log.warn("Invalid ticket data for arrival notification");
                return;
            }

            Trip trip = ticket.getTrip();
            String phone = ticket.getPurchase() != null && ticket.getPurchase().getUser() != null
                    ? ticket.getPurchase().getUser().getPhone()
                    : null;

            if (phone == null || phone.isBlank()) {
                log.warn("No phone number for ticket {}", ticket.getId());
                return;
            }

            String message = NotificationTemplate.ARRIVAL_SOON.format(
                    trip.getRoute().getDestination(),
                    trip.getArrivalAt().format(TIME_FORMATTER),
                    platform,
                    ticket.getSeatNumber()
            );

            NotificationRequest request = new NotificationRequest(phone, message, type);
            notificationFactory.send(request);
            log.info("Arrival soon notification sent for ticket {}", ticket.getId());

        } catch (Exception e) {
            log.error("Failed to send arrival soon notification for ticket {}: {}",
                    ticket.getId(), e.getMessage(), e);
        }
    }

    public void sendTicketCancellation(Ticket ticket, String reason, NotificationType type) {
        try {
            if (ticket == null || ticket.getPassenger() == null) {
                log.warn("Invalid ticket data for cancellation notification");
                return;
            }

            String phone = ticket.getPassenger().getPhoneNumber();
            if (phone == null || phone.isBlank()) {
                log.warn("No phone number for ticket cancellation {}", ticket.getId());
                return;
            }

            Trip trip = ticket.getTrip();
            String message = NotificationTemplate.TICKET_CANCELLED.format(
                    trip != null ? trip.getRoute().getOrigin() : "N/A",
                    trip != null ? trip.getRoute().getDestination() : "N/A",
                    trip != null ? trip.getDate().format(DATE_FORMATTER) : "N/A",
                    ticket.getSeatNumber(),
                    reason != null ? reason : "Cancelación solicitada"
            );

            NotificationRequest request = new NotificationRequest(phone, message, type);
            notificationFactory.send(request);
            log.info("Ticket cancellation notification sent for ticket {}", ticket.getId());

        } catch (Exception e) {
            log.error("Failed to send ticket cancellation notification for ticket {}: {}",
                    ticket.getId(), e.getMessage(), e);
        }
    }
}