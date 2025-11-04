package co.unimagdalena.domine.repositories;

import co.unimagdalena.domine.entities.Parcel;
import co.unimagdalena.domine.entities.ParcelStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface ParcelRepository extends JpaRepository<Parcel,Long> {
    Optional<Parcel> findByCode(String code);
    List<Parcel> findBySenderNameIgnoringCase(String senderName);
    List<Parcel> findByReceiverNameIgnoringCase(String receiverName);
    Optional<Parcel> findByDeliveryOTP(String deliveryOTP);

    @Query("""
        SELECT p FROM Parcel p
        WHERE (:fromId IS NULL OR p.fromStop.id = :fromId)
            AND (:toId IS NULL OR p.toStop.id = :toId)
    """)
    List<Parcel> findAllByStretch(@Param("fromId") Long fromId, @Param("toId") Long toId);

    long countByStatus(ParcelStatus status);
    Page<Parcel> findAllByStatus(ParcelStatus status, Pageable pageable);

    @Query("SELECT SUM(P.price) FROM Parcel P")
    BigDecimal calculateTotal();
}