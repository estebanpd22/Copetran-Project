package co.unimagdalena.repositories;

import co.unimagdalena.domine.entities.*;
import co.unimagdalena.domine.repositories.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

public class SeatRepositoryTest extends AbstractRepositoryTI {
    @Autowired
    private SeatRepository seatRepository;
    @Autowired
    private BusRepository busRepository;

    private Bus createBus(String plate, Integer capacity, BusStatus status, OffsetDateTime soatExpirationDate) {
        return busRepository.save(Bus.builder()
                .plate(plate)
                .capacity(capacity)
                .status(status)
                .soatExpirationDate(soatExpirationDate)
                .build());
    }

    private Seat createSeat(Bus bus, Integer number, BigDecimal price, SeatType type, SeatStatus status) {
        return seatRepository.save(Seat.builder()
                .bus(bus)
                .number(number)
                .price(price)
                .type(type)
                .status(status)
                .build());
    }

    @Test
    @DisplayName("Debe encontrar un asiento por busId")
    void shouldFindSeatByBusId() {
        // Given
        Bus bus = createBus("ABC123", 40, BusStatus.AVAILABLE, OffsetDateTime.now().plusYears(1));
        Seat seat = createSeat(bus, 1, BigDecimal.valueOf(50000), SeatType.STANDARD, SeatStatus.AVAILABLE);

        // When
        Optional<Seat> found = seatRepository.findSeatByBus_Id(bus.getId());

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getBus().getId()).isEqualTo(bus.getId());
        assertThat(found.get().getNumber()).isEqualTo(1);
    }

    @Test
    @DisplayName("Debe encontrar asientos por busId ordenados por número ascendente")
    void shouldFindByBusIdOrderByNumberAsc() {
        // Given
        Bus bus = createBus("XYZ789", 40, BusStatus.AVAILABLE, OffsetDateTime.now().plusYears(1));
        createSeat(bus, 5, BigDecimal.valueOf(50000), SeatType.STANDARD, SeatStatus.AVAILABLE);
        createSeat(bus, 2, BigDecimal.valueOf(60000), SeatType.PREFERENTIAL, SeatStatus.AVAILABLE);
        createSeat(bus, 8, BigDecimal.valueOf(50000), SeatType.STANDARD, SeatStatus.TAKEN);

        // When
        List<Seat> seats = seatRepository.findByBusIdOrderByNumberAsc(bus.getId());

        // Then
        assertThat(seats).hasSize(3);
        assertThat(seats.get(0).getNumber()).isEqualTo(2);
        assertThat(seats.get(1).getNumber()).isEqualTo(5);
        assertThat(seats.get(2).getNumber()).isEqualTo(8);
    }

    @Test
    @DisplayName("Debe encontrar un asiento por busId y número")
    void shouldFindByBusIdAndNumber() {
        // Given
        Bus bus = createBus("DEF456", 40, BusStatus.AVAILABLE, OffsetDateTime.now().plusYears(1));
        createSeat(bus, 10, BigDecimal.valueOf(55000), SeatType.STANDARD, SeatStatus.AVAILABLE);
        createSeat(bus, 15, BigDecimal.valueOf(65000), SeatType.PREFERENTIAL, SeatStatus.AVAILABLE);

        // When
        Optional<Seat> found = seatRepository.findByBusIdAndNumber(bus.getId(), 15);

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getNumber()).isEqualTo(15);
        assertThat(found.get().getPrice()).isEqualByComparingTo(BigDecimal.valueOf(65000));
        assertThat(found.get().getType()).isEqualTo(SeatType.PREFERENTIAL);
    }

    @Test
    @DisplayName("Debe encontrar asientos por busId y tipo")
    void shouldFindByBusIdAndType() {
        // Given
        Bus bus = createBus("GHI789", 40, BusStatus.AVAILABLE, OffsetDateTime.now().plusYears(1));
        createSeat(bus, 1, BigDecimal.valueOf(50000), SeatType.STANDARD, SeatStatus.AVAILABLE);
        createSeat(bus, 2, BigDecimal.valueOf(50000), SeatType.STANDARD, SeatStatus.AVAILABLE);
        createSeat(bus, 3, BigDecimal.valueOf(70000), SeatType.PREFERENTIAL, SeatStatus.AVAILABLE);

        // When
        List<Seat> standardSeats = seatRepository.findByBusIdAndType(bus.getId(), SeatType.STANDARD);

        // Then
        assertThat(standardSeats).hasSize(2);
        assertThat(standardSeats).allMatch(s -> s.getType() == SeatType.STANDARD);
    }

    @Test
    @DisplayName("Debe contar los asientos de un bus")
    void shouldCountByBusId() {
        // Given
        Bus bus = createBus("JKL012", 40, BusStatus.AVAILABLE, OffsetDateTime.now().plusYears(1));
        createSeat(bus, 1, BigDecimal.valueOf(50000), SeatType.STANDARD, SeatStatus.AVAILABLE);
        createSeat(bus, 2, BigDecimal.valueOf(60000), SeatType.PREFERENTIAL, SeatStatus.AVAILABLE);
        createSeat(bus, 3, BigDecimal.valueOf(50000), SeatType.STANDARD, SeatStatus.TAKEN);
        createSeat(bus, 4, BigDecimal.valueOf(50000), SeatType.STANDARD, SeatStatus.UNAVAILABLE);

        // When
        long count = seatRepository.countByBusId(bus.getId());

        // Then
        assertThat(count).isEqualTo(4);
    }

    @Test
    @DisplayName("Debe retornar empty cuando no existe el asiento")
    void shouldReturnEmptyWhenSeatNotFound() {
        // Given
        Bus bus = createBus("MNO345", 40, BusStatus.AVAILABLE, OffsetDateTime.now().plusYears(1));

        // When
        Optional<Seat> found = seatRepository.findByBusIdAndNumber(bus.getId(), 99);

        // Then
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("Debe retornar lista vacía cuando el bus no tiene asientos")
    void shouldReturnEmptyListWhenBusHasNoSeats() {
        // Given
        Bus bus = createBus("PQR678", 40, BusStatus.AVAILABLE, OffsetDateTime.now().plusYears(1));

        // When
        List<Seat> seats = seatRepository.findByBusIdOrderByNumberAsc(bus.getId());

        // Then
        assertThat(seats).isEmpty();
    }

    @Test
    @DisplayName("Debe retornar lista vacía cuando no hay asientos del tipo especificado")
    void shouldReturnEmptyListWhenNoSeatsOfType() {
        // Given
        Bus bus = createBus("STU901", 40, BusStatus.AVAILABLE, OffsetDateTime.now().plusYears(1));
        createSeat(bus, 1, BigDecimal.valueOf(50000), SeatType.STANDARD, SeatStatus.AVAILABLE);

        // When
        List<Seat> preferentialSeats = seatRepository.findByBusIdAndType(bus.getId(), SeatType.PREFERENTIAL);

        // Then
        assertThat(preferentialSeats).isEmpty();
    }
}
