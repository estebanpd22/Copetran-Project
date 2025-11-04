package co.unimagdalena.domine.repositories;

import co.unimagdalena.domine.entities.Baggage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BaggageRepository extends JpaRepository<Baggage, Long> {
    List<Baggage> findByTicketId(Long ticketId);

    @Query("SELECT b FROM Baggage b WHERE b.ticket.trip.id = :tripId")
    List<Baggage> findByTripId(@Param("tripId") Long tripId);
}
