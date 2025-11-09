package co.unimagdalena.domine.repositories;

import co.unimagdalena.domine.entities.Stop;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface StopRepository extends JpaRepository<Stop,Long> {
    Optional<Stop> findStopById(Long id);
    List<Stop> findByRouteIdOrderByOrderAsc(Long routeId);
    Optional<Stop> findByRouteIdAndOrder(Long routeId, Integer order);
    boolean existsByRouteIdAndId(Long routeId, Long stopId);
    @Query( "SELECT s " +
            "FROM Stop s " +
            "WHERE s.route.id = :routeId AND s.order " +
            "BETWEEN :fromOrder AND :toOrder " +
            "ORDER BY s.order")
    List<Stop> findStopsBetween(@Param("routeId") Long routeId, @Param("fromOrder") Integer fromOrder, @Param("toOrder") Integer toOrder);
}