package co.unimagdalena.domine.repositories;

import co.unimagdalena.domine.entities.BusStatus;
import co.unimagdalena.domine.entities.SeatHold;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface SeatHoldRepository extends JpaRepository<SeatHold, Long> {
    List<SeatHold> findByTripId(Long tripId);
    List<SeatHold> findByUserId(Long userId);

    @Query("SELECT s FROM SeatHold s WHERE s.expiresAt < CURRENT_TIMESTAMP AND s.status = 'HOLD'")
    List<SeatHold> findExpiredHolds();

    //El estado de una silla es dinamico, puede ser modificado en cualquier momento
    @Modifying(clearAutomatically = true)
    @Transactional
    @Query("UPDATE Bus b " +
            "SET b.status = :status " +
            "WHERE b.id = :busId")
    void changeBusStatus(@Param("busId") Long busId, @Param("status") BusStatus status);
}