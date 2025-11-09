package co.unimagdalena.domine.repositories;

import co.unimagdalena.domine.entities.Seat;
import co.unimagdalena.domine.entities.Ticket;
import co.unimagdalena.domine.entities.TicketStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

public interface TicketRepository extends JpaRepository<Ticket,Long> {
    List<Ticket> findByTripId(Long tripId);
    List<Ticket> findByPurchaseId(Long purchaseId);
    Optional<Ticket> findByQrCode(String qrCode);
    List<Ticket> findTicketByStatus(TicketStatus status);
    List<Ticket> findByPurchaseUserId(Long purchaseUserId);

    @Query("SELECT COUNT(t) FROM Ticket t WHERE t.trip.id = :tripId AND t.status = 'SOLD'")
    long countSoldByTrip(@Param("tripId") Long tripId);

    @Query("""
    SELECT COUNT(t)
    FROM Ticket t
    WHERE t.status = :status
      AND t.createdAt >= COALESCE(:start, t.createdAt)
      AND t.createdAt <= COALESCE(:end, t.createdAt)
    """)
    long countByStatusAndOptionalDateRange(@Param("status") TicketStatus status, @Param("start") OffsetDateTime start,
                                           @Param("end") OffsetDateTime end);
}