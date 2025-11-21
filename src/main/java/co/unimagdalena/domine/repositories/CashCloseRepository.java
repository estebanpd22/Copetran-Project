package co.unimagdalena.domine.repositories;

import co.unimagdalena.domine.entities.CashClose;
import co.unimagdalena.domine.entities.CashCloseStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CashCloseRepository extends JpaRepository<CashClose, Long> {
    List<CashClose> findByUserId(Long userId);
    List<CashClose> findByTripId(Long tripId);
    Optional<CashClose> findByTripIdAndUserId(Long tripId, Long userId);
    List<CashClose> findByStatus(CashCloseStatus status);
    
    @Query("SELECT c FROM CashClose c WHERE c.closedAt BETWEEN :startDate AND :endDate")
    List<CashClose> findByClosedAtBetween(@Param("startDate") OffsetDateTime startDate, 
                                          @Param("endDate") OffsetDateTime endDate);
}
