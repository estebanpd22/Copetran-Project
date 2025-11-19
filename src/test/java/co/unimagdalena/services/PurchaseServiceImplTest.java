package co.unimagdalena.services;

import co.unimagdalena.api.dto.PurchaseDto.*;
import co.unimagdalena.domine.entities.*;
import co.unimagdalena.domine.repositories.PurchaseRepository;
import co.unimagdalena.domine.repositories.UserRepository;
import co.unimagdalena.exception.NotFoundException;
import co.unimagdalena.notification.NotificationHelper;
import co.unimagdalena.notification.NotificationType;
import co.unimagdalena.services.impl.PurchaseServiceImpl;
import co.unimagdalena.services.mapper.PurchaseMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PurchaseServiceImplTest {

    @Mock
    private PurchaseRepository purchaseRepository;

    @Mock
    private PurchaseMapper purchaseMapper;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TicketService ticketService;

    @Mock
    private SeatHoldService seatHoldService;

    @Mock
    private NotificationHelper notificationHelper;

    @InjectMocks
    private PurchaseServiceImpl purchaseService;

    private PurchaseCreateRequest createRequest;
    private Purchase purchase;
    private PurchaseResponse purchaseResponse;
    private User user;
    private Trip trip;
    private Ticket ticket;

    @BeforeEach
    void setUp() {
        // Setup User
        user = User.builder()
                .id(1L)
                .fullName("Juan Perez")
                .email("juan@example.com")
                .phone("+573001234567")
                .role(UserRole.PASSENGER)
                .status(UserStatus.ACTIVE)
                .build();

        // Setup Trip
        trip = Trip.builder()
                .id(1L)
                .date(LocalDate.now())
                .departureAt(OffsetDateTime.now().plusDays(1))
                .arrivalAt(OffsetDateTime.now().plusDays(1).plusHours(4))
                .status(TripStatus.SCHEDULED)
                .build();

        // Setup Ticket Request
        PurchaseCreateRequest.TicketRequest ticketRequest = new PurchaseCreateRequest.TicketRequest(
                1L, // tripId
                1L, // passengerId
                1L, // seatId
                "A1", // seatNumber
                1L, // fromStopId
                2L, // toStopId
                null // baggage
        );

        createRequest = new PurchaseCreateRequest(
                1L, // userId
                PaymentMethod.CARD,
                Collections.singletonList(ticketRequest)
        );

        // Setup Ticket
        ticket = Ticket.builder()
                .id(1L)
                .price(BigDecimal.valueOf(50000))
                .seatNumber("A1")
                .status(TicketStatus.SOLD)
                .trip(trip)
                .build();

        // Setup Purchase
        purchase = Purchase.builder()
                .id(1L)
                .paymentMethod(PaymentMethod.CARD)
                .totalAmount(BigDecimal.valueOf(50000))
                .paymentStatus(PaymentStatus.PENDING)
                .createdAt(OffsetDateTime.now())
                .user(user)
                .tickets(Collections.singletonList(ticket))
                .build();

        purchaseResponse = new PurchaseResponse(
                1L,
                BigDecimal.valueOf(50000),
                PaymentMethod.CARD,
                PaymentStatus.PENDING,
                OffsetDateTime.now(),
                null,
                Collections.emptyList()
        );
    }

    // ==================== CREATE PURCHASE ====================

    @Test
    @DisplayName("Should create purchase successfully")
    void shouldCreatePurchaseSuccessfully() {
        // Arrange
        doNothing().when(seatHoldService).validateActiveHolds(anyLong(), anyList(), anyLong());
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(purchaseMapper.toEntity(createRequest)).thenReturn(purchase);
        when(ticketService.createTicket(any(), any())).thenReturn(ticket);
        when(purchaseRepository.save(any(Purchase.class))).thenReturn(purchase);
        when(purchaseMapper.toResponse(purchase)).thenReturn(purchaseResponse);

        // Act
        PurchaseResponse result = purchaseService.createPurchase(createRequest);

        // Assert
        assertNotNull(result);
        assertEquals(PaymentStatus.PENDING, result.paymentStatus());
        verify(seatHoldService).validateActiveHolds(anyLong(), anyList(), anyLong());
        verify(userRepository).findById(1L);
        verify(ticketService).createTicket(any(), any());
        verify(purchaseRepository).save(any(Purchase.class));
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when tickets list is empty")
    void shouldThrowExceptionWhenTicketsListIsEmpty() {
        // Arrange
        PurchaseCreateRequest emptyRequest = new PurchaseCreateRequest(
                1L,
                PaymentMethod.CARD,
                Collections.emptyList()
        );

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> purchaseService.createPurchase(emptyRequest)
        );

        assertTrue(exception.getMessage().contains("La compra debe contener al menos un tiquete"));
        verify(purchaseRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when tickets belong to different trips")
    void shouldThrowExceptionWhenTicketsBelongToDifferentTrips() {
        // Arrange
        PurchaseCreateRequest.TicketRequest ticketRequest1 = new PurchaseCreateRequest.TicketRequest(
                1L, 1L, 1L, "A1", 1L, 2L, null
        );
        PurchaseCreateRequest.TicketRequest ticketRequest2 = new PurchaseCreateRequest.TicketRequest(
                2L, // Different tripId
                2L, 2L, "A2", 1L, 2L, null
        );

        PurchaseCreateRequest mixedRequest = new PurchaseCreateRequest(
                1L,
                PaymentMethod.CARD,
                Arrays.asList(ticketRequest1, ticketRequest2)
        );

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> purchaseService.createPurchase(mixedRequest)
        );

        assertTrue(exception.getMessage().contains("Todos los tiquetes en una compra deben pertenecer al mismo viaje"));
        verify(purchaseRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw NotFoundException when user does not exist")
    void shouldThrowExceptionWhenUserDoesNotExist() {
        // Arrange
        doNothing().when(seatHoldService).validateActiveHolds(anyLong(), anyList(), anyLong());
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        PurchaseCreateRequest requestWithInvalidUser = new PurchaseCreateRequest(
                999L,
                PaymentMethod.CARD,
                createRequest.tickets()
        );

        // Act & Assert
        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> purchaseService.createPurchase(requestWithInvalidUser)
        );

        assertTrue(exception.getMessage().contains("Usuario no encontrado"));
        verify(purchaseRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw IllegalStateException when total amount is zero or negative")
    void shouldThrowExceptionWhenTotalAmountIsZeroOrNegative() {
        // Arrange
        Ticket zeroTicket = Ticket.builder()
                .id(1L)
                .price(BigDecimal.ZERO)
                .build();

        doNothing().when(seatHoldService).validateActiveHolds(anyLong(), anyList(), anyLong());
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(purchaseMapper.toEntity(createRequest)).thenReturn(purchase);
        when(ticketService.createTicket(any(), any())).thenReturn(zeroTicket);

        // Act & Assert
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> purchaseService.createPurchase(createRequest)
        );

        assertTrue(exception.getMessage().contains("El monto total de la compra no puede ser cero o negativo"));
        verify(purchaseRepository, never()).save(any());
    }

    // ==================== CONFIRM PURCHASE ====================

    @Test
    @DisplayName("Should confirm purchase successfully")
    void shouldConfirmPurchaseSuccessfully() {
        // Arrange
        purchase.setPaymentStatus(PaymentStatus.PENDING);
        purchase.addTicket(ticket);

        when(purchaseRepository.findPurchaseById(1L)).thenReturn(Optional.of(purchase));
        doNothing().when(seatHoldService).validateActiveHolds(anyLong(), anyList(), anyLong());
        doNothing().when(ticketService).generateQrForTicket(anyLong());
        when(purchaseRepository.save(purchase)).thenReturn(purchase);
        doNothing().when(notificationHelper).sendPurchaseConfirmation(purchase, NotificationType.WHATSAPP);

        // Act
        purchaseService.confirmPurchase(1L, "PAY-REF-123");

        // Assert
        verify(purchaseRepository).findPurchaseById(1L);
        verify(seatHoldService).validateActiveHolds(anyLong(), anyList(), anyLong());
        verify(ticketService).generateQrForTicket(ticket.getId());
        verify(purchaseRepository).save(purchase);
        verify(notificationHelper).sendPurchaseConfirmation(purchase, NotificationType.WHATSAPP);
    }

    @Test
    @DisplayName("Should return early when purchase is already confirmed")
    void shouldReturnEarlyWhenPurchaseAlreadyConfirmed() {
        // Arrange
        purchase.setPaymentStatus(PaymentStatus.CONFIRMED);
        when(purchaseRepository.findPurchaseById(1L)).thenReturn(Optional.of(purchase));

        // Act
        purchaseService.confirmPurchase(1L, "PAY-REF-123");

        // Assert
        verify(purchaseRepository).findPurchaseById(1L);
        verify(seatHoldService, never()).validateActiveHolds(anyLong(), anyList(), anyLong());
        verify(ticketService, never()).generateQrForTicket(anyLong());
        verify(purchaseRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw IllegalStateException when confirming non-pending purchase")
    void shouldThrowExceptionWhenConfirmingNonPendingPurchase() {
        // Arrange
        purchase.setPaymentStatus(PaymentStatus.CANCELLED);
        when(purchaseRepository.findPurchaseById(1L)).thenReturn(Optional.of(purchase));

        // Act & Assert
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> purchaseService.confirmPurchase(1L, "PAY-REF-123")
        );

        assertTrue(exception.getMessage().contains("Solo se pueden confirmar compras en estado PENDING"));
        verify(purchaseRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw IllegalStateException when seat holds are expired during confirmation")
    void shouldThrowExceptionWhenSeatHoldsExpired() {
        // Arrange
        purchase.setPaymentStatus(PaymentStatus.PENDING);
        purchase.addTicket(ticket);

        when(purchaseRepository.findPurchaseById(1L)).thenReturn(Optional.of(purchase));
        doThrow(new IllegalStateException("SeatHolds expirados"))
                .when(seatHoldService).validateActiveHolds(anyLong(), anyList(), anyLong());

        // Act & Assert
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> purchaseService.confirmPurchase(1L, "PAY-REF-123")
        );

        assertTrue(exception.getMessage().contains("El tiempo de reserva de sus asientos ha expirado"));
        verify(purchaseRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should continue confirmation even if notification fails")
    void shouldContinueWhenNotificationFails() {
        // Arrange
        purchase.setPaymentStatus(PaymentStatus.PENDING);
        purchase.addTicket(ticket);

        when(purchaseRepository.findPurchaseById(1L)).thenReturn(Optional.of(purchase));
        doNothing().when(seatHoldService).validateActiveHolds(anyLong(), anyList(), anyLong());
        doNothing().when(ticketService).generateQrForTicket(anyLong());
        when(purchaseRepository.save(purchase)).thenReturn(purchase);
        doThrow(new RuntimeException("Notification failed"))
                .when(notificationHelper).sendPurchaseConfirmation(purchase, NotificationType.WHATSAPP);

        // Act - Should not throw exception
        assertDoesNotThrow(() -> purchaseService.confirmPurchase(1L, "PAY-REF-123"));

        // Assert
        verify(purchaseRepository).save(purchase);
        verify(notificationHelper).sendPurchaseConfirmation(purchase, NotificationType.WHATSAPP);
    }

    @Test
    @DisplayName("Should throw NotFoundException when purchase to confirm does not exist")
    void shouldThrowExceptionWhenPurchaseToConfirmDoesNotExist() {
        // Arrange
        when(purchaseRepository.findPurchaseById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> purchaseService.confirmPurchase(999L, "PAY-REF-123")
        );

        assertTrue(exception.getMessage().contains("Compra no encontrada"));
        verify(purchaseRepository, never()).save(any());
    }

    // ==================== CANCEL PURCHASE ====================

    @Test
    @DisplayName("Should cancel purchase successfully")
    void shouldCancelPurchaseSuccessfully() {
        // Arrange
        purchase.setPaymentStatus(PaymentStatus.PENDING);
        purchase.addTicket(ticket);

        when(purchaseRepository.findPurchaseById(1L)).thenReturn(Optional.of(purchase));
        doNothing().when(ticketService).releaseSeatsByPurchase(1L);
        when(purchaseRepository.save(purchase)).thenReturn(purchase);

        // Act
        purchaseService.cancelPurchase(1L);

        // Assert
        verify(purchaseRepository).findPurchaseById(1L);
        verify(ticketService).releaseSeatsByPurchase(1L);
        verify(purchaseRepository).save(purchase);
    }

    @Test
    @DisplayName("Should throw IllegalStateException when cancelling confirmed purchase")
    void shouldThrowExceptionWhenCancellingConfirmedPurchase() {
        // Arrange
        purchase.setPaymentStatus(PaymentStatus.CONFIRMED);
        when(purchaseRepository.findPurchaseById(1L)).thenReturn(Optional.of(purchase));

        // Act & Assert
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> purchaseService.cancelPurchase(1L)
        );

        assertTrue(exception.getMessage().contains("No se puede cancelar una compra ya confirmada"));
        verify(ticketService, never()).releaseSeatsByPurchase(anyLong());
        verify(purchaseRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw IllegalStateException when cancelling already cancelled purchase")
    void shouldThrowExceptionWhenCancellingAlreadyCancelledPurchase() {
        // Arrange
        purchase.setPaymentStatus(PaymentStatus.CANCELLED);
        when(purchaseRepository.findPurchaseById(1L)).thenReturn(Optional.of(purchase));

        // Act & Assert
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> purchaseService.cancelPurchase(1L)
        );

        assertTrue(exception.getMessage().contains("is already cancelled"));
        verify(ticketService, never()).releaseSeatsByPurchase(anyLong());
        verify(purchaseRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw NotFoundException when purchase to cancel does not exist")
    void shouldThrowExceptionWhenPurchaseToCancelDoesNotExist() {
        // Arrange
        when(purchaseRepository.findPurchaseById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> purchaseService.cancelPurchase(999L)
        );

        assertTrue(exception.getMessage().contains("Compra no encontrada"));
        verify(ticketService, never()).releaseSeatsByPurchase(anyLong());
        verify(purchaseRepository, never()).save(any());
    }

    // ==================== GET PURCHASE ====================

    @Test
    @DisplayName("Should get purchase by ID successfully")
    void shouldGetPurchaseByIdSuccessfully() {
        // Arrange
        when(purchaseRepository.findPurchaseById(1L)).thenReturn(Optional.of(purchase));
        when(purchaseMapper.toResponse(purchase)).thenReturn(purchaseResponse);

        // Act
        PurchaseResponse result = purchaseService.getPurchase(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.id());
        verify(purchaseRepository).findPurchaseById(1L);
        verify(purchaseMapper).toResponse(purchase);
    }

    @Test
    @DisplayName("Should throw NotFoundException when purchase does not exist")
    void shouldThrowExceptionWhenPurchaseDoesNotExist() {
        // Arrange
        when(purchaseRepository.findPurchaseById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> purchaseService.getPurchase(999L)
        );

        assertTrue(exception.getMessage().contains("Compra no encontrada"));
        verify(purchaseRepository).findPurchaseById(999L);
        verify(purchaseMapper, never()).toResponse(any());
    }

    // ==================== GET PURCHASES BY USER ID ====================

    @Test
    @DisplayName("Should get purchases by user ID successfully")
    void shouldGetPurchasesByUserIdSuccessfully() {
        // Arrange
        Purchase purchase2 = Purchase.builder()
                .id(2L)
                .paymentStatus(PaymentStatus.CONFIRMED)
                .build();

        user.addPurchase(purchase);
        user.addPurchase(purchase2);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(purchaseMapper.toResponse(any(Purchase.class))).thenReturn(purchaseResponse);

        // Act
        List<PurchaseResponse> result = purchaseService.getPurchasesByUserId(1L);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(userRepository).findById(1L);
        verify(purchaseMapper, times(2)).toResponse(any(Purchase.class));
    }

    @Test
    @DisplayName("Should throw NotFoundException when user does not exist for get purchases")
    void shouldThrowExceptionWhenUserDoesNotExistForGetPurchases() {
        // Arrange
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> purchaseService.getPurchasesByUserId(999L)
        );

        assertTrue(exception.getMessage().contains("Usuario no encontrado"));
        verify(userRepository).findById(999L);
        verify(purchaseMapper, never()).toResponse(any());
    }

    @Test
    @DisplayName("Should return empty list when user has no purchases")
    void shouldReturnEmptyListWhenUserHasNoPurchases() {
        // Arrange
        User userWithoutPurchases = User.builder()
                .id(2L)
                .fullName("User Without Purchases")
                .email("user2@example.com")
                .purchases(Collections.emptyList())
                .build();

        when(userRepository.findById(2L)).thenReturn(Optional.of(userWithoutPurchases));

        // Act
        List<PurchaseResponse> result = purchaseService.getPurchasesByUserId(2L);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(userRepository).findById(2L);
        verify(purchaseMapper, never()).toResponse(any());
    }

    // ==================== GET PURCHASES BY DATE RANGE ====================

    @Test
    @DisplayName("Should get purchases by date range successfully")
    void shouldGetPurchasesByDateRangeSuccessfully() {
        // Arrange
        OffsetDateTime start = OffsetDateTime.now().minusDays(7);
        OffsetDateTime end = OffsetDateTime.now();

        Purchase purchase2 = Purchase.builder()
                .id(2L)
                .createdAt(OffsetDateTime.now().minusDays(3))
                .build();

        List<Purchase> purchases = Arrays.asList(purchase, purchase2);

        when(purchaseRepository.findByDateRange(start, end)).thenReturn(purchases);
        when(purchaseMapper.toResponse(any(Purchase.class))).thenReturn(purchaseResponse);

        // Act
        List<PurchaseResponse> result = purchaseService.getPurchasesByDateRange(start, end);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(purchaseRepository).findByDateRange(start, end);
        verify(purchaseMapper, times(2)).toResponse(any(Purchase.class));
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when start date is after end date")
    void shouldThrowExceptionWhenStartDateIsAfterEndDate() {
        // Arrange
        OffsetDateTime start = OffsetDateTime.now();
        OffsetDateTime end = OffsetDateTime.now().minusDays(7);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> purchaseService.getPurchasesByDateRange(start, end)
        );

        assertTrue(exception.getMessage().contains("La fecha de inicio debe ser anterior a la fecha de fin"));
        verify(purchaseRepository, never()).findByDateRange(any(), any());
    }

    @Test
    @DisplayName("Should return empty list when no purchases in date range")
    void shouldReturnEmptyListWhenNoPurchasesInDateRange() {
        // Arrange
        OffsetDateTime start = OffsetDateTime.now().minusDays(30);
        OffsetDateTime end = OffsetDateTime.now().minusDays(20);

        when(purchaseRepository.findByDateRange(start, end)).thenReturn(Collections.emptyList());

        // Act
        List<PurchaseResponse> result = purchaseService.getPurchasesByDateRange(start, end);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(purchaseRepository).findByDateRange(start, end);
        verify(purchaseMapper, never()).toResponse(any());
    }

    // ==================== HELPER METHODS ====================

    private Purchase createPurchase(Long id, PaymentStatus status, BigDecimal amount) {
        return Purchase.builder()
                .id(id)
                .paymentMethod(PaymentMethod.CARD)
                .totalAmount(amount)
                .paymentStatus(status)
                .createdAt(OffsetDateTime.now())
                .user(user)
                .build();
    }

    private Ticket createTicket(Long id, BigDecimal price, Trip trip) {
        return Ticket.builder()
                .id(id)
                .price(price)
                .seatNumber("A1")
                .status(TicketStatus.SOLD)
                .trip(trip)
                .build();
    }

    private User createUser(Long id, String fullName, String email) {
        return User.builder()
                .id(id)
                .fullName(fullName)
                .email(email)
                .phone("+573001234567")
                .role(UserRole.PASSENGER)
                .status(UserStatus.ACTIVE)
                .build();
    }
}
