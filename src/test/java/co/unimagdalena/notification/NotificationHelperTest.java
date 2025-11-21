package co.unimagdalena.notification;

import co.unimagdalena.domine.entities.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationHelperTest {

    @Mock
    private NotificationFactory notificationFactory;

    @InjectMocks
    private NotificationHelper notificationHelper;

    private Route route;
    private Bus bus;
    private Trip trip;
    private User user;
    private Passenger passenger;
    private Stop fromStop;
    private Stop toStop;
    private Purchase purchase;
    private Ticket ticket1;
    private Ticket ticket2;

    @BeforeEach
    void setUp() {
        route = createRoute(1L, "Bogotá", "Medellín");
        bus = createBus(1L, "ABC123", 40);
        trip = createTrip(1L, bus, route);
        user = createUser(1L, "test@example.com", "3001234567");
        passenger = createPassenger(1L, "Juan Pérez", "123456789", "3009876543");
        fromStop = createStop(1L, "Terminal Bogotá", 0, route);
        toStop = createStop(2L, "Terminal Medellín", 5, route);
        purchase = createPurchase(1L, user, BigDecimal.valueOf(150000), PaymentStatus.CONFIRMED);
        ticket1 = createTicket(1L, "1A", trip, passenger, fromStop, toStop, purchase);
        ticket2 = createTicket(2L, "1B", trip, passenger, fromStop, toStop, purchase);

        purchase.getTickets().add(ticket1);
        purchase.getTickets().add(ticket2);
        ticket1.setPurchase(purchase);
        ticket2.setPurchase(purchase);
    }

    @Test
    @DisplayName("Debe enviar notificación de confirmación de compra correctamente")
    void testSendPurchaseConfirmation_Success() {
        // Given
        ArgumentCaptor<NotificationRequest> captor = ArgumentCaptor.forClass(NotificationRequest.class);

        // When
        notificationHelper.sendPurchaseConfirmation(purchase, NotificationType.WHATSAPP);

        // Then
        verify(notificationFactory, times(1)).send(captor.capture());

        NotificationRequest capturedRequest = captor.getValue();
        assertThat(capturedRequest.type()).isEqualTo(NotificationType.WHATSAPP);
        assertThat(capturedRequest.message())
                .contains("Bogotá")
                .contains("Medellín")
                .contains("1A, 1B")
                .contains("PUR-1");
        assertThat(capturedRequest.message())
                .contains("150.000,00");
    }

    @Test
    @DisplayName("Debe enviar notificación de cancelación de ticket correctamente")
    void testSendTicketCancellation_Success() {
        // Given
        String cancellationReason = "Solicitud del usuario";
        ArgumentCaptor<NotificationRequest> captor = ArgumentCaptor.forClass(NotificationRequest.class);

        // When
        notificationHelper.sendTicketCancellation(ticket1, cancellationReason, NotificationType.SMS); // ✅ CAMBIAR método

        // Then
        verify(notificationFactory, times(1)).send(captor.capture());

        NotificationRequest capturedRequest = captor.getValue();
        assertThat(capturedRequest.type()).isEqualTo(NotificationType.SMS);
        assertThat(capturedRequest.message())
                .contains("Ticket Cancelado")
                .contains("Bogotá")
                .contains("Medellín")
                .contains("1A")
                .contains(cancellationReason);
    }

    @Test
    @DisplayName("Debe enviar notificación con tipo SMS correctamente")
    void testSendNotification_WithSMSType() {
        // Given
        ArgumentCaptor<NotificationRequest> captor = ArgumentCaptor.forClass(NotificationRequest.class);

        // When
        notificationHelper.sendPurchaseConfirmation(purchase, NotificationType.SMS);

        // Then
        verify(notificationFactory).send(captor.capture());
        assertThat(captor.getValue().type()).isEqualTo(NotificationType.SMS);
    }

    @Test
    @DisplayName("No debe enviar notificación si la compra no tiene tickets")
    void testSendPurchaseConfirmation_NoTickets() {
        // Given
        Purchase emptyPurchase = createPurchase(2L, user, BigDecimal.valueOf(100000), PaymentStatus.CONFIRMED);
        emptyPurchase.getTickets().clear(); // Vaciar tickets

        // When
        notificationHelper.sendPurchaseConfirmation(emptyPurchase, NotificationType.WHATSAPP);

        // Then
        verify(notificationFactory, never()).send(any());
    }

    @Test
    @DisplayName("No debe enviar notificación si el usuario no tiene teléfono")
    void testSendPurchaseConfirmation_NoPhone() {
        // Given
        User userWithoutPhone = createUser(2L, "nophone@example.com", null);
        Purchase purchaseWithoutPhone = createPurchase(3L, userWithoutPhone, BigDecimal.valueOf(100000), PaymentStatus.CONFIRMED);
        purchaseWithoutPhone.getTickets().add(ticket1);

        // When
        notificationHelper.sendPurchaseConfirmation(purchaseWithoutPhone, NotificationType.WHATSAPP);

        // Then
        verify(notificationFactory, never()).send(any());
    }

    @Test
    @DisplayName("Debe enviar notificación de cambio de andén correctamente")
    void testSendPlatformChange_Success() {
        // Given
        String newPlatform = "Andén 5";
        List<String> phones = List.of("3001234567", "3009876543");
        ArgumentCaptor<NotificationRequest> captor = ArgumentCaptor.forClass(NotificationRequest.class);

        // When
        notificationHelper.sendPlatformChange(trip, newPlatform, phones, NotificationType.WHATSAPP);

        // Then
        verify(notificationFactory, times(2)).send(captor.capture());

        List<NotificationRequest> allRequests = captor.getAllValues();
        assertThat(allRequests).hasSize(2);
        assertThat(allRequests.get(0).type()).isEqualTo(NotificationType.WHATSAPP);
        assertThat(allRequests.get(0).message())
                .contains("Cambio de Andén")
                .contains(newPlatform);
    }

    @Test
    @DisplayName("Debe enviar notificación de llegada próxima correctamente")
    void testSendArrivalSoon_Success() {
        // Given
        String platform = "Andén 3";
        ArgumentCaptor<NotificationRequest> captor = ArgumentCaptor.forClass(NotificationRequest.class);

        // When
        notificationHelper.sendArrivalSoon(ticket1, platform, NotificationType.SMS);

        // Then
        verify(notificationFactory, times(1)).send(captor.capture());

        NotificationRequest capturedRequest = captor.getValue();
        assertThat(capturedRequest.type()).isEqualTo(NotificationType.SMS);
        assertThat(capturedRequest.message())
                .contains("Próxima Llegada")
                .contains("Medellín")
                .contains("1A")
                .contains(platform);
    }

    // Helper methods
    private Route createRoute(Long id, String origin, String destination) {
        Route route = new Route();
        route.setId(id);
        route.setCode("R001");
        route.setName(origin + " - " + destination);
        route.setOrigin(origin);
        route.setDestination(destination);
        route.setDistanceKm(400.0F);
        route.setDurationMin(360F);
        return route;
    }

    private Bus createBus(Long id, String plate, Integer capacity) {
        Bus bus = new Bus();
        bus.setId(id);
        bus.setPlate(plate);
        bus.setCapacity(capacity);
        bus.setStatus(BusStatus.AVAILABLE);
        return bus;
    }

    private Trip createTrip(Long id, Bus bus, Route route) {
        Trip trip = new Trip();
        trip.setId(id);
        trip.setDate(LocalDate.of(2025, 12, 25));
        trip.setBus(bus);
        trip.setRoute(route);
        trip.setDepartureAt(OffsetDateTime.parse("2025-12-25T08:00:00-05:00"));
        trip.setArrivalAt(OffsetDateTime.parse("2025-12-25T14:00:00-05:00"));
        trip.setStatus(TripStatus.SCHEDULED);
        return trip;
    }

    private User createUser(Long id, String email, String phone) {
        User user = new User();
        user.setId(id);
        user.setEmail(email);
        user.setFullName("Test User");
        user.setPhone(phone);
        user.setPasswordHash("hash");
        user.setRole(UserRole.PASSENGER);
        user.setStatus(UserStatus.ACTIVE);
        return user;
    }

    private Passenger createPassenger(Long id, String fullName, String documentNumber, String phone) {
        Passenger passenger = new Passenger();
        passenger.setId(id);
        passenger.setFullName(fullName);
        passenger.setDocumentType("CC");
        passenger.setDocumentNumber(documentNumber);
        passenger.setBirthDate(LocalDate.of(1990, 5, 15));
        passenger.setPhoneNumber(phone);
        passenger.setCreatedAt(OffsetDateTime.now());
        return passenger;
    }

    private Stop createStop(Long id, String name, Integer order, Route route) {
        Stop stop = new Stop();
        stop.setId(id);
        stop.setName(name);
        stop.setOrder(order);
        stop.setLatitude(4.7110);
        stop.setLongitude(-74.0721);
        stop.setRoute(route);
        return stop;
    }

    private Purchase createPurchase(Long id, User user, BigDecimal totalAmount, PaymentStatus paymentStatus) {
        Purchase purchase = new Purchase();
        purchase.setId(id);
        purchase.setUser(user);
        purchase.setTotalAmount(totalAmount);
        purchase.setPaymentMethod(PaymentMethod.CARD);
        purchase.setPaymentStatus(paymentStatus);
        purchase.setCreatedAt(OffsetDateTime.now());
        purchase.setTickets(new ArrayList<>());
        return purchase;
    }

    private Ticket createTicket(Long id, String seatNumber, Trip trip, Passenger passenger,
                                Stop fromStop, Stop toStop, Purchase purchase) {
        Ticket ticket = new Ticket();
        ticket.setId(id);
        ticket.setSeatNumber(seatNumber);
        ticket.setPrice(BigDecimal.valueOf(75000));
        ticket.setStatus(TicketStatus.SOLD);
        ticket.setTrip(trip);
        ticket.setPassenger(passenger);
        ticket.setFromStop(fromStop);
        ticket.setToStop(toStop);
        ticket.setPurchase(purchase);
        return ticket;
    }
}
