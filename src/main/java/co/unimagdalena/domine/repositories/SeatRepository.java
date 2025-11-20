package co.unimagdalena.domine.repositories;

import co.unimagdalena.domine.entities.Seat;
import co.unimagdalena.domine.entities.SeatType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SeatRepository extends JpaRepository<Seat,Long> {
    Optional<Seat> findSeatByBus_Id(Long busId);
    List<Seat> findByBusIdOrderByNumberAsc(Long busId);
    Optional<Seat> findByBusIdAndNumber(Long busId, Integer number);
    List<Seat> findByBusIdAndType(Long busId, SeatType type);
    long countByBusId(Long busId);

    double countSeatsByBus_Id(Long busId);
}