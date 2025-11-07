package co.unimagdalena.domine.repositories;

import co.unimagdalena.domine.entities.PaymentStatus;
import co.unimagdalena.domine.entities.Purchase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;


public interface PurchaseRepository extends JpaRepository<Purchase, Long> {
    Optional<Purchase> findPurchaseById(Long id);
    //Lista de compras segun estado de pago (Ejemplo, pagos pendientes o cancelados,
    // los usuarios en estas condiciones no pueden abordar)
    List<Purchase> findPurchasesByPaymentStatus(PaymentStatus paymentStatus);

    @Query("SELECT p FROM Purchase p WHERE p.createdAt BETWEEN :start AND :end")
    List<Purchase> findByDateRange(@Param("start") OffsetDateTime start, @Param("end") OffsetDateTime end);
}
