package co.unimagdalena.domine.repositories;

import co.unimagdalena.domine.entities.Checklist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ChecklistRepository extends JpaRepository<Checklist, Long> {
    Optional<Checklist> findByTripId(Long tripId);
    boolean existsByTripId(Long tripId);
}
