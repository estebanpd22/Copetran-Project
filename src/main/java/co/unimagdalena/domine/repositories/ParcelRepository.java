package co.unimagdalena.domine.repositories;

import co.unimagdalena.domine.entities.Parcel;
import co.unimagdalena.domine.entities.ParcelStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface ParcelRepository extends JpaRepository<Parcel,Long> {
    Optional<Parcel> findParcelById(Long id);
    Optional<Parcel> findByCode(String code);
    List<Parcel> findBySenderNameIgnoringCase(String senderName);
    List<Parcel> findBySenderNameIgnoreCaseAndSenderPhone(String senderName, String senderPhone);
    List<Parcel> findByReceiverNameIgnoringCase(String receiverName);
    List<Parcel> findByReceiverNameIgnoringCaseAndReceiverPhone(String receiverName, String receiverPhone);
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

    //El estado del Parcel es dinamico, puede ser modificado en cualquier momento
    @Modifying(clearAutomatically = true)
    @Transactional
    @Query("UPDATE Parcel p " +
            "SET p.status = :status " +
            "WHERE p.id = :parcelId")
    void changeParcelStatus(@Param("parcelId") Long parcelId, @Param("status") ParcelStatus status);

    //Una vez entregado el producto, se deberá proporcionar una foto
    @Modifying(clearAutomatically = true)
    @Transactional
    @Query("""
            UPDATE Parcel p
            SET p.proofPhotoUrl = :proofPhotoUrl
            WHERE p.id = :parcelId""")
    void addProofPhotoUrl(@Param("parcelId") Long parcelId, @Param("proofPhotoUrl") String proofPhotoUrl);
}