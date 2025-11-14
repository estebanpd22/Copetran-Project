package co.unimagdalena.services.impl;

import co.unimagdalena.api.dto.PurchaseDto.*;
import co.unimagdalena.domine.entities.PaymentStatus;
import co.unimagdalena.domine.entities.Purchase;
import co.unimagdalena.domine.entities.Ticket;
import co.unimagdalena.domine.entities.User;
import co.unimagdalena.domine.repositories.PurchaseRepository;
import co.unimagdalena.domine.repositories.UserRepository;
import co.unimagdalena.exception.NotFoundException;
import co.unimagdalena.notification.NotificationHelper;
import co.unimagdalena.notification.NotificationType;
import co.unimagdalena.services.PurchaseService;
import co.unimagdalena.services.SeatHoldService;
import co.unimagdalena.services.TicketService;
import co.unimagdalena.services.mapper.PurchaseMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class PurchaseServiceImpl implements PurchaseService {

    private final PurchaseRepository purchaseRepository;
    private final PurchaseMapper purchaseMapper;
    private final UserRepository userRepository;
    private final TicketService ticketService;
    private final SeatHoldService seatHoldService;
    private final NotificationHelper notificationHelper;

    // --- ORQUESTACIÓN (Creación) ---

    @Override
    @Transactional
    public PurchaseResponse createPurchase(PurchaseCreateRequest request) {

        // 1. Validaciones
        validateAllTicketsSameTrip(request.tickets());
        seatHoldService.validateActiveHolds(
                request.tickets().get(0).tripId(), // 1. ID del Viaje
                request.tickets().stream().map(PurchaseCreateRequest.TicketRequest::seatNumber).toList(), // 2. Lista de asientos
                request.userId() // 3. ID del Usuario
        );

        // 2. Mapeo y Resolución
        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado con ID: " + request.userId()));

        Purchase purchase = purchaseMapper.toEntity(request);
        purchase.setUser(user);
        purchase.setCreatedAt(OffsetDateTime.now());

        // ESTADO CORREGIDO: Inicia siempre como PENDING (eliminamos PENDING_SYNC)
        purchase.setPaymentStatus(PaymentStatus.PENDING);

        // 3. Orquestación: Crear Tiquetes y Calcular Total
        BigDecimal totalAmount = BigDecimal.ZERO;

        for (PurchaseCreateRequest.TicketRequest ticketReq : request.tickets()) {
            // Delega creación y cálculo de precio
            Ticket ticket = ticketService.createTicket(ticketReq, purchase);
            purchase.addTicket(ticket);
            totalAmount = totalAmount.add(ticket.getPrice());
        }

        // 4. Finalizar y Persistir
        if (totalAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalStateException("El monto total de la compra no puede ser cero o negativo.");
        }

        purchase.setTotalAmount(totalAmount);
        Purchase savedPurchase = purchaseRepository.save(purchase);

        log.info("Compra ID {} creada. Status: PENDING", savedPurchase.getId());

        return purchaseMapper.toResponse(savedPurchase);
    }

    // --- GESTIÓN DE ESTADO (Transiciones) ---

    @Override
    @Transactional
    public void confirmPurchase(Long purchaseId, String paymentReference) {
        Purchase purchase = findPurchaseById(purchaseId);

        // ESTADO CORREGIDO: Validamos contra CONFIRMED
        if (purchase.getPaymentStatus() == PaymentStatus.CONFIRMED) {
            log.warn("Intento de re-confirmar compra ya confirmada: {}", purchaseId);
            return;
        }

        if (purchase.getPaymentStatus() != PaymentStatus.PENDING) {
            throw new IllegalStateException("Solo se pueden confirmar compras en estado PENDING.");
        }

        // 1. Validación Defensiva: Verificar que Holds sigan activos
        try {
            seatHoldService.validateActiveHolds(
                    purchase.getTickets().get(0).getTrip().getId(), // 1. ID del Viaje
                    purchase.getTickets().stream().map(Ticket::getSeatNumber).toList(), // 2. Lista de asientos
                    purchase.getUser().getId() // 3. ID del Usuario
            );
        } catch (Exception e) {
            log.error("SeatHolds expirados para la compra {}", purchaseId);
            // Ya que eliminamos markAsFailed, solo lanzamos la excepción para revertir la transacción.
            throw new IllegalStateException("El tiempo de reserva de sus asientos ha expirado. Por favor, intente de nuevo.");
        }

        // 2. Actualizar Tickets y Generar QR (HECHO MANUALMENTE - NO DELEGADO)
        purchase.getTickets().forEach(ticket -> {
            // Asumiendo que la entidad Ticket tiene un campo status.
            // ticket.setStatus(Ticket.Status.SOLD);
            ticketService.generateQrForTicket(ticket.getId()); // Usando método existente
        });

        // 3. Actualizar Purchase
        purchase.setPaymentStatus(PaymentStatus.CONFIRMED);
        // purchase.setPaymentReference(paymentReference); // Si la entidad lo permite
        purchaseRepository.save(purchase);
        log.info("Compra ID {} confirmada (CONFIRMED). Referencia: {}", purchaseId, paymentReference);

        // 4. Notificación
        try {
            notificationHelper.sendPurchaseConfirmation(purchase, NotificationType.WHATSAPP);
        } catch (Exception e) {
            log.error("Fallo al enviar notificación: {}", e.getMessage());
        }
    }

    @Override
    @Transactional
    public void cancelPurchase(Long purchaseId) {
        Purchase purchase = findPurchaseById(purchaseId);

        if (purchase.getPaymentStatus() == PaymentStatus.CONFIRMED) {
            throw new IllegalStateException("No se puede cancelar una compra ya confirmada.");
        }

        if (purchase.getPaymentStatus() == PaymentStatus.CANCELLED) {
            throw new IllegalStateException(String.format("Purchase with ID %d is already cancelled", purchaseId));
        }

        // 1. Liberar Asientos (Delegación LIMPIA)
        ticketService.releaseSeatsByPurchase(purchaseId);

        // 2. Actualizar Tickets (HECHO MANUALMENTE)
        purchase.getTickets().forEach(ticket -> {
            // Asumiendo que la entidad Ticket tiene un campo status.
            // ticket.setStatus(Ticket.Status.CANCELLED);
        });

        // 3. Actualizar Purchase
        purchase.setPaymentStatus(PaymentStatus.CANCELLED);
        purchaseRepository.save(purchase);
        log.info("Compra ID {} cancelada.", purchaseId);
    }

    // El método reconcileOfflineSales se elimina porque no tienes el estado PENDING_SYNC.
    // El método markAsFailed se elimina porque no tienes el estado FAILED.

    // --- BÚSQUEDAS (Finders) ---
    // (Estos métodos permanecen sin cambios)

    @Override
    @Transactional(readOnly = true)
    public PurchaseResponse getPurchase(Long purchaseId) {
        Purchase purchase = findPurchaseById(purchaseId);
        return purchaseMapper.toResponse(purchase);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PurchaseResponse> getPurchasesByUserId(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado: " + userId));

        return user.getPurchases().stream()
                .map(purchaseMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PurchaseResponse> getPurchasesByDateRange(OffsetDateTime start, OffsetDateTime end) {
        if (start.isAfter(end)) {
            throw new IllegalArgumentException("La fecha de inicio debe ser anterior a la fecha de fin.");
        }
        List<Purchase> purchases = purchaseRepository.findByDateRange(start, end);
        return purchases.stream()
                .map(purchaseMapper::toResponse)
                .collect(Collectors.toList());
    }

    // --- MÉTODOS PRIVADOS DE UTILIDAD ---

    private Purchase findPurchaseById(Long purchaseId) {
        return purchaseRepository.findPurchaseById(purchaseId)
                .orElseThrow(() -> new NotFoundException("Compra no encontrada con ID: " + purchaseId));
    }

    private void validateAllTicketsSameTrip(List<PurchaseCreateRequest.TicketRequest> tickets) {
        if (tickets.isEmpty()) {
            throw new IllegalArgumentException("La compra debe contener al menos un tiquete.");
        }

        Long firstTripId = tickets.get(0).tripId();
        boolean allSameTrip = tickets.stream()
                .allMatch(ticket -> ticket.tripId().equals(firstTripId));

        if (!allSameTrip) {
            throw new IllegalArgumentException("Todos los tiquetes en una compra deben pertenecer al mismo viaje.");
        }
    }
}
