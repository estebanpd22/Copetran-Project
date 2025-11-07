package co.unimagdalena.domine.repositories;

import co.unimagdalena.domine.entities.Seat;
import co.unimagdalena.domine.entities.SeatType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SeatRepository extends JpaRepository<Seat,Long> {
    List<Seat> finByBusIdOrderByNumberAsc(Long busId);
    Optional<Seat> findByBusIdAndNumber(Long busId, Integer number);
    List<Seat> finByBusIdAndType(Long busId, SeatType type);
    long countByBusId(Long busId);
}
