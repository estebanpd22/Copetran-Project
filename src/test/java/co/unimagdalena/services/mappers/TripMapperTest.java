package co.unimagdalena.services.mappers;

import co.unimagdalena.api.dto.TripDto;
import co.unimagdalena.domine.entities.*;
import co.unimagdalena.services.mapper.TripMapper;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.LocalDate;
import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;

public class TripMapperTest {
    private final TripMapper mapper = Mappers.getMapper(TripMapper.class);

    @Test
    void toEntity_shouldMapToCreate() {
        LocalDate date = LocalDate.of(2025, 1, 15);
        OffsetDateTime departure = OffsetDateTime.now();
        OffsetDateTime arrival = departure.plusHours(3);

        TripDto.TripCreateRequest request = new TripDto.TripCreateRequest(
                date,
                departure,
                arrival,
                TripStatus.SCHEDULED,
                1L,
                1L
        );

        Trip trip = mapper.toEntity(request);

        assertThat(trip.getDate()).isEqualTo(date);
        assertThat(trip.getDepartureAt()).isEqualTo(departure);
        assertThat(trip.getArrivalAt()).isEqualTo(arrival);
        assertThat(trip.getStatus()).isEqualTo(TripStatus.SCHEDULED);
    }

    @Test
    void toResponse_shouldMapEntity() {
        Route route = Route.builder().id(1L).code("R001").name("Route 1").build();
        Bus bus = Bus.builder().id(1L).plate("ABC-123").build();

        LocalDate date = LocalDate.of(2025, 1, 15);
        OffsetDateTime departure = OffsetDateTime.now();
        OffsetDateTime arrival = departure.plusHours(3);

        Trip trip = Trip.builder()
                .id(1L)
                .date(date)
                .departureAt(departure)
                .arrivalAt(arrival)
                .status(TripStatus.SCHEDULED)
                .route(route)
                .bus(bus)
                .build();

        TripDto.TripResponse dto = mapper.toResponse(trip);

        assertThat(dto.id()).isEqualTo(1L);
        assertThat(dto.date()).isEqualTo(date);
        assertThat(dto.status()).isEqualTo(TripStatus.SCHEDULED);
        assertThat(dto.route()).isNotNull();
        assertThat(dto.bus()).isNotNull();
    }

    @Test
    void toSummary_shouldMapEntity() {
        Route route = Route.builder().id(1L).code("R001").name("Route 1").build();

        LocalDate date = LocalDate.of(2025, 1, 15);
        OffsetDateTime departure = OffsetDateTime.now();
        OffsetDateTime arrival = departure.plusHours(3);

        Trip trip = Trip.builder()
                .id(1L)
                .date(date)
                .departureAt(departure)
                .arrivalAt(arrival)
                .status(TripStatus.SCHEDULED)
                .route(route)
                .build();

        TripDto.TripSummary dto = mapper.toSummary(trip);

        assertThat(dto.id()).isEqualTo(1L);
        assertThat(dto.date()).isEqualTo(date);
        assertThat(dto.status()).isEqualTo(TripStatus.SCHEDULED);
        assertThat(dto.route()).isNotNull();
    }

    @Test
    void updateEntity_shouldUpdateFields() {
        Trip trip = Trip.builder()
                .id(1L)
                .date(LocalDate.of(2025, 1, 15))
                .status(TripStatus.SCHEDULED)
                .build();

        LocalDate newDate = LocalDate.of(2025, 1, 20);

        TripDto.TripUpdateRequest update = new TripDto.TripUpdateRequest(
                newDate,
                null,
                null,
                TripStatus.DEPARTED,
                null,
                null
        );

        mapper.updateEntity(update, trip);

        assertThat(trip.getDate()).isEqualTo(newDate);
        assertThat(trip.getStatus()).isEqualTo(TripStatus.DEPARTED);
    }
}
