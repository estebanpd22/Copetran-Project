package co.unimagdalena.domine.repositories;

import co.unimagdalena.domine.entities.Route;
import co.unimagdalena.domine.entities.Trip;
import co.unimagdalena.domine.entities.TripStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;

public interface TripRepository extends JpaRepository<Trip,Long> {
    //Lista de viajes con igual estado y dia en la misma ruta
    List<Trip> findByRouteIdAndDateAndStatus(Long routeId, LocalDate date, TripStatus status);

    //Lista de viajes de un día especifico, segun el estado. (Ejemplo, viajes cancelados del dia actual)
    List<Trip> findByDateAndStatus(LocalDate date, TripStatus status);

    List<Trip> findByBusIdAndStatus(Long busId, TripStatus status);
    List<Trip> findByStatus(TripStatus status);

    @Query( "SELECT t " +
            "FROM Trip t " +
            "WHERE t.route.id = :routeId " +
            "      AND t.date = :date " +
            "      AND t.status IN :statuses")
    List<Trip> findAvailableTrips(@Param("routeId") Long routeId,  @Param("date") LocalDate date,
                                  @Param("statuses") List<TripStatus> statuses);

    // viajes que estan proximos a salir.
    @Query( "SELECT t " +
            "FROM Trip t " +
            "WHERE t.date = :date " +
            "       AND CAST(t.status AS string) = 'SCHEDULED'" +
            "      AND t.departureAt <= :threshold")
    List<Trip> findTripsNearDeparture(@Param("date") LocalDate date,
                                      @Param("threshold") OffsetDateTime threshold);

    //encontrar viaje que con bus y sus asientos para ver su disponibilidad
    @Query( "SELECT t " +
            "FROM Trip t " +
            "LEFT JOIN FETCH t.bus b " +
            "LEFT JOIN FETCH b.seats " +
            "WHERE t.id = :tripId")
    Optional<Trip> findByIdWithBusAndSeats(@Param("tripId") Long tripId);
}