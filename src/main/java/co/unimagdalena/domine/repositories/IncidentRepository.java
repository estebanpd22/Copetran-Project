package co.unimagdalena.domine.repositories;

import co.unimagdalena.domine.entities.EntityType;
import co.unimagdalena.domine.entities.Incident;
import co.unimagdalena.domine.entities.IncidentType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface IncidentRepository extends JpaRepository<Incident, Long> {
    List<Incident> findByEntityType(EntityType entityType);
    Optional<Incident> findIncidentById(Long id);
    Optional<Incident> findIncidentByEntityId(Long entityId);

    @Query("SELECT i FROM Incident i WHERE i.incidentType= :type ORDER BY i.createdAt DESC")
    List<Incident> findRecentByType(@Param("type") IncidentType type);
}