package co.unimagdalena.repositories;

import co.unimagdalena.domine.entities.*;
import co.unimagdalena.domine.repositories.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

public class PurchaseRepositoryTest extends AbstractRepositoryTI {
    @Autowired
    private PurchaseRepository purchaseRepository;
    @Autowired
    private UserRepository userRepository;

    private User createUser(String fullName, String email, String phone, String passwordHash,
                            UserRole role, UserStatus status, LocalDateTime createdAt) {
        return userRepository.save(User.builder()
                .fullName(fullName)
                .email(email)
                .phone(phone)
                .passwordHash(passwordHash)
                .role(role)
                .status(status)
                .createdAt(createdAt)
                .build());
    }

    private Purchase createPurchase(User user, PaymentMethod paymentMethod, BigDecimal totalAmount,
                                    PaymentStatus paymentStatus, OffsetDateTime createdAt) {
        return purchaseRepository.save(Purchase.builder()
                .user(user)
                .paymentMethod(paymentMethod)
                .totalAmount(totalAmount)
                .paymentStatus(paymentStatus)
                .createdAt(createdAt)
                .build());
    }

    @Test
    @DisplayName("Debe encontrar una compra por ID")
    void shouldFindPurchaseById() {
        // Given
        User user = createUser("Juan Pérez", "juan.perez@email.com", "3001234567",
                "hashedpassword", UserRole.PASSENGER, UserStatus.ACTIVE,
                LocalDateTime.now());
        Purchase purchase = createPurchase(user, PaymentMethod.CARD, BigDecimal.valueOf(150000),
                PaymentStatus.CONFIRMED, OffsetDateTime.now());

        // When
        Optional<Purchase> found = purchaseRepository.findPurchaseById(purchase.getId());

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(purchase.getId());
        assertThat(found.get().getPaymentMethod()).isEqualTo(PaymentMethod.CARD);
        assertThat(found.get().getTotalAmount()).isEqualByComparingTo(BigDecimal.valueOf(150000));
        assertThat(found.get().getPaymentStatus()).isEqualTo(PaymentStatus.CONFIRMED);
        assertThat(found.get().getUser().getId()).isEqualTo(user.getId());
    }

    @Test
    @DisplayName("Debe encontrar compras por estado de pago")
    void shouldFindPurchasesByPaymentStatus() {
        // Given
        User user1 = createUser("María López", "maria.lopez@email.com", "3009876543",
                "hashedpassword1", UserRole.PASSENGER, UserStatus.ACTIVE,
                LocalDateTime.now());
        User user2 = createUser("Carlos Ruiz", "carlos.ruiz@email.com", "3002345678",
                "hashedpassword2", UserRole.PASSENGER, UserStatus.ACTIVE,
                LocalDateTime.now());

        createPurchase(user1, PaymentMethod.TRANSFER, BigDecimal.valueOf(80000),
                PaymentStatus.PENDING, OffsetDateTime.now());
        createPurchase(user2, PaymentMethod.CASH, BigDecimal.valueOf(120000),
                PaymentStatus.PENDING, OffsetDateTime.now());
        createPurchase(user1, PaymentMethod.CARD, BigDecimal.valueOf(95000),
                PaymentStatus.CONFIRMED, OffsetDateTime.now());

        // When
        List<Purchase> pendingPurchases = purchaseRepository.findPurchasesByPaymentStatus(PaymentStatus.PENDING);

        // Then
        assertThat(pendingPurchases).hasSize(2);
        assertThat(pendingPurchases).allMatch(p -> p.getPaymentStatus() == PaymentStatus.PENDING);
    }

    @Test
    @DisplayName("Debe encontrar compras en un rango de fechas")
    void shouldFindByDateRange() {
        // Given
        User user = createUser("Ana Torres", "ana.torres@email.com", "3008765432",
                "hashedpassword3", UserRole.PASSENGER, UserStatus.ACTIVE,
                LocalDateTime.now());

        OffsetDateTime start = OffsetDateTime.now().minusDays(7);
        OffsetDateTime end = OffsetDateTime.now();

        createPurchase(user, PaymentMethod.QR, BigDecimal.valueOf(75000),
                PaymentStatus.CONFIRMED, start.plusDays(1));
        createPurchase(user, PaymentMethod.CARD, BigDecimal.valueOf(110000),
                PaymentStatus.CONFIRMED, start.plusDays(3));
        createPurchase(user, PaymentMethod.TRANSFER, BigDecimal.valueOf(65000),
                PaymentStatus.CONFIRMED, end.minusDays(1));

        // When
        List<Purchase> purchases = purchaseRepository.findByDateRange(start, end);

        // Then
        assertThat(purchases).hasSize(3);
        assertThat(purchases).allMatch(p ->
                !p.getCreatedAt().isBefore(start) && !p.getCreatedAt().isAfter(end)
        );
    }

    @Test
    @DisplayName("Debe retornar empty cuando no existe la compra")
    void shouldReturnEmptyWhenPurchaseNotFound() {
        // When
        Optional<Purchase> found = purchaseRepository.findPurchaseById(999L);

        // Then
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("Debe retornar lista vacía cuando no hay compras con el estado especificado")
    void shouldReturnEmptyListWhenNoPurchasesWithStatus() {
        // Given
        User user = createUser("Pedro Gómez", "pedro.gomez@email.com", "3003456789",
                "hashedpassword4", UserRole.PASSENGER, UserStatus.ACTIVE,
                LocalDateTime.now());
        createPurchase(user, PaymentMethod.CASH, BigDecimal.valueOf(90000),
                PaymentStatus.CONFIRMED, OffsetDateTime.now());

        // When
        List<Purchase> cancelledPurchases = purchaseRepository.findPurchasesByPaymentStatus(PaymentStatus.CANCELLED);

        // Then
        assertThat(cancelledPurchases).isEmpty();
    }

    @Test
    @DisplayName("Debe retornar lista vacía cuando no hay compras en el rango de fechas")
    void shouldReturnEmptyListWhenNoPurchasesInDateRange() {
        // Given
        User user = createUser("Laura Díaz", "laura.diaz@email.com", "3007654321",
                "hashedpassword5", UserRole.PASSENGER, UserStatus.ACTIVE,
                LocalDateTime.now());
        createPurchase(user, PaymentMethod.CARD, BigDecimal.valueOf(85000),
                PaymentStatus.CONFIRMED, OffsetDateTime.now());

        OffsetDateTime start = OffsetDateTime.now().minusYears(2);
        OffsetDateTime end = OffsetDateTime.now().minusYears(1);

        // When
        List<Purchase> purchases = purchaseRepository.findByDateRange(start, end);

        // Then
        assertThat(purchases).isEmpty();
    }
}