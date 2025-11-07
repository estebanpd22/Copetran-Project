package co.unimagdalena.domine.repositories;

import co.unimagdalena.domine.entities.Amenity;
import co.unimagdalena.domine.entities.Bus;
import co.unimagdalena.domine.entities.BusStatus;
import co.unimagdalena.domine.entities.Trip;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface BusRepository extends JpaRepository<Bus,Long> {
    //Esta es OPCIONAL, en caso de que el ID no sea solo para la tabla
    Bus findById(long id);

    Optional<Bus> findByPlate(String plate);

    List<Bus> finByStatus(String status);

    @Query("SELECT b " +
            "FROM Bus b " +
            "LEFT JOIN FETCH b.seats " +
            "WHERE b.id = :busId")
    Optional<Bus> findByIdWithSeats(@Param("busId") Long busId);

    //Buses que tengan las comodidades especificadas
    List<Bus> findBusesByAmenities(Set<Amenity> amenities);

    //Buses que tenga viajes especificados
    List<Bus> findBusesByTrips(List<Trip> trips);

    @Modifying
    @Query("UPDATE Bus b " +
            "SET b.status = :status " +
            "WHERE b.id = :busId")
    void changeBusStatus(@Param("busId") Long busId, @Param("status") BusStatus status);
}
